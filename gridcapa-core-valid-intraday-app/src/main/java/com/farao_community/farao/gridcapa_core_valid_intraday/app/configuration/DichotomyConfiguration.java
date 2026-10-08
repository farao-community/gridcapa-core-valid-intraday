/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("core-valid-intraday-runner.dichotomy")
public class DichotomyConfiguration {

    private int maxSelectedFrenchCnec;

    public int getMaxSelectedFrenchCnec() {
        return maxSelectedFrenchCnec;
    }

    public void setMaxSelectedFrenchCnec(final int maxSelectedFrenchCnec) {
        this.maxSelectedFrenchCnec = maxSelectedFrenchCnec;
    }
}
