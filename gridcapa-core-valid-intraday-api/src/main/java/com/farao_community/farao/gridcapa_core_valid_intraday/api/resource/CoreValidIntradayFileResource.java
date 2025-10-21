/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.api.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
public class CoreValidIntradayFileResource {
    private final String filename;
    private final String url;

    @JsonCreator
    public CoreValidIntradayFileResource(final @JsonProperty("filename") String filename,
                                         final @JsonProperty("url") String url) {
        this.filename = Objects.requireNonNull(filename);
        this.url = Objects.requireNonNull(url);
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
