/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.starter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 * This config class allows the scanning of the package by Spring Boot, hence declaring CoreValidIntradayClient as a bean
 */
@Configuration
@EnableConfigurationProperties(CoreValidIntradayClientProperties.class)
@ComponentScan
public class CoreValidIntradayClientAutoConfiguration {
}
