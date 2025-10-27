/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.starter;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.JsonApiConverter;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;

import java.io.IOException;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
class CoreValidIntradayClientTest {
    private final JsonApiConverter jsonApiConverter = new JsonApiConverter();

    @Test
    void checkThatClientHandleMessageCorrectly() throws IOException {
        AmqpTemplate amqpTemplate = Mockito.mock(AmqpTemplate.class);
        CoreValidIntradayClient client = new CoreValidIntradayClient(amqpTemplate, buildProperties());
        CoreValidIntradayRequest request = jsonApiConverter.fromJsonMessage(getClass().getResourceAsStream("/coreValidIntradayRequest.json").readAllBytes(), CoreValidIntradayRequest.class);

        Mockito.doNothing().when(amqpTemplate).send(Mockito.same("my-queue"), Mockito.any());
        Assertions.assertDoesNotThrow(() -> client.run(request));
    }

    private CoreValidIntradayClientProperties buildProperties() {
        return new CoreValidIntradayClientProperties(
                new CoreValidIntradayClientProperties.BindingConfiguration("my-queue",
                                                                           null,
                                                                           "60000",
                                                                           "application-id"));
    }
}
