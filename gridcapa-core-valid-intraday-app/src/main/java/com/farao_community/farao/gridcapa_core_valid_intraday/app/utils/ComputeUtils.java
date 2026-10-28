/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;

import java.math.BigDecimal;
import java.util.List;

public final class ComputeUtils {

    private ComputeUtils() {
        // utility class
    }

    public static BigDecimal computeCnecVertexProduct(final Vertex vertex,
                                                      final CnecRamBranchData cnec,
                                                      final List<CoreHub> coreHubs) {
        return coreHubs.stream()
                .map(CoreHub::clusterVerticeCode)
                .map(code -> multiplyVertexAndCnecCoordinates(vertex, cnec, code))
                .reduce(BigDecimal::add)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Vertex %d and Cnec %S missing required coordinates for product computation",
                                      vertex.vertexId(), cnec.branchId())
                ));
    }

    private static BigDecimal multiplyVertexAndCnecCoordinates(final Vertex vertex,
                                                               final CnecRamBranchData cnec,
                                                               final String code) {
        return cnec.getPtdfValues().get(code).multiply(BigDecimal.valueOf(vertex.coordinates().get(code)));
    }
}
