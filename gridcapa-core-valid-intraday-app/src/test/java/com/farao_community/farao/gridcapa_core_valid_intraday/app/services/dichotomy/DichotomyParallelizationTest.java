/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services.dichotomy;

import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.rao_runner.api.resource.RaoSuccessResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestVertices;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DichotomyParallelizationTest {

    private static final String FAILED = "failed";

    @Test
    void launchDichotomyAllFailedTest() {
        final Logger businessLogger = mock(Logger.class);
        final DichotomyParallelizationWorker worker = mock(DichotomyParallelizationWorker.class);
        final CoreValidIntradayRequest request = mock(CoreValidIntradayRequest.class);
        final ShiftStudyPointData shiftStudyPointData = mock(ShiftStudyPointData.class);

        when(worker.runDichotomyForOneVertex(any(Vertex.class), any(CoreValidIntradayRequest.class), any(ShiftStudyPointData.class)))
                .thenReturn(CompletableFuture.completedFuture(DichotomyResult.buildFromRaoFailure(FAILED)));

        final DichotomyParallelization dichotomyParallelization = new DichotomyParallelization(worker, businessLogger);
        final List<Vertex> testVertices = getTestVertices();
        final List<Pair<Vertex, DichotomyResult<RaoSuccessResponse>>> results = dichotomyParallelization.launchDichotomy(testVertices, request, shiftStudyPointData);

        verify(worker, Mockito.times(testVertices.size())).runDichotomyForOneVertex(any(), any(), any());
        assertThat(results.size()).isEqualTo(testVertices.size());
        results.forEach(
                result -> {
                    assertThat(result.getValue().isRaoFailed()).isTrue();
                    assertThat(result.getValue().getRaoFailureMessage()).isEqualTo(FAILED);
                }
        );
    }

    @Test
    void launchDichotomyAllOkTest() {
        final Logger businessLogger = mock(Logger.class);
        final DichotomyParallelizationWorker worker = mock(DichotomyParallelizationWorker.class);
        final CoreValidIntradayRequest request = mock(CoreValidIntradayRequest.class);
        final ShiftStudyPointData shiftStudyPointData = mock(ShiftStudyPointData.class);

        final Index<RaoSuccessResponse> index = mock(Index.class);
        final DichotomyResult<RaoSuccessResponse> dichotomyResult = mock(DichotomyResult.class);

        when(worker.runDichotomyForOneVertex(any(Vertex.class), any(CoreValidIntradayRequest.class), any(ShiftStudyPointData.class)))
                .thenReturn(CompletableFuture.completedFuture(dichotomyResult));

        final DichotomyParallelization dichotomyParallelization = new DichotomyParallelization(worker, businessLogger);
        final List<Vertex> testVertices = getTestVertices();
        final List<Pair<Vertex, DichotomyResult<RaoSuccessResponse>>> results = dichotomyParallelization.launchDichotomy(testVertices, request, shiftStudyPointData);

        verify(worker, Mockito.times(testVertices.size())).runDichotomyForOneVertex(any(), any(), any());
        assertThat(results.size()).isEqualTo(testVertices.size());
        results.forEach(
                result -> {
                    assertThat(result.getValue().isRaoFailed()).isFalse();
                    assertThat(result.getValue().getRaoFailureMessage()).isNull();
                }
        );
    }
}
