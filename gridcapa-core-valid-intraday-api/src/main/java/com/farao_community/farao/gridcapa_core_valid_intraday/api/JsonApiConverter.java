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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.github.jasminb.jsonapi.models.errors.Error;

/**
 * JSON API conversion component
 * Allows automatic conversion from resources or exceptions towards JSON API formatted bytes.
 *
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
public class JsonApiConverter {
    private final ObjectMapper objectMapper;
    private final ResourceConverter converter;

    public JsonApiConverter() {
        this.objectMapper = createObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        this.converter = createConverter();
    }

    public <T> T fromJsonMessage(byte[] jsonMessage, Class<T> tClass) {
        return converter.readDocument(jsonMessage, tClass).get();
    }

    public <T> byte[] toJsonMessage(T jsonApiObject) {
        JSONAPIDocument<?> jsonapiDocument = new JSONAPIDocument<>(jsonApiObject);
        try {
            return converter.writeDocument(jsonapiDocument);
        } catch (DocumentSerializationException e) {
            throw new CoreValidIntradayInternalException("Exception occurred during object conversion", e);
        }
    }

    public byte[] toJsonMessage(AbstractCoreValidIntradayException exception) {
        JSONAPIDocument<?> jsonapiDocument = new JSONAPIDocument<>(convertExceptionToJsonError(exception));
        try {
            return converter.writeDocument(jsonapiDocument);
        } catch (DocumentSerializationException e) {
            throw new CoreValidIntradayInternalException("Exception occurred during exception message conversion", e);
        }
    }

    private ResourceConverter createConverter() {
        ResourceConverter resourceConverter = new ResourceConverter(objectMapper, CoreValidIntradayRequest.class);
        resourceConverter.disableSerializationOption(SerializationFeature.INCLUDE_META);
        return resourceConverter;
    }

    private Error convertExceptionToJsonError(AbstractCoreValidIntradayException exception) {
        Error error = new Error();
        error.setStatus(Integer.toString(exception.getStatus()));
        error.setCode(exception.getCode());
        error.setTitle(exception.getTitle());
        error.setDetail(exception.getDetails());
        return error;
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
