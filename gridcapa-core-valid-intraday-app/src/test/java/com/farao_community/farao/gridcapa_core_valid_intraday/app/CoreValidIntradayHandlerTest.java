/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URL;


/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@SpringBootTest
class CoreValidIntradayHandlerTest {

    @Autowired
    private CoreValidIntradayHandler coreValidHandler;

    @MockitoBean
    private MinioAdapter minioAdapter;

    @MockitoBean
    private FileImporter fileImporter;

    @Test
    void handleCoreValidRequestTest() {
        //TODO Assertions.fail("to be implemented");
    }

    private CoreValidIntradayFileResource createFileResource(String filename, URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }

}
