/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.powsybl.iidm.network.Country;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComputeUtilsTest {

    private static final String FRANCE_EI_CODE = "10YFR-RTE------C";
    private static final String DE_HVDC_EI_CODE = "22Y201903145---4";

    @Test
    void getEICodeFromCountryHubTest() {
        final CoreHub coreHub = mock(CoreHub.class);
        when(coreHub.country()).thenReturn(Country.FR);
        when(coreHub.isHvdcHub()).thenReturn(false);
        final EICode eiCodeFromHub = ComputeUtils.getEICodeFromHub(coreHub);
        Assertions.assertThat(eiCodeFromHub).isEqualTo(new EICode(FRANCE_EI_CODE));
    }

    @Test
    void getEICodeFromHvdcHubTest() {
        final CoreHub coreHub = mock(CoreHub.class);
        when(coreHub.clusterVerticeCode()).thenReturn("DE_AL");
        when(coreHub.isHvdcHub()).thenReturn(true);
        final EICode eiCodeFromHub = ComputeUtils.getEICodeFromHub(coreHub);
        Assertions.assertThat(eiCodeFromHub).isEqualTo(new EICode(DE_HVDC_EI_CODE));
    }

    @Test
    void getEICodeFromUnknownHvdcHubTest() {
        final CoreHub coreHub = mock(CoreHub.class);
        when(coreHub.clusterVerticeCode()).thenReturn("unknown");
        when(coreHub.isHvdcHub()).thenReturn(true);
        assertThatThrownBy(() -> ComputeUtils.getEICodeFromHub(coreHub))
                .doesNotThrowAnyExceptionExcept(IllegalArgumentException.class);
    }

    @Test
    void getEICodeFromUnknownCountryHubTest() {
        final CoreHub coreHub = mock(CoreHub.class);
        when(coreHub.isHvdcHub()).thenReturn(false);
        assertThatThrownBy(() -> ComputeUtils.getEICodeFromHub(coreHub))
                .doesNotThrowAnyExceptionExcept(NullPointerException.class);
    }

    @Test
    void getEICodeFromClusterVerticeCodeKnownCountry() {
        final EICode eiCodeFromHub1 = ComputeUtils.getEICodeFromClusterVerticeCode("FR");
        Assertions.assertThat(eiCodeFromHub1).isEqualTo(new EICode(FRANCE_EI_CODE));
    }

    @Test
    void getEICodeFromClusterVerticeCodeKnownHvdc() {
        final EICode eiCodeFromHub1 = ComputeUtils.getEICodeFromClusterVerticeCode("DE_AL");
        Assertions.assertThat(eiCodeFromHub1).isEqualTo(new EICode(DE_HVDC_EI_CODE));
    }

    @Test
    void getEICodeFromClusterVerticeCodeUnknownCountry() {
        assertThatThrownBy(() -> ComputeUtils.getEICodeFromClusterVerticeCode("ZZ"))
                .doesNotThrowAnyExceptionExcept(IllegalArgumentException.class);
    }

    @Test
    void getEICodeFromClusterVerticeCodeUnknownHvdc() {
        assertThatThrownBy(() -> ComputeUtils.getEICodeFromClusterVerticeCode("AAAA"))
                .doesNotThrowAnyExceptionExcept(IllegalArgumentException.class);
    }

    @Test
    void computeCnecVertexProductError() {
        final CoreHubsConfiguration hubs = new TestUtils.TestCoreHubConf();
        final Vertex vertex = new Vertex(1, Map.of("ZZ1", 10000, "ZZ2", 1000, "ZZ3", 100));
        final CnecRamBranchData cnec = new CnecRamBranchData("id1", 1450, 157, 15, true, false, true, Map.of("ZZ1", BigDecimal.valueOf(.2), "ZZ2", BigDecimal.valueOf(0.3), "ZZ3", BigDecimal.valueOf(.4)));
        Assertions.assertThatThrownBy(() -> ComputeUtils.computeCnecVertexProduct(vertex, cnec, hubs.getCoreHubs()))
                .doesNotThrowAnyExceptionExcept(NullPointerException.class);
    }

    @Test
    void computeCnecVertexProduct() {
        final CoreHubsConfiguration hubs = new TestUtils.TestCoreHubConf();
        final Vertex vertex = new Vertex(1, Map.of("AA", 10000, "BB", 1000, "CC", 100));
        final CnecRamBranchData cnec = new CnecRamBranchData("id1", 1450, 157, 15, true, false, true, Map.of("AA", BigDecimal.valueOf(.2), "BB", BigDecimal.valueOf(0.3), "CC", BigDecimal.valueOf(.4)));
        Assertions.assertThat(ComputeUtils.computeCnecVertexProduct(vertex, cnec, hubs.getCoreHubs())).isEqualByComparingTo(BigDecimal.valueOf(2340));
    }

    @Test
    void getGlobalNetPostionsByEICode() {
        final CoreHubsConfiguration hubs = new TestUtils.TestCoreHubReducedRealConf();
        final ReferenceProgram referenceProgram = mock(ReferenceProgram.class);
        when(referenceProgram.getGlobalNetPosition(any(EICode.class))).thenReturn(125.0);
        final List<CoreHub> coreHubs = hubs.getCoreHubs();
        final Map<String, Double> globalNetPostionsByEICode = ComputeUtils.getGlobalNetPostionsByEICode(referenceProgram, coreHubs);
        assertThat(globalNetPostionsByEICode).hasSize(coreHubs.size());
        assertThat(globalNetPostionsByEICode).containsEntry(FRANCE_EI_CODE, 125.0)
                .containsEntry(DE_HVDC_EI_CODE, 125.0);
        assertThat(globalNetPostionsByEICode.get("ANYTHING")).isNull();
    }
}
