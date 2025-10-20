/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
    /**
     * Serializes an OffsetDateTime as its ISO-8601 string representation into the provided JSON output.
     *
     * @param offsetDateTime the OffsetDateTime to serialize
     * @param jsonGenerator the JsonGenerator to write the string value to
     * @param serializerProvider the active SerializerProvider (unused)
     * @throws IOException if writing to the JsonGenerator fails
     */
    @Override
    public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(offsetDateTime.toString());
    }
}