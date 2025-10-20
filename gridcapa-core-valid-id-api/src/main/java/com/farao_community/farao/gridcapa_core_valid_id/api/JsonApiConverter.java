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
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public class JsonApiConverter {
    private final ObjectMapper objectMapper;

    /**
     * Creates a JsonApiConverter and initializes its ObjectMapper with JDK 8 and Java Time support.
     *
     * <p>The constructor prepares the mapper used for JSON API (de)serialization by registering
     * modules that handle Optional and Java Time types.
     */
    public JsonApiConverter() {
        this.objectMapper = createObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Convert a JSON:API-formatted byte array into an instance of the specified type.
     *
     * @param jsonMessage the JSON:API document as a byte array
     * @param tClass the target class to deserialize into
     * @param <T> the type of the returned object
     * @return an instance of {@code tClass} populated from the JSON:API document
     */
    public <T> T fromJsonMessage(byte[] jsonMessage, Class<T> tClass) {
        ResourceConverter converter = createConverter();
        return converter.readDocument(jsonMessage, tClass).get();
    }

    /**
     * Serialize a JSON API resource or error object into a JSON API–formatted byte array.
     *
     * @param jsonApiObject the resource or error object to convert to a JSON API document
     * @return the resulting JSON API document as a byte array
     * @throws CoreValidIdInternalException if the document cannot be serialized
     */
    public <T> byte[] toJsonMessage(T jsonApiObject) {
        ResourceConverter converter = createConverter();
        JSONAPIDocument<?> jsonapiDocument = new JSONAPIDocument<>(jsonApiObject);
        try {
            return converter.writeDocument(jsonapiDocument);
        } catch (DocumentSerializationException e) {
            throw new CoreValidIdInternalException("Exception occurred during object conversion", e);
        }
    }

    /**
     * Converts the given AbstractCoreValidIdException into a JSON:API error document and returns it as bytes.
     *
     * @param exception the exception to convert into a JSON:API Error document
     * @return a JSON:API-compliant byte array representing the error document for the provided exception
     * @throws CoreValidIdInternalException if serialization of the JSON:API document fails
     */
    public byte[] toJsonMessage(AbstractCoreValidIdException exception) {
        ResourceConverter converter = createConverter();
        JSONAPIDocument<?> jsonapiDocument = new JSONAPIDocument<>(convertExceptionToJsonError(exception));
        try {
            return converter.writeDocument(jsonapiDocument);
        } catch (DocumentSerializationException e) {
            throw new CoreValidIdInternalException("Exception occurred during exception message conversion", e);
        }
    }

    /**
     * Creates and configures a ResourceConverter for JSON API operations.
     *
     * @return a ResourceConverter initialized for CoreValidIdRequest with meta inclusion disabled
     */
    private ResourceConverter createConverter() {
        ResourceConverter converter = new ResourceConverter(objectMapper, CoreValidIdRequest.class);
        converter.disableSerializationOption(SerializationFeature.INCLUDE_META);
        return converter;
    }

    /**
     * Converts an AbstractCoreValidIdException into a JSON API Error object.
     *
     * @param exception the exception whose status, code, title, and details will be mapped to the Error
     * @return an Error populated with the exception's status (as string), code, title, and detail
     */
    private Error convertExceptionToJsonError(AbstractCoreValidIdException exception) {
        Error error = new Error();
        error.setStatus(Integer.toString(exception.getStatus()));
        error.setCode(exception.getCode());
        error.setTitle(exception.getTitle());
        error.setDetail(exception.getDetails());
        return error;
    }

    /**
     * Create a new Jackson ObjectMapper instance.
     *
     * @return a new ObjectMapper for JSON serialization and deserialization
     */
    private ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}