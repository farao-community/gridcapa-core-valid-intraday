/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Amira Kahya {@literal <amira.kahya at rte-france.com>}
 */
@SpringBootTest
class FileImporterTest {

    @Autowired
    private FileImporter fileImporter;

    private static final OffsetDateTime TEST_DATE_TIME = OffsetDateTime.parse("2021-07-22T22:30Z");

    @Test
    void shouldImportReferenceProgramAndComputeGlobalNetPositions() {
        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", getClass().getResource("/20210723-FID2-632-v2-10V1001C--00264T-to-10V1001C--00085T.xml"));
        final ReferenceProgram referenceProgram = fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME);
        assertEquals(-50, referenceProgram.getGlobalNetPosition("10YFR-RTE------C"));
        assertEquals(-1325, referenceProgram.getGlobalNetPosition("10YCB-GERMANY--8"));
        assertEquals(225, referenceProgram.getGlobalNetPosition("10YNL----------L"));
        assertEquals(1150, referenceProgram.getGlobalNetPosition("10YBE----------2"));
    }

    private CoreValidIntradayFileResource createFileResource(final String filename, final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }
}
