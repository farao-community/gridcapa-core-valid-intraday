/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa.task_manager.api.TaskParameterDto;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.Season;

import java.net.URL;
import java.util.List;
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
                new TaskParameterDto("RAM_MARGIN", "INT", "15", "213")
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
}
