/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.FlowBasedDomainBranchData;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.VerticesUtils;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceExchangeData;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class VerticesSelectorTest {
    private final VerticesSelector selector = new VerticesSelector(new TestCoreHubConf());
    private static MockedStatic<VerticesUtils> verticesUtilMock;

    @BeforeAll
    static void init() {
        verticesUtilMock = mockStatic(VerticesUtils.class);
    }

    @AfterAll
    static void close() {
        verticesUtilMock.close();
    }

    @Test
    void shouldSelectWithNSphere() {
        verticesUtilMock.when(() -> VerticesUtils.getVerticesProjectedOnDomain(any(), any(), any()))
            .thenReturn(getTestVertices());

        final List<Vertex> selectedVertices2for3 = selector.selectVerticesWithControlZone(getTestVertices(),
                                                                                          getTestBranches(),
                                                                                          getTestRefProg(),
                                                                                          10.0,
                                                                                          3,
                                                                                          false);

        final List<Vertex> selectedVertices2to3for3 = selector.selectVerticesWithControlZone(getTestVertices(),
                                                                                             getTestBranches(),
                                                                                             getTestRefProg(),
                                                                                             10.0,
                                                                                             3,
                                                                                             true);

        final List<Vertex> selectedVertices4for3 = selector.selectVerticesWithControlZone(getTestVertices(),
                                                                                          getTestBranches(),
                                                                                          getTestRefProg(),
                                                                                          1000.0,
                                                                                          3,
                                                                                          false);

        final List<Vertex> selectedVertices4to3for3 = selector.selectVerticesWithControlZone(getTestVertices(),
                                                                                             getTestBranches(),
                                                                                             getTestRefProg(),
                                                                                             1000.0,
                                                                                             3,
                                                                                             true);

        Assertions.assertThat(selectedVertices2for3.stream()
                                  .map(Vertex::vertexId)
                                  .toList())
            .containsExactlyInAnyOrder(1, 5);

        Assertions.assertThat(selectedVertices2to3for3.stream()
                                  .map(Vertex::vertexId)
                                  .toList())
            .containsExactlyInAnyOrder(1, 4, 5);

        Assertions.assertThat(selectedVertices4for3.stream()
                                  .map(Vertex::vertexId)
                                  .toList())
            .containsExactlyInAnyOrder(1, 3, 4, 5);

        Assertions.assertThat(selectedVertices4to3for3.stream()
                                  .map(Vertex::vertexId)
                                  .toList())
            .containsExactlyInAnyOrder(1, 4, 5);
    }

    @Test
    void shouldSelectClosest() {
        verticesUtilMock.when(() -> VerticesUtils.getVerticesProjectedOnDomain(any(), any(), any()))
            .thenReturn(getTestVertices());

        final List<Vertex> selectedVertices = selector.selectVerticesByDistance(getTestVertices(),
                                                                                getTestBranches(),
                                                                                getTestRefProg(),
                                                                                2);

        Assertions.assertThat(selectedVertices.stream()
                                  .map(Vertex::vertexId)
                                  .toList())
            .containsExactlyInAnyOrder(1, 5);
    }

    private ReferenceProgram getTestRefProg() {
        // net positions : AA = -300 , BB = 600, CC = -300
        return new TestRefProg();
    }

    private static class TestRefProg extends ReferenceProgram {
        public TestRefProg(final List<ReferenceExchangeData> referenceExchangeDataList) {
            super(referenceExchangeDataList);
        }

        public TestRefProg() {
            super(new ArrayList<>());
        }

        @Override
        public double getGlobalNetPosition(final String area) {
            return (area.equals("AA") || area.equals("CC")) ? -300.0 : 600.0;
        }
    }

    private class TestCoreHubConf extends CoreHubsConfiguration {
        public TestCoreHubConf() {
            // test class
        }

        @Override
        public List<CoreHub> getCoreHubs() {
            return List.of(
                new CoreHub("Test1", "ram1", "fb1", "AA", "AA", false, 1),
                new CoreHub("Test2", "ram2", "fb2", "BB", "BB", false, 1),
                new CoreHub("Test3", "ram3", "fb3", "CC", "CC", false, 1)
            );
        }
    }

    private List<Vertex> getTestVertices() {
        return List.of(new Vertex(1, Map.of("AA", -301, "BB", 600, "CC", -300)),
                       new Vertex(2, Map.of("AA", 300, "BB", -600, "CC", -300)),
                       new Vertex(3, Map.of("AA", 300, "BB", 600, "CC", -300)),
                       new Vertex(4, Map.of("AA", -350, "BB", 600, "CC", -300)),
                       new Vertex(5, Map.of("AA", -299, "BB", 600, "CC", -300)));
    }

    private List<TestBranch> getTestBranches() {
        final Map<String, BigDecimal> ptdfs = Map.of(
            "fb1", BigDecimal.valueOf(0.1),
            "fb2", BigDecimal.valueOf(0.2),
            "fb3", BigDecimal.valueOf(0.3)
        );
        return List.of(
            new TestBranch(100, 50, ptdfs)
        );
    }

    private record TestBranch(int getRam0Core, int getAmr,
                              Map<String, BigDecimal> getPtdfValues) implements FlowBasedDomainBranchData {
    }

}
