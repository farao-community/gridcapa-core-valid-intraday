/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services.dichotomy;

import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.rao_runner.api.resource.RaoSuccessResponse;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class DichotomyParallelizationWorker {

    private final DichotomyRunner dichotomyRunner;

    public DichotomyParallelizationWorker(final DichotomyRunner dichotomyRunner) {
        this.dichotomyRunner = dichotomyRunner;
    }

    @Async("threadPoolTaskExecutor")
    public Future<DichotomyResult<RaoSuccessResponse>> runDichotomyForOneVertex(final Vertex vertex,
                                                                                final CoreValidIntradayRequest coreValidIntradayRequest,
                                                                                final ShiftStudyPointData shiftStudyPointData) {
        // propagate in logs MDC the task requestId as an extra field to be able to send logs with calculation tasks.
        MDC.put("gridcapa-task-id", coreValidIntradayRequest.getId());
        final DichotomyResult<RaoSuccessResponse> dichotomyResult = dichotomyRunner.runDichotomy(vertex,
                                                                                                 coreValidIntradayRequest,
                                                                                                 shiftStudyPointData);
        return CompletableFuture.completedFuture(dichotomyResult);
    }
}
