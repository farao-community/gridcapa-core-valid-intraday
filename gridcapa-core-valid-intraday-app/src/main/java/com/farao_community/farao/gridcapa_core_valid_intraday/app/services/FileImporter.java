/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.VerticesImporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.powsybl.glsk.api.GlskDocument;
import com.powsybl.glsk.api.io.GlskDocumentImporters;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.api.parameters.CracCreationParameters;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintImporter;
import com.powsybl.openrao.data.crac.io.fbconstraint.parameters.FbConstraintCracCreationParameters;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import com.powsybl.openrao.data.refprog.refprogxmlimporter.RefProgImporter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Amira Kahya {@literal <amira.kahya at rte-france.com>}
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@Service
public class FileImporter {
    private final List<CoreHub> coreHubs;
    private final UrlValidationService urlValidationService;

    public FileImporter(final UrlValidationService urlValidationService,
                        final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
        this.urlValidationService = urlValidationService;
    }

    public ReferenceProgram importReferenceProgram(final CoreValidIntradayFileResource refProgFile,
                                                   final OffsetDateTime timestamp) {
        return importFile(refProgFile, is -> RefProgImporter.importRefProg(is, timestamp));
    }

    public Network importNetwork(final CoreValidIntradayFileResource cgmFile) {
        return importFile(cgmFile, is -> Network.read(getFilenameFromUrl(cgmFile.getUrl()), is));
    }

    public List<Vertex> importVertices(final CoreValidIntradayFileResource verticesFile) {
        return importFile(verticesFile, is -> VerticesImporter.importVertices(is, coreHubs));
    }

    public GlskDocument importGlskFile(final CoreValidIntradayFileResource glskFile) {
        return importFile(glskFile, GlskDocumentImporters::importGlsk);
    }

    public FbConstraintCreationContext importMergedCnec(final CoreValidIntradayFileResource mergedCnecFile,  final Network network, final OffsetDateTime targetProcessDateTime) {
        final CracCreationParameters cracCreationParameters = new CracCreationParameters();
        cracCreationParameters.addExtension(FbConstraintCracCreationParameters.class, new FbConstraintCracCreationParameters());
        cracCreationParameters.getExtension(FbConstraintCracCreationParameters.class).setTimestamp(targetProcessDateTime);
        return importFile(mergedCnecFile, is -> (FbConstraintCreationContext) new FbConstraintImporter().importData(is, cracCreationParameters, network));
    }

    public <T> T importFile(final CoreValidIntradayFileResource file,
                            final Function<InputStream, T> inputStreamMapper) {
        try (final InputStream fileContentStream = urlValidationService.openUrlStream(file.getUrl())) {
            return inputStreamMapper.apply(fileContentStream);
        } catch (final Exception e) {
            throw new CoreValidIntradayInvalidDataException(String.format("Cannot import %s file from URL '%s'", file.getFilename(), file.getUrl()), e);
        }
    }

    String getFilenameFromUrl(final String url) {
        try {
            return FilenameUtils.getName(new URI(url).toURL().getPath());
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            throw new CoreValidIntradayInvalidDataException(String.format("URL is invalid: %s", url), e);
        }
    }
}
