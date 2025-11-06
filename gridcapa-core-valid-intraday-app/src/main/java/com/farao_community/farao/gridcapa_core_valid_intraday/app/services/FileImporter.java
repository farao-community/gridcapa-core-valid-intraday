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
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import com.powsybl.openrao.data.refprog.refprogxmlimporter.RefProgImporter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

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
        try (final InputStream refProgStream = urlValidationService.openUrlStream(refProgFile.getUrl())) {
            return RefProgImporter.importRefProg(refProgStream, timestamp);
        } catch (final Exception e) {
            throw getImportException(refProgFile, e);
        }
    }

    public Network importNetwork(final CoreValidIntradayFileResource cgmFile) {
        try (final InputStream networkInputStream = urlValidationService.openUrlStream(cgmFile.getUrl())) {
            return Network.read(getFilenameFromUrl(cgmFile.getUrl()), networkInputStream);
        } catch (final Exception e) {
            throw getImportException(cgmFile, e);
        }
    }

    public List<Vertex> importVertices(final CoreValidIntradayFileResource verticesFile) {
        try (final InputStream verticefileInputStream = urlValidationService.openUrlStream(verticesFile.getUrl())) {
            return VerticesImporter.importVertices(verticefileInputStream, coreHubs);
        } catch (final Exception e) {
            throw getImportException(verticesFile, e);
        }
    }

    public GlskDocument importGlskFile(final CoreValidIntradayFileResource glskFile) {
        try (final InputStream glskStream = urlValidationService.openUrlStream(glskFile.getUrl())) {
            return GlskDocumentImporters.importGlsk(glskStream);
        } catch (final IOException e) {
            throw getImportException(glskFile, e);
        }
    }

    private CoreValidIntradayInvalidDataException getImportException(final CoreValidIntradayFileResource resource,
                                                                     final Throwable exception) {
        return new CoreValidIntradayInvalidDataException(String.format("Cannot import %s file from URL '%s'", resource.getFilename(), resource.getUrl()),
                                                         exception);
    }

    String getFilenameFromUrl(final String url) {
        try {
            return FilenameUtils.getName(new URI(url).toURL().getPath());
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            throw new CoreValidIntradayInvalidDataException(String.format("URL is invalid: %s", url), e);
        }
    }
}
