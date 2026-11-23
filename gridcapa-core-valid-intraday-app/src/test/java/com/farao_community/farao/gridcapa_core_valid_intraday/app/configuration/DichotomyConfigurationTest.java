/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DichotomyConfigurationTest {

    private static final int MAX_ITERATIONS = 4;
    private static final double MAX_VALUE = 1.0;
    private static final double MIN_VALUE = 0.0;
    private static final double PRECISION = 0.125;

    @Autowired
    private DichotomyConfiguration dichotomyConfiguration;

    @Test
    void getDichotomyConfigurationTest() {
        Assertions.assertThat(dichotomyConfiguration.getMaxIterations()).isEqualTo(MAX_ITERATIONS);
        Assertions.assertThat(dichotomyConfiguration.getMaxValue()).isEqualTo(MAX_VALUE);
        Assertions.assertThat(dichotomyConfiguration.getMinValue()).isEqualTo(MIN_VALUE);
        Assertions.assertThat(dichotomyConfiguration.getPrecision()).isEqualTo(PRECISION);
    }
}
