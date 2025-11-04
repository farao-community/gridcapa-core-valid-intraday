/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.exception.CoreValidCommonsInvalidDataException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

class VerticesImporterTest {

    private final List<CoreHub> coreHubs = getTestCoreHubs();

    @Test
    void importVertices() throws IOException {
        try (final InputStream inputStream = getClass().getResource("vertices.csv").openStream();) {
            final List<Vertex> vertices = VerticesImporter.importVertices(inputStream, coreHubs);
            Assertions.assertThat(vertices)
                    .isNotNull()
                    .hasSize(4)
                    .first()
                    .hasFieldOrPropertyWithValue("vertexId", 1);
            final Map<String, Integer> entries = Map.of(
                    "AA", 11,
                    "BB", 111,
                    "CC", 1111,
                    "D_D", 1
            );
            final Map<String, Integer> coordinates = vertices.getFirst().coordinates();
            Assertions.assertThat(coordinates)
                    .hasSize(4)
                    .containsAllEntriesOf(entries);
            final Vertex vertex3 = vertices.get(3);
            Assertions.assertThat(vertex3.vertexId()).isEqualTo(4);
            final Map<String, Integer> entries3 = Map.of(
                    "AA", 66,
                    "BB", 666,
                    "CC", 6666,
                    "D_D", 0
            );
            final Map<String, Integer> coordinates3 = vertex3.coordinates();
            Assertions.assertThat(coordinates3)
                    .hasSize(4)
                    .containsAllEntriesOf(entries3);
        }
    }

    @Test
    void importVerticesWithException() throws IOException {
        try (final InputStream inputStream = Mockito.mock(InputStream.class);) {
            Assertions.assertThatExceptionOfType(CoreValidCommonsInvalidDataException.class)
                    .isThrownBy(() -> VerticesImporter.importVertices(inputStream, coreHubs))
                    .withMessage("Exception occurred during parsing vertices file");
        }
    }

    @Test
    void importEmptyNonHvdcVerticesWithException() throws IOException {
        try (final InputStream inputStream = getClass().getResource("bad-vertices.csv").openStream();) {
            Assertions.assertThatExceptionOfType(CoreValidCommonsInvalidDataException.class)
                    .isThrownBy(() -> VerticesImporter.importVertices(inputStream, coreHubs))
                    .withMessage("Exception occurred during parsing vertices file");
        }
    }

    private List<CoreHub> getTestCoreHubs() {
        return List.of(
            new CoreHub("Test1", "ram1", "fb1", "fc1", "AA", false, 1),
            new CoreHub("Test2", "ram2", "fb2", "fc2", "BB", false, 1),
            new CoreHub("Test3", "ram3", "fb3", "fc3", "CC", false, 1),
            new CoreHub("Test4", "ram4", "fb4", "fc4", "D_D", true, 1)
        );
    }
}
