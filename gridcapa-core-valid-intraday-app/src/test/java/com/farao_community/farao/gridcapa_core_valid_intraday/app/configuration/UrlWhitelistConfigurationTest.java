/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
@SpringBootTest
class UrlWhitelistConfigurationTest {

    @Autowired
    private UrlWhitelistConfiguration urlWhitelistConfiguration;

    @Test
    void getWhitelist() {
        List<String> whitelist = urlWhitelistConfiguration.getWhitelist();
        Assertions.assertNotNull(whitelist);
        Assertions.assertEquals(2, whitelist.size());
        Assertions.assertTrue(whitelist.contains("file:/"));
        Assertions.assertTrue(whitelist.contains("http://minio:9000/"));
    }
}
