/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
class CoreValidIdRequestTest {

    private CoreValidIdFileResource cnecRam;
    private CoreValidIdFileResource vertice;
    private CoreValidIdFileResource cgm;
    private CoreValidIdFileResource glsk;
    private CoreValidIdFileResource mergedCnec;
    private CoreValidIdFileResource marketPoint;
    private CoreValidIdFileResource pra;
    private OffsetDateTime dateTime;

    @BeforeEach
    void setUp() {
        cnecRam = new CoreValidIdFileResource("cnecRam.txt", "http://path/to/cnecRam/file");
        vertice = new CoreValidIdFileResource("vertice.txt", "http://path/to/vertice/file");
        cgm = new CoreValidIdFileResource("network.txt", "http://path/to/cgm/file");
        glsk = new CoreValidIdFileResource("glsk.txt", "http://path/to/glsk/file");
        mergedCnec = new CoreValidIdFileResource("mergedCnec.txt", "http://path/to/mergedCnec/file");
        marketPoint = new CoreValidIdFileResource("marketPoint.txt", "http://path/to/marketPoint/file");
        pra = new CoreValidIdFileResource("pra.txt", "http://path/to/pra/file");
        dateTime = OffsetDateTime.parse("2025-10-01T00:30Z");
    }

    @Test
    void checkManualCoreValidRequest() {
        CoreValidIdRequest coreValidIdRequest = new CoreValidIdRequest("id", "runId", dateTime, cnecRam, vertice, cgm, glsk, mergedCnec, marketPoint, pra);
        assertNotNull(coreValidIdRequest);
        assertEquals("id", coreValidIdRequest.getId());
        assertEquals("runId", coreValidIdRequest.getCurrentRunId());
        assertEquals("2025-10-01T00:30Z", coreValidIdRequest.getTimestamp().toString());
        assertEquals("cnecRam.txt", coreValidIdRequest.getCnecRam().getFilename());
        assertEquals("vertice.txt", coreValidIdRequest.getVertice().getFilename());
        assertEquals("network.txt", coreValidIdRequest.getCgm().getFilename());
        assertEquals("glsk.txt", coreValidIdRequest.getGlsk().getFilename());
        assertEquals("mergedCnec.txt", coreValidIdRequest.getMergedCnec().getFilename());
        assertEquals("marketPoint.txt", coreValidIdRequest.getMarketPoint().getFilename());
        assertEquals("pra.txt", coreValidIdRequest.getPra().getFilename());
        assertEquals("http://path/to/mergedCnec/file", coreValidIdRequest.getMergedCnec().getUrl());
        assertFalse(coreValidIdRequest.getLaunchedAutomatically());
    }

    @Test
    void checkAutoCoreValidRequest() {
        CoreValidIdRequest coreValidIdRequest = new CoreValidIdRequest("id", "runId", dateTime, cnecRam, vertice, cgm, glsk, mergedCnec, marketPoint, pra, true);
        assertTrue(coreValidIdRequest.getLaunchedAutomatically());
    }

}
