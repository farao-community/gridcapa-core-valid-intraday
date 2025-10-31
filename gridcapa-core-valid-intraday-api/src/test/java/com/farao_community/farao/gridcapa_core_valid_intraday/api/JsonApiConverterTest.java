/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.api;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.AbstractCoreValidIntradayException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInternalException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
class JsonApiConverterTest {
    @Test
    void checkCoreValidInputsJsonConversion() throws URISyntaxException, IOException {
        JsonApiConverter jsonApiConverter = new JsonApiConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/validRequest.json").toURI()));
        CoreValidIntradayRequest coreValidIntradayRequest = jsonApiConverter.fromJsonMessage(inputMessage.getBytes(), CoreValidIntradayRequest.class);
        assertEquals("id", coreValidIntradayRequest.getId());
        assertEquals("cnecRam.txt", coreValidIntradayRequest.getCnecRam().getFilename());
        assertEquals("https://cnecRam/file/url", coreValidIntradayRequest.getCnecRam().getUrl());
        assertEquals("vertices.txt", coreValidIntradayRequest.getVertices().getFilename());
        assertEquals("https://vertice/file/url", coreValidIntradayRequest.getVertices().getUrl());
        assertEquals("cgm.txt", coreValidIntradayRequest.getCgm().getFilename());
        assertEquals("https://cgm/file/url", coreValidIntradayRequest.getCgm().getUrl());
        assertEquals("glsk.txt", coreValidIntradayRequest.getGlsk().getFilename());
        assertEquals("https://glsk/file/url", coreValidIntradayRequest.getGlsk().getUrl());
        assertEquals("mergedCnec.txt", coreValidIntradayRequest.getMergedCnec().getFilename());
        assertEquals("https://mergedCnec/file/url", coreValidIntradayRequest.getMergedCnec().getUrl());
        assertEquals("marketPoint.txt", coreValidIntradayRequest.getMarketPoint().getFilename());
        assertEquals("https://marketPoint/file/url", coreValidIntradayRequest.getMarketPoint().getUrl());
        assertEquals("pra.txt", coreValidIntradayRequest.getPra().getFilename());
        assertEquals("https://pra/file/url", coreValidIntradayRequest.getPra().getUrl());
    }

    @Test
    void checkInternalExceptionJsonConversion() throws URISyntaxException, IOException {
        JsonApiConverter jsonApiConverter = new JsonApiConverter();
        AbstractCoreValidIntradayException exception = new CoreValidIntradayInternalException("Something really bad happened");
        String expectedMessage = Files.readString(Paths.get(getClass().getResource("/coreValidIntradayInternalError.json").toURI()));
        assertEquals(expectedMessage, new String(jsonApiConverter.toJsonMessage(exception)));
    }

}
