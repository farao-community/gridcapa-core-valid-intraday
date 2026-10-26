/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

class SelectedVertexDataTest {

    @Test
    void createVertexDataTest() {
        Vertex vertex = new Vertex(123, Map.of("AA", 1000, "BB", 2000, "CC", 3300));
        SelectedVertexData data = new SelectedVertexData(vertex);
        final double normDistance = 0.123;
        data.setDistanceNorm(normDistance);
        final BigDecimal normRam = BigDecimal.valueOf(0.444);
        data.setRamNorm(normRam);
        final double normDirection = 0.1245;
        data.setDirectionNorm(normDirection);
        final double score = 0.555;
        data.setScore(score);

        Assertions.assertThat(data.getVertex()).isEqualTo(vertex);
        Assertions.assertThat(data.getScore()).isEqualTo(score);
        Assertions.assertThat(data.getDistanceNorm()).isEqualTo(normDistance);
        Assertions.assertThat(data.getDirectionNorm()).isEqualTo(normDirection);
        Assertions.assertThat(data.getRamNorm()).isEqualByComparingTo(normRam);
    }
}
