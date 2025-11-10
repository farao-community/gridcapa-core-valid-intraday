package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import java.math.BigDecimal;
import java.util.Map;

public interface IFlowBasedDomainBranchData {
    Integer getRam0Core();

    Integer getAmr();

    Map<String, BigDecimal> getPtdfValues();
}
