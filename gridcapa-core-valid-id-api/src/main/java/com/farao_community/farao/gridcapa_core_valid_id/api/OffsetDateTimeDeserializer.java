/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    /**
     * Deserializes the current JSON string value into an OffsetDateTime.
     *
     * @param jsonParser the parser positioned on a JSON string containing an ISO-8601 offset date-time
     * @param deserializationContext provider of contextual information for deserialization (not used)
     * @return the parsed OffsetDateTime
     * @throws IOException if an I/O error occurs while reading from the parser
     */
    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return OffsetDateTime.parse(jsonParser.getText());
    }
}