/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertice;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.exception.CoreValidCommonsInvalidDataException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class VerticeImporterTest {

    private final List<CoreHub> coreHubs = getTestCoreHubs();

    @Test
    void importVertices() throws IOException {
        InputStream inputStream = getClass().getResource("vertice.csv").openStream();
        final List<Vertice> vertices = VerticeImporter.importVertices(inputStream, coreHubs);
        Assertions.assertThat(vertices)
                .isNotNull()
                .hasSize(4);
        final Vertice vertice = vertices.getFirst();
        Assertions.assertThat(vertice.getVerticeId()).isEqualTo(1);
        final HashMap<String, Integer> entries = new HashMap<>();
        entries.put("AA", 11);
        entries.put("BB", 111);
        entries.put("CC", 1111);
        entries.put("D_D", 1);
        final Map<String, Integer> positions = vertice.getPositions();
        Assertions.assertThat(positions)
                .hasSize(4)
                .containsAllEntriesOf(entries);
        final Vertice vertice3 = vertices.get(3);
        Assertions.assertThat(vertice3.getVerticeId()).isEqualTo(4);
        final HashMap<String, Integer> entries3 = new HashMap<>();
        entries3.put("AA", 66);
        entries3.put("BB", 666);
        entries3.put("CC", 6666);
        entries3.put("D_D", 0);
        final Map<String, Integer> positions3 = vertice3.getPositions();
        Assertions.assertThat(positions3)
                .hasSize(4)
                .containsAllEntriesOf(entries3);

    }

    @Test
    void importVerticesWithException() {
        InputStream inputStream = Mockito.mock(InputStream.class);
        Assertions.assertThatExceptionOfType(CoreValidCommonsInvalidDataException.class)
                .isThrownBy(() -> VerticeImporter.importVertices(inputStream, coreHubs))
                .withMessage("Exception occurred during parsing vertice file");
    }

    @Test
    void importEmptyNonHvdcVerticesWithException() throws IOException {
        InputStream inputStream = getClass().getResource("bad-vertice.csv").openStream();
        Assertions.assertThatExceptionOfType(CoreValidCommonsInvalidDataException.class)
                .isThrownBy(() -> VerticeImporter.importVertices(inputStream, coreHubs))
                .withMessage("Exception occurred during parsing vertice file");
    }

    private List<CoreHub> getTestCoreHubs() {
        final List<CoreHub> hubs = new ArrayList<>();
        hubs.add(new CoreHub("Test1", "ram1", "fb1", "fc1", "AA", false, 1));
        hubs.add(new CoreHub("Test2", "ram2", "fb2", "fc2", "BB", false, 1));
        hubs.add(new CoreHub("Test3", "ram3", "fb3", "fc3", "CC", false, 1));
        hubs.add(new CoreHub("Test4", "ram4", "fb4", "fc4", "D_D", true, 1));
        return hubs;
    }
}
