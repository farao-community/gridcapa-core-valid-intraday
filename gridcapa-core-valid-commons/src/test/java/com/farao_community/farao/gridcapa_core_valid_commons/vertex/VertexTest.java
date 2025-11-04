/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class VertexTest {

    public static final Map<String, Integer> COORDINATES = Map.of(
            "AA", 1234,
            "BB", 2345,
            "CC", 3456
    );

    private Vertex getTestVertex(final int verticeId) {
        return new Vertex(verticeId, COORDINATES);
    }

    @Test
    void getVertexIds() {
        final Vertex test1 = getTestVertex(1);
        Assertions.assertThat(test1.vertexId()).isEqualTo(1);
        final Vertex test2 = getTestVertex(2);
        Assertions.assertThat(test2.vertexId()).isEqualTo(2);
        final Vertex test200 = getTestVertex(200);
        Assertions.assertThat(test200.vertexId()).isEqualTo(200);
    }

    @Test
    void getCoordinates() {
        final Vertex test = getTestVertex(5);
        Assertions.assertThat(test.vertexId()).isEqualTo(5);
        Assertions.assertThat(test.coordinates())
                .hasSize(3)
                .containsExactlyInAnyOrderEntriesOf(COORDINATES);
    }
}
