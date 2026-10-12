/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.VertexToFrenchCnecs;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    private static List<CnecRamBranchData> getTestCnecRam1() {
        final CnecRamBranchData cnec1 = new CnecRamBranchData("id1", 1450, 157, 15, true, false, Map.of("fb1", BigDecimal.valueOf(.1), "fb2", BigDecimal.valueOf(0.1), "fb3", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec2 = new CnecRamBranchData("id2", 3450, 257, 16, false, true, Map.of("fb1", BigDecimal.valueOf(.101), "fb2", BigDecimal.valueOf(0.101), "fb3", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec3 = new CnecRamBranchData("id3", 3450, 257, 14, false, true, Map.of("fb1", BigDecimal.valueOf(.101), "fb2", BigDecimal.valueOf(0.101), "fb3", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec4 = new CnecRamBranchData("id4", 1450, 157, 360, true, false, Map.of("fb1", BigDecimal.valueOf(.9), "fb2", BigDecimal.valueOf(0.9), "fb3", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec5 = new CnecRamBranchData("id5", 3450, 257, 365, false, true, Map.of("fb1", BigDecimal.valueOf(.9), "fb2", BigDecimal.valueOf(0.9), "fb3", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec6 = new CnecRamBranchData("id6", 3450, 257, 360, false, true, Map.of("fb1", BigDecimal.valueOf(.9), "fb2", BigDecimal.valueOf(0.9), "fb3", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec7 = new CnecRamBranchData("id7", 1450, 157, 360, false, false, Map.of("fb1", BigDecimal.valueOf(.9), "fb2", BigDecimal.valueOf(0.9), "fb3", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec8 = new CnecRamBranchData("id8", 3450, 257, 365, true, true, Map.of("fb1", BigDecimal.valueOf(.9), "fb2", BigDecimal.valueOf(0.9), "fb3", BigDecimal.valueOf(.9)));

        return List.of(cnec1, cnec2, cnec3, cnec4, cnec5, cnec6, cnec7, cnec8);
    }

    private static List<CnecRamBranchData> getTestCnecRam2() {
        final CnecRamBranchData cnec1 = new CnecRamBranchData("id1", 1450, 157, 1000, true, false, Map.of("fb1", BigDecimal.valueOf(.1), "fb2", BigDecimal.valueOf(0.2), "fb3", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec2 = new CnecRamBranchData("id2", 3450, 257, 700, false, true, Map.of("fb1", BigDecimal.valueOf(.101), "fb2", BigDecimal.valueOf(0.201), "fb3", BigDecimal.valueOf(.1)));

        return List.of(cnec1, cnec2);
    }

}
