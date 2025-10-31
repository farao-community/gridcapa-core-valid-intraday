/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertice.Vertice;
import com.farao_community.farao.gridcapa_core_valid_commons.vertice.VerticeImporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import com.powsybl.openrao.data.refprog.refprogxmlimporter.RefProgImporter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImporter.class);

    public FileImporter(final UrlValidationService urlValidationService,
                        final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
        this.urlValidationService = urlValidationService;
    }

    public ReferenceProgram importReferenceProgram(
            final CoreValidIntradayFileResource refProgFile,
            final OffsetDateTime timestamp) {
        try (final InputStream refProgStream = urlValidationService.openUrlStream(refProgFile.getUrl())) {
            return RefProgImporter.importRefProg(refProgStream, timestamp);
        } catch (final Exception e) {
            throw new CoreValidIntradayInvalidDataException(String.format("Cannot import reference program file from URL '%s'", refProgFile.getUrl()), e);
        }
    }

    public List<Vertice> importVertices(final CoreValidIntradayFileResource verticeFile) {
        try (final InputStream verticefileInputStream = urlValidationService.openUrlStream(verticeFile.getUrl())) {
            return VerticeImporter.importVertices(verticefileInputStream, coreHubs);
        } catch (final Exception e) {
            throw new CoreValidIntradayInvalidDataException(String.format("Cannot import vertice file from URL '%s'", verticeFile.getUrl()), e);
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
