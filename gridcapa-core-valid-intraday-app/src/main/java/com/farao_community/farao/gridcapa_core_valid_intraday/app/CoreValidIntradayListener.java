/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatusUpdate;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.JsonApiConverter;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.AbstractCoreValidIntradayException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@Component
public class CoreValidIntradayListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidIntradayListener.class);
    private static final String TASK_STATUS_UPDATE = "task-status-update";

    private final JsonApiConverter jsonApiConverter;
    private final CoreValidIntradayHandler coreValidIntradayHandler;
    private final StreamBridge streamBridge;

    public CoreValidIntradayListener(final CoreValidIntradayHandler coreValidIntradayHandler,
                                     final StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
        this.jsonApiConverter = new JsonApiConverter();
        this.coreValidIntradayHandler = coreValidIntradayHandler;
    }

    @Bean
    public Consumer<Flux<byte[]>> request() {
        return flux -> flux
                .doOnNext(this::onMessage)
                .subscribe();
    }

    public void onMessage(final byte[] req) {
        try {
            final CoreValidIntradayRequest coreValidIntradayRequest = jsonApiConverter.fromJsonMessage(req, CoreValidIntradayRequest.class);
            runCoreValidIntradayRequest(coreValidIntradayRequest);
        } catch (final RuntimeException e) {
            LOGGER.error("Core valid exception occurred", e);
        }
    }

    private void runCoreValidIntradayRequest(final CoreValidIntradayRequest coreValidIntradayRequest) {
        try {
            LOGGER.info("Core valid request received: {}", coreValidIntradayRequest);
            streamBridge.send(TASK_STATUS_UPDATE, new TaskStatusUpdate(UUID.fromString(coreValidIntradayRequest.getId()), TaskStatus.RUNNING));
            final String coreValidIntradayResponseId = coreValidIntradayHandler.handleCoreValidIntradayRequest(coreValidIntradayRequest);
            updateTaskStatus(coreValidIntradayResponseId, coreValidIntradayRequest.getTimestamp(), TaskStatus.SUCCESS);
        } catch (final AbstractCoreValidIntradayException e) {
            LOGGER.error("Core valid exception occurred", e);
            updateTaskStatus(coreValidIntradayRequest.getId(), coreValidIntradayRequest.getTimestamp(), TaskStatus.ERROR);
        } catch (final RuntimeException e) {
            LOGGER.error("Unknown exception occurred", e);
            updateTaskStatus(coreValidIntradayRequest.getId(), coreValidIntradayRequest.getTimestamp(), TaskStatus.ERROR);
        }
    }

    private void updateTaskStatus(final String requestId,
                                  final OffsetDateTime timestamp,
                                  final TaskStatus targetStatus) {
        streamBridge.send(TASK_STATUS_UPDATE, new TaskStatusUpdate(UUID.fromString(requestId), targetStatus));
        LOGGER.info("Updating task status to {} for timestamp {}", targetStatus, timestamp);
    }

}
