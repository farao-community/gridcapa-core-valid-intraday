/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
@SpringBootTest
class UrlValidationServiceTest {

    @Autowired
    private UrlValidationService urlValidationService;

    @Test
    void checkExceptionThrownWhenUrlIsNotPartOfWhitelistedUrls() {
        final Exception exception = Assertions.assertThrows(CoreValidIntradayInvalidDataException.class, () -> urlValidationService.openUrlStream("url1"));
        final String expectedMessage = "is not part of application's whitelisted url's";
        final String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void checkExceptionThrownWhenUrlIsNull() {
        final String expectedMessage = "URL cannot be null or blank";
        final Exception exception = Assertions.assertThrows(CoreValidIntradayInvalidDataException.class, () -> urlValidationService.openUrlStream(null));
        final String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void checkExceptionThrownWhenUrlIsBlank() {
        final String expectedMessage = "URL cannot be null or blank";
        final Exception exception = Assertions.assertThrows(CoreValidIntradayInvalidDataException.class, () -> urlValidationService.openUrlStream("  "));
        final String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void checkWhenUrlIsOk() {
        try (var stream = urlValidationService.openUrlStream("file:/")) {
            Assertions.assertNotNull(stream);
        } catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void checkExceptionThrownWhenUrlNOK() {
        final String expectedMessage = "Cannot download FileResource file from URL";
        final Exception exception = Assertions.assertThrows(CoreValidIntradayInvalidDataException.class, () -> urlValidationService.openUrlStream("file:/___DOESNT_EXIST"));
        final String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}
