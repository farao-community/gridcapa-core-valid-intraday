/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.VertexToFrenchCnecs;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestCnecRam1;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestCnecRam2;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestCoreValidIntradayTaskParametersMaxSelect2;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestVertices3;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DichotomyMethodFinderTest {

    @Test
    void getFrenchCnecsDefiningVerticesOK() {

        Logger businessLogger = mock(Logger.class);
        TestUtils.TestCoreHubConf testCoreHubsConf = new TestUtils.TestCoreHubConf();
        List<VertexToFrenchCnecs>  vertexToFrenchCnecs = DichotomyMethodFinder.getFrenchCnecsDefiningVertices(getTestVertices3(),
                                                                                                              getTestCnecRam1(),
                                                                                                              testCoreHubsConf.getCoreHubs(),
                                                                                                              getTestCoreValidIntradayTaskParametersMaxSelect2(),
                                                                                                              businessLogger);
        Assertions.assertThat(vertexToFrenchCnecs).hasSize(2);
        Assertions.assertThat(vertexToFrenchCnecs.getFirst().vertex().vertexId()).isEqualTo(1);
        Assertions.assertThat(vertexToFrenchCnecs.getFirst().frenchCnecs()).hasSize(1);
        Assertions.assertThat(vertexToFrenchCnecs.getFirst().frenchCnecs().getFirst().branchId()).isEqualTo("id2");
        Assertions.assertThat(vertexToFrenchCnecs.getLast().vertex().vertexId()).isEqualTo(3);
        Assertions.assertThat(vertexToFrenchCnecs.getLast().frenchCnecs()).hasSize(1);
        Assertions.assertThat(vertexToFrenchCnecs.getLast().frenchCnecs().getFirst().branchId()).isEqualTo("id5");
        verify(businessLogger, atLeastOnce()).info("Vertex with id: {}, has no corresponding french Cnecs with correct direction", 2);
    }

    @Test
    void getFrenchCnecsDefiningVerticesEmpty() {

        Logger businessLogger =  mock(Logger.class);
        TestUtils.TestCoreHubConf testCoreHubsConf = new TestUtils.TestCoreHubConf();
        List<VertexToFrenchCnecs>  vertexToFrenchCnecs = DichotomyMethodFinder.getFrenchCnecsDefiningVertices(getTestVertices3(),
                                                                                                              getTestCnecRam2(),
                                                                                                              testCoreHubsConf.getCoreHubs(),
                                                                                                              getTestCoreValidIntradayTaskParametersMaxSelect2(),
                                                                                                              businessLogger);
        Assertions.assertThat(vertexToFrenchCnecs).isEmpty();
        verify(businessLogger, times(5)).info(eq("Vertex with id: {}, has no corresponding french Cnecs with correct direction"), anyInt());
    }
}
