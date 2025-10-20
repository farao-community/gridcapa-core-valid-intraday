/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api;

import com.farao_community.farao.gridcapa_core_valid_id.api.exception.AbstractCoreValidIdException;
import com.farao_community.farao.gridcapa_core_valid_id.api.exception.CoreValidIdInternalException;
import com.farao_community.farao.gridcapa_core_valid_id.api.resource.CoreValidIdRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
class JsonApiConverterTest {
    @Test
    void checkCoreValidInputsJsonConversion() throws URISyntaxException, IOException {
        JsonApiConverter jsonApiConverter = new JsonApiConverter();
        String inputMessage = Files.readString(Paths.get(getClass().getResource("/validRequest.json").toURI()));
        CoreValidIdRequest coreValidIdRequest = jsonApiConverter.fromJsonMessage(inputMessage.getBytes(), CoreValidIdRequest.class);
        assertEquals("id", coreValidIdRequest.getId());
        assertEquals("cnecRam.txt", coreValidIdRequest.getCnecRam().getFilename());
        assertEquals("https://cnecRam/file/url", coreValidIdRequest.getCnecRam().getUrl());
        assertEquals("vertice.txt", coreValidIdRequest.getVertice().getFilename());
        assertEquals("https://vertice/file/url", coreValidIdRequest.getVertice().getUrl());
        assertEquals("cgm.txt", coreValidIdRequest.getCgm().getFilename());
        assertEquals("https://cgm/file/url", coreValidIdRequest.getCgm().getUrl());
        assertEquals("glsk.txt", coreValidIdRequest.getGlsk().getFilename());
        assertEquals("https://glsk/file/url", coreValidIdRequest.getGlsk().getUrl());
        assertEquals("mergedCnec.txt", coreValidIdRequest.getMergedCnec().getFilename());
        assertEquals("https://mergedCnec/file/url", coreValidIdRequest.getMergedCnec().getUrl());
        assertEquals("marketPoint.txt", coreValidIdRequest.getMarketPoint().getFilename());
        assertEquals("https://marketPoint/file/url", coreValidIdRequest.getMarketPoint().getUrl());
        assertEquals("pra.txt", coreValidIdRequest.getPra().getFilename());
        assertEquals("https://pra/file/url", coreValidIdRequest.getPra().getUrl());
    }

    @Test
    void checkInternalExceptionJsonConversion() throws URISyntaxException, IOException {
        JsonApiConverter jsonApiConverter = new JsonApiConverter();
        AbstractCoreValidIdException exception = new CoreValidIdInternalException("Something really bad happened");
        String expectedMessage = Files.readString(Paths.get(getClass().getResource("/coreValidIdInternalError.json").toURI()));
        assertEquals(expectedMessage, new String(jsonApiConverter.toJsonMessage(exception)));
    }

}
