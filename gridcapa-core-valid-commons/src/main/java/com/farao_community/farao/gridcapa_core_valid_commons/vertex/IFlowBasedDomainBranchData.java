package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import java.math.BigDecimal;
import java.util.Map;

public interface IFlowBasedDomainBranchData {
    int getRam0Core();

    int getAmr();

    Map<String, BigDecimal> getPtdfValues();
}
