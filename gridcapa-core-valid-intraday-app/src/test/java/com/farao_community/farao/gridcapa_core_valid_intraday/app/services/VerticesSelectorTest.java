/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceExchangeData;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class VerticesSelectorTest {
    private final VerticesSelector selector = new VerticesSelector(new TestCoreHubConf());

    @Test
    void shouldSelectWithNSphere() {

        final List<Vertex> selectedVertices2for3 = selector.selectVerticesWithinNSphere(getTestVertices(),
                                                                                        getTestRefProg(),
                                                                                        10.0,
                                                                                        3,
                                                                                        false);

        final List<Vertex> selectedVertices2to3for3 = selector.selectVerticesWithinNSphere(getTestVertices(),
                                                                                           getTestRefProg(),
                                                                                           10.0,
                                                                                           3,
                                                                                           true);

        final List<Vertex> selectedVertices4for3 = selector.selectVerticesWithinNSphere(getTestVertices(),
                                                                                        getTestRefProg(),
                                                                                        1000.0,
                                                                                        3,
                                                                                        false);

        final List<Vertex> selectedVertices4to3for3 = selector.selectVerticesWithinNSphere(getTestVertices(),
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
        final List<Vertex> selectedVertices = selector.selectClosestVertices(getTestVertices(),
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

    private static class TestCoreHubConf extends CoreHubsConfiguration {
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

}
