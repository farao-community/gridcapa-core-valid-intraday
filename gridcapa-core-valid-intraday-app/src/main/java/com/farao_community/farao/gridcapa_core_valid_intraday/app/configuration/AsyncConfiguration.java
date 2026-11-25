/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration;

import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy.CoreValidForkJoinWorkerThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Marc Schwitzguébel {@literal <marc.schwitzguebel at rte-france.com>}
 */

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor asyncExecutor() {
        final CoreValidForkJoinWorkerThreadFactory factory = new CoreValidForkJoinWorkerThreadFactory();
        return new ForkJoinPool(Math.min(32767, Runtime.getRuntime().availableProcessors()), factory, null, false);
    }

}
