/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_commons.vertice;

import java.util.Map;

public class Vertice {

    private final int verticeId;
    private final Map<String, Integer> positions;

    public Vertice(final int verticeId, final Map<String, Integer> positions) {
        this.verticeId = verticeId;
        this.positions = positions;
    }

    public int getVerticeId() {
        return verticeId;
    }

    public Map<String, Integer> getPositions() {
        return positions;
    }
}
