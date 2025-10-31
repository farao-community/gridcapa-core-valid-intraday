/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class VerticeTest {

    private Vertice getTestVertice(final int verticeId) {
        final HashMap<String, Integer> map = new HashMap<>();
        map.put("AA", 1234);
        map.put("BB", 2345);
        map.put("CC", 3456);
        return new Vertice(verticeId, map);

    }

    @Test
    void getVerticeIds() {
        final Vertice test1 = getTestVertice(1);
        Assertions.assertEquals(1, test1.getVerticeId());
        final Vertice test2 = getTestVertice(2);
        Assertions.assertEquals(2, test2.getVerticeId());
        final Vertice test200 = getTestVertice(200);
        Assertions.assertEquals(200, test200.getVerticeId());
    }

    @Test
    void getPositions() {
        final Vertice test = getTestVertice(5);
        Assertions.assertEquals(5, test.getVerticeId());
        Assertions.assertNotNull(test.getPositions());
        final Map<String, Integer> positions = test.getPositions();
        Assertions.assertEquals(3, positions.size());
        Assertions.assertEquals(1234, positions.get("AA"));
        Assertions.assertEquals(2345, positions.get("BB"));
        Assertions.assertEquals(3456, positions.get("CC"));

    }
}
