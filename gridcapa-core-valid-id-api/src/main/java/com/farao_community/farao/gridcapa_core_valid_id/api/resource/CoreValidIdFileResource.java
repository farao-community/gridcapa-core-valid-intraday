/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public class CoreValidIdFileResource {
    private final String filename;
    private final String url;

    /**
     * Create a CoreValidIdFileResource with the given filename and URL.
     *
     * @param filename the non-null filename of the file resource
     * @param url      the non-null URL of the file resource
     * @throws NullPointerException if {@code filename} or {@code url} is null
     */
    @JsonCreator
    public CoreValidIdFileResource(final @JsonProperty("filename") String filename,
                                   final @JsonProperty("url") String url) {
        this.filename = Objects.requireNonNull(filename);
        this.url = Objects.requireNonNull(url);
    }

    /**
     * The filename associated with this file resource.
     *
     * @return the filename of the resource
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the URL of the file resource.
     *
     * @return the file resource URL; never null
     */
    public String getUrl() {
        return url;
    }

    /**
     * Produce a string representation that includes this resource's field names and values.
     *
     * @return a string containing the field names and their values
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}