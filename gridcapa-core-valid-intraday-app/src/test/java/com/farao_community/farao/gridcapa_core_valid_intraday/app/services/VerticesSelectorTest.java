/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.SelectedVertexData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils;
import com.powsybl.iidm.network.Country;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceExchangeData;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getIdsFromSelectedVertexData;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestCnecRamSelectedVertices;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestVertices;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.getTestVertices2;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.mapVerticesToSelectedVerticesData;
import static org.assertj.core.api.Assertions.assertThat;

class VerticesSelectorTest {
    private static final double PONDERATION = 0.33;
    private final VerticesSelector selector = new VerticesSelector(new TestUtils.TestCoreHubConf());

    @Test
    void selectionSynthesisTest() {
        final CoreValidIntradayTaskParameters coreValidIntradayTaskParameters = TestUtils.getTestCoreValidIntradayTaskParameters();

        final List<SelectedVertexData> rankedVertices1 = selector.selectionSynthesis(getTestVertices2(), getTestRefProg(), getTestCnecRamSelectedVertices(), coreValidIntradayTaskParameters);

        assertThat(getIdsFromSelectedVertexData(rankedVertices1))
                .isNotEmpty()
                .hasSize(5)
                .containsExactly(2, 5, 1, 4, 3);

        //order: 1, 5, 4, 3, 2
        assertThat(rankedVertices1.get(0).getScore()).isEqualTo(1.24);
        assertThat(rankedVertices1.get(1).getScore()).isEqualTo(1.33);
        assertThat(rankedVertices1.get(2).getScore()).isEqualTo(2.3);
        assertThat(rankedVertices1.get(3).getScore()).isEqualTo(2.31);
        assertThat(rankedVertices1.get(4).getScore()).isEqualTo(2.62);

        final List<SelectedVertexData> rankedVertices2 = selector.selectionSynthesis(getTestVertices2(), getTestRefProg(), getTestCnecRamSelectedVertices(), TestUtils.getTestCoreValidIntradayTaskParametersMaxSelect2());

        assertThat(getIdsFromSelectedVertexData(rankedVertices2))
                .isNotEmpty()
                .hasSize(4)
                .containsExactly(2, 5, 1, 4);
        //order: 1, 5, 4, 3
        assertThat(rankedVertices2.get(0).getScore()).isEqualTo(1.24);
        assertThat(rankedVertices2.get(1).getScore()).isEqualTo(1.33);
        assertThat(rankedVertices2.get(2).getScore()).isEqualTo(2.3);
        assertThat(rankedVertices2.get(3).getScore()).isEqualTo(2.31);
    }

    @Test
    void shouldOrderByDistance() {
        final List<SelectedVertexData> selectedVertices = mapVerticesToSelectedVerticesData(getTestVertices());
        selector.orderByDistance(selectedVertices, getTestRefProg(), PONDERATION);

        // the list isn't ordered any more but scored
        assertThat(getIdsFromSelectedVertexData(selectedVertices)).containsExactly(1, 2, 3, 4, 5);
        assertThat(selectedVertices.get(4).getNormDistance())
                .isLessThanOrEqualTo(selectedVertices.get(0).getNormDistance());
        assertThat(selectedVertices.get(0).getNormDistance())
                .isLessThanOrEqualTo(selectedVertices.get(3).getNormDistance());
        assertThat(selectedVertices.get(3).getNormDistance())
                .isLessThanOrEqualTo(selectedVertices.get(2).getNormDistance());
        assertThat(selectedVertices.get(2).getNormDistance())
                .isLessThanOrEqualTo(selectedVertices.get(1).getNormDistance());

        assertThat(selectedVertices.get(0).getNormDirection()).isNull();
        assertThat(selectedVertices.get(1).getNormDirection()).isNull();
        assertThat(selectedVertices.get(2).getNormDirection()).isNull();
        assertThat(selectedVertices.get(3).getNormDirection()).isNull();
        assertThat(selectedVertices.get(4).getNormDirection()).isNull();

        assertThat(selectedVertices.get(0).getNormRam()).isNull();
        assertThat(selectedVertices.get(1).getNormRam()).isNull();
        assertThat(selectedVertices.get(2).getNormRam()).isNull();
        assertThat(selectedVertices.get(3).getNormRam()).isNull();
        assertThat(selectedVertices.get(4).getNormRam()).isNull();

        assertThat(selectedVertices.get(0).getScore()).isNotNull();
        assertThat(selectedVertices.get(1).getScore()).isNotNull();
        assertThat(selectedVertices.get(2).getScore()).isNotNull();
        assertThat(selectedVertices.get(3).getScore()).isNotNull();
        assertThat(selectedVertices.get(4).getScore()).isNotNull();
    }

    @Test
    void shouldOrderByConstrainedValue() {
        final List<SelectedVertexData> selectedVertices = mapVerticesToSelectedVerticesData(getTestVertices2());
        selector.orderByConstrainedValue(selectedVertices, getTestCnecRamSelectedVertices(), PONDERATION);
        assertThat(selectedVertices).hasSize(5);

        //order should be 2, 5, 1, 3, 4
        assertThat(selectedVertices.get(1).getNormRam())
                .isLessThanOrEqualTo(selectedVertices.get(4).getNormRam());
        assertThat(selectedVertices.get(4).getNormRam())
                .isLessThanOrEqualTo(selectedVertices.get(3).getNormRam());
        assertThat(selectedVertices.get(3).getNormRam())
                .isLessThanOrEqualTo(selectedVertices.get(0).getNormRam());
        assertThat(selectedVertices.get(0).getNormRam())
                .isLessThanOrEqualTo(selectedVertices.get(2).getNormRam());

        assertThat(selectedVertices.get(0).getNormDirection()).isNull();
        assertThat(selectedVertices.get(1).getNormDirection()).isNull();
        assertThat(selectedVertices.get(2).getNormDirection()).isNull();
        assertThat(selectedVertices.get(3).getNormDirection()).isNull();
        assertThat(selectedVertices.get(4).getNormDirection()).isNull();

        assertThat(selectedVertices.get(0).getNormDistance()).isNull();
        assertThat(selectedVertices.get(1).getNormDistance()).isNull();
        assertThat(selectedVertices.get(2).getNormDistance()).isNull();
        assertThat(selectedVertices.get(3).getNormDistance()).isNull();
        assertThat(selectedVertices.get(4).getNormDistance()).isNull();

        assertThat(selectedVertices.get(0).getScore()).isNotNull();
        assertThat(selectedVertices.get(1).getScore()).isNotNull();
        assertThat(selectedVertices.get(2).getScore()).isNotNull();
        assertThat(selectedVertices.get(3).getScore()).isNotNull();
        assertThat(selectedVertices.get(4).getScore()).isNotNull();

    }

    @Test
    void shouldOrderByAngle() {
        final List<SelectedVertexData> selectedVertices = mapVerticesToSelectedVerticesData(getTestVertices());
        selector.orderByAngle(selectedVertices, getTestRefProg(), PONDERATION);
        assertThat(getIdsFromSelectedVertexData(selectedVertices)).containsExactly(1, 2, 3, 4, 5);

        // order should be 2 3 4 5 1
        assertThat(selectedVertices.get(1).getNormDirection())
                .isLessThanOrEqualTo(selectedVertices.get(2).getNormDirection());
        assertThat(selectedVertices.get(2).getNormDirection())
                .isLessThanOrEqualTo(selectedVertices.get(3).getNormDirection());
        assertThat(selectedVertices.get(3).getNormDirection())
                .isLessThanOrEqualTo(selectedVertices.get(4).getNormDirection());
        assertThat(selectedVertices.get(4).getNormDirection())
                .isLessThanOrEqualTo(selectedVertices.get(0).getNormDirection());

        assertThat(selectedVertices.get(0).getScore()).isNotNull();
        assertThat(selectedVertices.get(1).getScore()).isNotNull();
        assertThat(selectedVertices.get(2).getScore()).isNotNull();
        assertThat(selectedVertices.get(3).getScore()).isNotNull();
        assertThat(selectedVertices.get(4).getScore()).isNotNull();

        final List<SelectedVertexData> angleOnlySelection = mapVerticesToSelectedVerticesData(List.of(new Vertex(6, Map.of("AA", -600, "BB", 1200, "CC", -600)),
                                                                                                      new Vertex(7, Map.of("AA", -300, "BB", 600, "CC", 0))
        ));
        selector.orderByAngle(angleOnlySelection, getTestRefProg(), PONDERATION);
        assertThat(getIdsFromSelectedVertexData(angleOnlySelection)).containsExactly(6, 7);

        // order should be 7 6
        assertThat(angleOnlySelection.get(1).getNormDirection())
                .isLessThanOrEqualTo(angleOnlySelection.get(0).getNormDirection());

        assertThat(angleOnlySelection.get(0).getNormDistance()).isNull();
        assertThat(angleOnlySelection.get(1).getNormDistance()).isNull();

        assertThat(angleOnlySelection.get(0).getNormRam()).isNull();
        assertThat(angleOnlySelection.get(1).getNormRam()).isNull();

        assertThat(angleOnlySelection.get(0).getScore()).isNotNull();
        assertThat(angleOnlySelection.get(1).getScore()).isNotNull();
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
        public double getGlobalNetPosition(final EICode area) {
            return (area.equals(new EICode(Country.FR)) || area.equals(new EICode(Country.BE))) ? -300.0 : 600.0;
        }
    }
}
