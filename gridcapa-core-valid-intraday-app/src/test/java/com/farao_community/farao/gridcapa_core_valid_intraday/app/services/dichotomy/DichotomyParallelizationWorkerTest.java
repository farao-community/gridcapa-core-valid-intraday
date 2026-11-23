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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class DichotomyParallelizationWorkerTest {

    @Autowired
    private DichotomyParallelizationWorker worker;

    @MockitoBean
    private DichotomyRunner dichotomyRunner;

    @Mock
    private Vertex vertex;

    @Mock
    private CoreValidIntradayRequest coreValidIntradayRequest;

    @Mock
    private ShiftStudyPointData shiftStudyPointData;

    @Test
    void dichotomyParallelizationWorkerTest() throws ExecutionException, InterruptedException {
        DichotomyResult<RaoSuccessResponse> dichotomyResult = mock(DichotomyResult.class);
        when(dichotomyRunner.runDichotomy(any(Vertex.class), any(CoreValidIntradayRequest.class), any(ShiftStudyPointData.class)))
                .thenReturn(dichotomyResult);
        final Future<DichotomyResult<RaoSuccessResponse>> dichotomyResultFuture = worker.runDichotomyForOneVertex(vertex, coreValidIntradayRequest, shiftStudyPointData);

        DichotomyResult<RaoSuccessResponse> dichotomyResult1 = dichotomyResultFuture.get();
        assertThat(dichotomyResultFuture.isDone()).isTrue();
        assertThat(dichotomyResult1).isNotNull();
    }
}
