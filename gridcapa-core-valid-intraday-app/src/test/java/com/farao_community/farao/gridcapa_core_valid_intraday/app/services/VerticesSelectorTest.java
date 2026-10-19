/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecVertexRamData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils;
import com.powsybl.iidm.network.Country;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceExchangeData;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestVertices;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestVertices2;
import static org.assertj.core.api.Assertions.assertThat;

class VerticesSelectorTest {
    private final VerticesSelector selector = new VerticesSelector(new TestUtils.TestCoreHubConf());

    @Test
    void shouldOrderByClosest() {
        final List<Vertex> selectedVertices = selector.orderByDistance(getTestVertices(), getTestRefProg());

        assertThat(getIds(selectedVertices)).containsExactly(1, 5, 4, 3, 2);
    }

    @Test
    void shouldOrderByConstrained() {

        final List<CnecVertexRamData> selectedVertices = selector.orderByConstrainedValue(getTestVertices2(), getTestCnecRam());
        assertThat(selectedVertices).hasSize(5);
        assertThat(selectedVertices.getFirst().ram()).isLessThanOrEqualTo(selectedVertices.getLast().ram());
        assertThat(selectedVertices.getFirst().ram()).isEqualTo(251);

    }

    private static List<CnecRamBranchData> getTestCnecRam() {
        final CnecRamBranchData cnec1 = new CnecRamBranchData("id1", 1450, 157, 1400, true, true, Map.of("fb1", BigDecimal.valueOf(.3345), "fb2", BigDecimal.valueOf(0.156), "fb3", BigDecimal.valueOf(.78)));
        final CnecRamBranchData cnec2 = new CnecRamBranchData("id2", 3450, 257, 1400, true, true, Map.of("fb1", BigDecimal.valueOf(.345), "fb2", BigDecimal.valueOf(0.256), "fb3", BigDecimal.valueOf(.78)));

        return List.of(cnec1, cnec2);
    }

    @Test
    void shouldOrderByClosestByAngle() {
        final List<Vertex> selectedVertice = selector.orderByAngle(List.of(new Vertex(1, Map.of("AA", -301, "BB", 600, "CC", -300))), getTestRefProg());
        assertThat(getIds(selectedVertice)).containsExactly(1);

        final List<Vertex> selectedVertices = selector.orderByAngle(getTestVertices(), getTestRefProg());
        assertThat(getIds(selectedVertices)).containsExactly(1, 5, 4, 3, 2);

        final List<Vertex> angleOnlySelection = selector.orderByAngle(
                List.of(new Vertex(6, Map.of("AA", -600, "BB", 1200, "CC", -600)),
                        new Vertex(7, Map.of("AA", -300, "BB", 600, "CC", 0))),
                getTestRefProg());
        assertThat(getIds(angleOnlySelection)).containsExactly(6, 7);
    }

    private ReferenceProgram getTestRefProg() {
        // net positions : AA = -300 , BB = 600, CC = -300
        return new TestRefProg();
    }

    private static List<Integer> getIds(final List<Vertex> vertices) {
        return vertices.stream().map(Vertex::vertexId).toList();
    }

    @Test
    void selectionSynthesis() {
        final CoreValidIntradayTaskParameters coreValidIntradayTaskParameters = TestUtils.getTestCoreValidIntradayTaskParameters();

        final List<Vertex> rankedVertices1 = selector.selectionSynthesis(getTestVertices(), getTestRefProg(), getTestCnecRam(), coreValidIntradayTaskParameters);

        assertThat(getIds(rankedVertices1))
                .isNotEmpty()
                .hasSize(5)
                .containsExactly(1, 5, 4, 3, 2);
        final List<Vertex> rankedVertices2 = selector.selectionSynthesis(getTestVertices(), getTestRefProg(), getTestCnecRam(), TestUtils.getTestCoreValidIntradayTaskParametersMaxSelect2());

        assertThat(getIds(rankedVertices2))
                .isNotEmpty()
                .hasSize(4)
                .containsExactly(1, 5, 4, 3);
    }

    private static class TestRefProg extends ReferenceProgram {
        public TestRefProg(final List<ReferenceExchangeData> referenceExchangeDataList) {
            super(referenceExchangeDataList);
        }

        public TestRefProg() {
            super(new ArrayList<>());
        }

        @Override
        public double getGlobalNetPosition(final EICode area) {
            return (area.equals(new EICode(Country.FR)) || area.equals(new EICode(Country.BE))) ? -300.0 : 600.0;
        }
    }
}
