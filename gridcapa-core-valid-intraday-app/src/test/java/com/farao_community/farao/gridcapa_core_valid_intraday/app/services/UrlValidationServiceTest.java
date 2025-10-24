package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UrlValidationServiceTest {
    @Autowired
    private UrlValidationService urlValidationService;

    @Test
    void checkExceptionThrownWhenUrlIsNotPartOfWhitelistedUrls() {
        Exception exception = assertThrows(CoreValidIntradayInvalidDataException.class, () -> urlValidationService.openUrlStream("url1"));
        String expectedMessage = "is not part of application's whitelisted url's";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}