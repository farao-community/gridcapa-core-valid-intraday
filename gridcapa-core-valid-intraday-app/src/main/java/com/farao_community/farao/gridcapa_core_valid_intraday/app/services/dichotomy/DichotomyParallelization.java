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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.farao_community.farao.dichotomy.api.results.DichotomyResult.buildFromRaoFailure;

@Service
public class DichotomyParallelization {

    private final DichotomyParallelizationWorker worker;
    private final Logger businessLogger;

    public DichotomyParallelization(final DichotomyParallelizationWorker dichotomyParallelizationWorker,
                                    final Logger businessLogger) {
        this.worker = dichotomyParallelizationWorker;
        this.businessLogger = businessLogger;
    }

    public List<Pair<Vertex, DichotomyResult<RaoSuccessResponse>>> launchDichotomy(final List<Vertex> vertices,
                                                                                   final CoreValidIntradayRequest coreValidIntradayRequest,
                                                                                   final ShiftStudyPointData shiftStudyPointData) {

        businessLogger.info("[START] - Starting all dichotomies");
        List<Pair<Vertex, Future<DichotomyResult<RaoSuccessResponse>>>> futures = new ArrayList<>();
        List<Pair<Vertex, DichotomyResult<RaoSuccessResponse>>> results = new ArrayList<>();
        vertices.forEach(vertex -> futures.add(Pair.of(vertex, worker.runDichotomyForOneVertex(vertex,
                                                                                                   coreValidIntradayRequest,
                                                                                                   shiftStudyPointData)))
        );
        futures.forEach(future -> {
            try {
                results.add(Pair.of(future.getKey(), waitAndGet(future)));
            } catch (InterruptedException e) {
                futures.forEach(f -> f.getValue().cancel(true));
                Thread.currentThread().interrupt();
            }
        });
        businessLogger.info("[END] - All dichotomies are done");
        return results;
    }

    private DichotomyResult<RaoSuccessResponse> waitAndGet(Pair<Vertex, Future<DichotomyResult<RaoSuccessResponse>>> dichotomy) throws InterruptedException {
        try {
            return dichotomy.getValue().get();
        } catch (ExecutionException e) {
            final int vertexId = dichotomy.getKey().vertexId();
            final String message = String.format("Error on dichotomy for vertex [%s]", vertexId);
            businessLogger.error(message);
            return buildFromRaoFailure(e.getMessage());
        }
    }
}
