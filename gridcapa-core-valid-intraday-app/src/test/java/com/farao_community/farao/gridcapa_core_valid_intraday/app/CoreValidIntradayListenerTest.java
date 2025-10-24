/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.app;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CoreValidIntradayListenerTest {
    @MockitoBean
    public CoreValidIntradayHandler coreValidHandler;

    @Autowired
    public CoreValidIntradayListener coreValidIntradayListener;

    @MockitoBean
    private StreamBridge streamBridge;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @TestConfiguration
    static class ProcessPublicationServiceTestConfiguration {
        @Bean
        @Primary
        public AmqpTemplate amqpTemplate() {
            return Mockito.mock(AmqpTemplate.class);
        }
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(amqpTemplate, coreValidHandler, streamBridge);
    }

    @Test
    void checkThatCorrectMessageIsHandledCorrectly() throws URISyntaxException, IOException {
        byte[] correctMessage = Files.readAllBytes(Paths.get(getClass().getResource("/validRequest.json").toURI()));
        coreValidIntradayListener.onMessage(correctMessage);
        Mockito.verify(streamBridge, Mockito.times(2)).send(Mockito.anyString(), Mockito.any());
        Mockito.verify(coreValidHandler, Mockito.times(1)).handleCoreValidIntradayRequest(Mockito.any(CoreValidIntradayRequest.class));
    }

    @Test
    void checkThatInvalidMessageReturnsError() throws URISyntaxException, IOException {
        byte[] invalidMessage = Files.readAllBytes(Paths.get(getClass().getResource("/invalidRequest.json").toURI()));
        coreValidIntradayListener.onMessage(invalidMessage);
        Mockito.verify(streamBridge, Mockito.times(0)).send(Mockito.anyString(), Mockito.any());
        Mockito.verify(coreValidHandler, Mockito.times(0)).handleCoreValidIntradayRequest(Mockito.any(CoreValidIntradayRequest.class));
    }

}
