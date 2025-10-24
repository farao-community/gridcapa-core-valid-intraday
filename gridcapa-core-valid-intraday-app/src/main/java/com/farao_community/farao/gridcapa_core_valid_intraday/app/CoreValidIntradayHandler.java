/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@Component
public class CoreValidIntradayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidIntradayHandler.class);
    private static final DateTimeFormatter ARTIFACTS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm");

    private final Logger eventsLogger;
    private final FileImporter fileImporter;
    private final MinioAdapter minioAdapter;

    public CoreValidIntradayHandler(FileImporter fileImporter, MinioAdapter minioAdapter, Logger eventsLogger) {
        this.fileImporter = fileImporter;
        this.minioAdapter = minioAdapter;
        this.eventsLogger = eventsLogger;
    }

    public String handleCoreValidIntradayRequest(CoreValidIntradayRequest coreValidIntradayRequest) {
        final String formattedTimestamp = setUpEventLogging(coreValidIntradayRequest);

        //TODO handle request
        return coreValidIntradayRequest.getId();

    }

    private static String setUpEventLogging(CoreValidIntradayRequest coreValidIntradayRequest) {
        MDC.put("gridcapa-task-id", coreValidIntradayRequest.getId());
        return TIMESTAMP_FORMATTER.format(coreValidIntradayRequest.getTimestamp());
    }

}
