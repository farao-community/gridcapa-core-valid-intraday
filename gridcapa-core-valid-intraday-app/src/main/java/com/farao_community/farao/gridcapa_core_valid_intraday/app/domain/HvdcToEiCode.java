package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.powsybl.openrao.commons.EICode;

public enum HvdcToEiCode {

    BE_AL("22Y201903144---9"),
    DE_AL("22Y201903145---4");

    private final String eiCode;

    HvdcToEiCode(String value) {
        eiCode = value;
    }

    public EICode getEICode() {
        return new EICode(eiCode);
    }
}
