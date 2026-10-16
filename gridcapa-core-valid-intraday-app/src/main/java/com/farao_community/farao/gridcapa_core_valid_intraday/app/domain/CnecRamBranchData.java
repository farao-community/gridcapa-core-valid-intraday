/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.FlowBasedDomainBranchData;

import java.math.BigDecimal;
import java.util.Map;

public record CnecRamBranchData(String branchId, int ram0Core, int amr, int ram, boolean isLimiting, boolean isFrench, Map<String, BigDecimal> ptdfValues) implements FlowBasedDomainBranchData {
    @Override
    public int getRam0Core() {
        return ram0Core;
    }

    @Override
    public int getAmr() {
        return amr;
    }

    @Override
    public Map<String, BigDecimal> getPtdfValues() {
        return Map.copyOf(ptdfValues);
    }
}
