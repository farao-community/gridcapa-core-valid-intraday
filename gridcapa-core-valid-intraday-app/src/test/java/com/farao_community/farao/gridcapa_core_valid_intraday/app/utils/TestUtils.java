/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa.task_manager.api.TaskParameterDto;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.SelectedVertexData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;
import com.powsybl.iidm.network.Country;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TestUtils {

    private TestUtils() {

    }

    private static List<TaskParameterDto> getTaskParameterDtos() {
        return List.of(
                new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "16", "6"),
                new TaskParameterDto("MARGIN_FOR_PREFILTER", "INT", "100", "1515"),
                new TaskParameterDto("IS_HISTORIC_FILTER_ACTIVE", "BOOLEAN", "true", "false"),
                new TaskParameterDto("IS_HUB_CAPACITY_FILTER_ACTIVE", "BOOLEAN", "true", "false"),
                new TaskParameterDto("FRM_MARGIN_PERCENTAGE", "INT", "5", "45"),
                new TaskParameterDto("MIN_RAM_MCCC", "INT", "20", "25"),
                new TaskParameterDto("PONDERATION_CLOSEST", "INT", "31", "1515"),
                new TaskParameterDto("PONDERATION_ANGLE", "INT", "33", "1515"),
                new TaskParameterDto("PONDERATION_CONSTRAINED", "INT", "34", "1515"),
                new TaskParameterDto("RAM_MARGIN", "INT", "15", "213"),
                new TaskParameterDto("MERGE_ANGLE", "INT", "1", "23"),
                new TaskParameterDto("FR_CNEC_COUNT_LIMIT", "INT", "7", "14")
        );
    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParameters() {
        final List<TaskParameterDto> parameters = getTaskParameterDtos();

        return new CoreValidIntradayTaskParameters(parameters);
    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParametersMaxSelect2() {
        final List<TaskParameterDto> parameters = new java.util.ArrayList<>(getTaskParameterDtos());
        parameters.set(0, new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "2", "6"));
        return new CoreValidIntradayTaskParameters(parameters);
    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParametersFilterHistOff() {
        final List<TaskParameterDto> parameters = new java.util.ArrayList<>(getTaskParameterDtos());
        parameters.set(0, new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "2", "6"));
        parameters.set(2, new TaskParameterDto("IS_HISTORIC_FILTER_ACTIVE", "BOOLEAN", "false", "false"));
        parameters.set(3, new TaskParameterDto("IS_HUB_CAPACITY_FILTER_ACTIVE", "BOOLEAN", "true", "false"));
        return new CoreValidIntradayTaskParameters(parameters);
    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParametersFilterHubOff() {
        final List<TaskParameterDto> parameters = new java.util.ArrayList<>(getTaskParameterDtos());
        parameters.set(0, new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "2", "6"));
        parameters.set(2, new TaskParameterDto("IS_HISTORIC_FILTER_ACTIVE", "BOOLEAN", "true", "false"));
        parameters.set(3, new TaskParameterDto("IS_HUB_CAPACITY_FILTER_ACTIVE", "BOOLEAN", "false", "false"));
        return new CoreValidIntradayTaskParameters(parameters);
    }

    public static CoreValidIntradayTaskParameters getTestCoreValidIntradayTaskParametersBothFiltersOff() {
        final List<TaskParameterDto> parameters = new java.util.ArrayList<>(getTaskParameterDtos());
        parameters.set(0, new TaskParameterDto("MAX_SELECTED_VERTICES", "INT", "2", "6"));
        parameters.set(2, new TaskParameterDto("IS_HISTORIC_FILTER_ACTIVE", "BOOLEAN", "false", "false"));
        parameters.set(3, new TaskParameterDto("IS_HUB_CAPACITY_FILTER_ACTIVE", "BOOLEAN", "false", "false"));
        return new CoreValidIntradayTaskParameters(parameters);
    }

    public static CoreValidIntradayFileResource createFileResource(final String filename,
                                                                   final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }

    public static Set<NetPositionHistory> createNphsFromSeason(final Season season,
                                                               final List<CoreHub> coreHubs) {
        return coreHubs.stream()
                .map(ch -> createNphFromSeasonAndCode(ch.ramcep2Code(), season))
                .collect(Collectors.toSet());

    }

    public static NetPositionHistory createNphFromSeasonAndCode(final String ramcep2Code,
                                                                final Season season) {
        final NetPositionHistory nph = new NetPositionHistory();
        nph.setSeason(season);
        nph.setHubRamcep2Code(ramcep2Code);
        nph.setMinimumNetPosition(-401.0);
        nph.setMaximumNetPosition(999.0);
        return nph;
    }

    public static List<Vertex> getTestVertices() {
        return List.of(new Vertex(1, Map.of("AA", -301, "BB", 600, "CC", -300)),
                       new Vertex(2, Map.of("AA", 300, "BB", -600, "CC", -300)),
                       new Vertex(3, Map.of("AA", 300, "BB", 600, "CC", -300)),
                       new Vertex(4, Map.of("AA", -350, "BB", 600, "CC", -300)),
                       new Vertex(5, Map.of("AA", -299, "BB", 600, "CC", -300)));
    }

    public static List<Vertex> getTestVertices2() {
        return List.of(new Vertex(1, Map.of("AA", -500, "BB", 400, "CC", 300)),
                       new Vertex(2, Map.of("AA", 1300, "BB", -1600, "CC", 1300)),
                       new Vertex(3, Map.of("AA", 200, "BB", 100, "CC", 50)),
                       new Vertex(4, Map.of("AA", -350, "BB", 600, "CC", 300)),
                       new Vertex(5, Map.of("AA", -299, "BB", 600, "CC", 300)));
    }

    public static List<Vertex> getTestVertices3() {
        return List.of(new Vertex(1, Map.of("AA", -100, "BB", 100, "CC", 100)),
                       new Vertex(2, Map.of("AA", 1500, "BB", -3000, "CC", 1500)),
                       new Vertex(3, Map.of("AA", 400, "BB", -400, "CC", 400)),
                       new Vertex(4, Map.of("AA", -200, "BB", 200, "CC", 200)),
                       new Vertex(5, Map.of("AA", -300, "BB", 300, "CC", 300)));
    }

    public static List<CnecRamBranchData> getTestCnecRam1() {
        final CnecRamBranchData cnec1 = new CnecRamBranchData("id1", 1450, 157, 15, true, false, Map.of("AA", BigDecimal.valueOf(.1), "BB", BigDecimal.valueOf(0.1), "CC", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec2 = new CnecRamBranchData("id2", 3450, 257, 16, false, true, Map.of("AA", BigDecimal.valueOf(.101), "BB", BigDecimal.valueOf(0.101), "CC", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec3 = new CnecRamBranchData("id3", 3450, 257, 14, false, true, Map.of("AA", BigDecimal.valueOf(.101), "BB", BigDecimal.valueOf(0.101), "CC", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec4 = new CnecRamBranchData("id4", 1450, 157, 360, true, false, Map.of("AA", BigDecimal.valueOf(.9), "BB", BigDecimal.valueOf(0.9), "CC", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec5 = new CnecRamBranchData("id5", 3450, 257, 365, false, true, Map.of("AA", BigDecimal.valueOf(.9), "BB", BigDecimal.valueOf(0.9), "CC", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec6 = new CnecRamBranchData("id6", 3450, 257, 360, false, true, Map.of("AA", BigDecimal.valueOf(.9), "BB", BigDecimal.valueOf(0.9), "CC", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec7 = new CnecRamBranchData("id7", 1450, 157, 360, false, false, Map.of("AA", BigDecimal.valueOf(.9), "BB", BigDecimal.valueOf(0.9), "CC", BigDecimal.valueOf(.9)));
        final CnecRamBranchData cnec8 = new CnecRamBranchData("id8", 3450, 257, 365, true, true, Map.of("AA", BigDecimal.valueOf(.9), "BB", BigDecimal.valueOf(0.9), "CC", BigDecimal.valueOf(.9)));

        return List.of(cnec1, cnec2, cnec3, cnec4, cnec5, cnec6, cnec7, cnec8);
    }

    public static List<CnecRamBranchData> getTestCnecRam2() {
        final CnecRamBranchData cnec1 = new CnecRamBranchData("id1", 1450, 157, 1000, true, false, Map.of("AA", BigDecimal.valueOf(.1), "BB", BigDecimal.valueOf(0.2), "CC", BigDecimal.valueOf(.1)));
        final CnecRamBranchData cnec2 = new CnecRamBranchData("id2", 3450, 257, 700, false, true, Map.of("AA", BigDecimal.valueOf(.101), "BB", BigDecimal.valueOf(0.201), "CC", BigDecimal.valueOf(.1)));

        return List.of(cnec1, cnec2);
    }

    public static class TestCoreHubConf extends CoreHubsConfiguration {
        public TestCoreHubConf() {
            // test class
        }

        @Override
        public List<CoreHub> getCoreHubs() {
            return List.of(
                    new CoreHub("Test1", "ram1", "fb1", "FR-CORE", "AA", false, false, 1, Country.FR),
                    new CoreHub("Test2", "ram2", "fb2", "DE-CORE", "BB", false, false, 1, Country.DE),
                    new CoreHub("Test3", "ram3", "fb3", "BE-CORE", "CC", false, false, 1, Country.BE)
            );
        }
    }

    public static List<SelectedVertexData> mapVerticesToSelectedVerticesData(List<Vertex> vertices) {
        return vertices.stream()
                .map(SelectedVertexData::new)
                .toList();
    }
}
