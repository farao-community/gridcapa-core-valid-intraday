/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.starter;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.JsonApiConverter;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@Component
public class CoreValidIntradayClient {
    private static final int DEFAULT_PRIORITY = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidIntradayClient.class);
    private static final String CONTENT_ENCODING = "UTF-8";
    private static final String CONTENT_TYPE = "application/vnd.api+json";

    private final AmqpTemplate amqpTemplate;
    private final CoreValidIntradayClientProperties coreValidIntradayClientProperties;
    private final JsonApiConverter jsonConverter;

    public CoreValidIntradayClient(final AmqpTemplate amqpTemplate, final CoreValidIntradayClientProperties coreValidIntradayClientProperties) {
        this.amqpTemplate = amqpTemplate;
        this.coreValidIntradayClientProperties = coreValidIntradayClientProperties;
        this.jsonConverter = new JsonApiConverter();
    }

    public void run(final CoreValidIntradayRequest coreValidIntradayRequest,
                    final int priority) {
        LOGGER.info("Core valid request sent: {}", coreValidIntradayRequest);
        amqpTemplate.send(coreValidIntradayClientProperties.binding().destination(),
                          coreValidIntradayClientProperties.binding().routingKey(),
                          buildMessage(coreValidIntradayRequest, priority));
    }

    public void run(final CoreValidIntradayRequest coreValidIntradayRequest) {
        run(coreValidIntradayRequest, DEFAULT_PRIORITY);
    }

    public Message buildMessage(final CoreValidIntradayRequest coreValidIntradayRequest, final int priority) {
        return MessageBuilder.withBody(jsonConverter.toJsonMessage(coreValidIntradayRequest))
                .andProperties(buildMessageProperties(priority))
                .build();
    }

    private MessageProperties buildMessageProperties(final int priority) {
        return MessagePropertiesBuilder.newInstance()
                .setAppId(coreValidIntradayClientProperties.binding().applicationId())
                .setContentEncoding(CONTENT_ENCODING)
                .setContentType(CONTENT_TYPE)
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .setExpiration(coreValidIntradayClientProperties.binding().expiration())
                .setPriority(priority)
                .build();
    }
}
