/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api.resource;

import com.farao_community.farao.gridcapa_core_valid_id.api.OffsetDateTimeDeserializer;
import com.farao_community.farao.gridcapa_core_valid_id.api.OffsetDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
@Type("core-valid-id-request")
public class CoreValidIdRequest {
    @Id
    private final String id;
    private final String currentRunId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private final OffsetDateTime timestamp;
    private final CoreValidIdFileResource cnecRam;
    private final CoreValidIdFileResource vertice;
    private final CoreValidIdFileResource cgm;
    private final CoreValidIdFileResource glsk;
    private final CoreValidIdFileResource mergedCnec;
    private final CoreValidIdFileResource marketPoint;
    private final CoreValidIdFileResource pra;
    private final boolean launchedAutomatically;

    /**
     * Constructs a CoreValidIdRequest with the given identifiers, timestamp, associated file resources, and launch flag.
     *
     * @param id unique request identifier
     * @param currentRunId identifier of the current processing run
     * @param timestamp ISO-8601 timestamp for the request
     * @param cnecRam CNEC RAM file resource
     * @param vertice Vertice file resource
     * @param cgm CGM file resource
     * @param glsk GLSK file resource
     * @param mergedCnec merged CNEC file resource
     * @param marketPoint MarketPoint file resource
     * @param pra PRA file resource
     * @param launchedAutomatically true if the request was launched automatically, false otherwise
     */
    @JsonCreator
    public CoreValidIdRequest(final @JsonProperty("id") String id,
                              final @JsonProperty("currentRunId") String currentRunId,
                              final @JsonProperty("timestamp") OffsetDateTime timestamp,
                              final @JsonProperty("cnecRam") CoreValidIdFileResource cnecRam,
                              final @JsonProperty("vertice") CoreValidIdFileResource vertice,
                              final @JsonProperty("cgm") CoreValidIdFileResource cgm,
                              final @JsonProperty("glsk") CoreValidIdFileResource glsk,
                              final @JsonProperty("mergedCnec") CoreValidIdFileResource mergedCnec,
                              final @JsonProperty("marketPoint") CoreValidIdFileResource marketPoint,
                              final @JsonProperty("pra") CoreValidIdFileResource pra,
                              final @JsonProperty("launchedAutomatically") boolean launchedAutomatically) {
        this.id = id;
        this.currentRunId = currentRunId;
        this.timestamp = timestamp;
        this.cnecRam = cnecRam;
        this.vertice = vertice;
        this.cgm = cgm;
        this.glsk = glsk;
        this.mergedCnec = mergedCnec;
        this.marketPoint = marketPoint;
        this.pra = pra;
        this.launchedAutomatically = launchedAutomatically;
    }

    /**
     * Creates a CoreValidIdRequest with the given identifiers, timestamp and file resources, and marks it as not launched automatically.
     *
     * @param id the request identifier
     * @param currentRunId the current run identifier associated with this request
     * @param timestamp the request timestamp in ISO-8601 offset date-time
     * @param cnecRam the CNEC RAM file resource, may be null
     * @param vertice the Vertice file resource, may be null
     * @param cgm the CGM file resource, may be null
     * @param glsk the GLSK file resource, may be null
     * @param mergedCnec the merged CNEC file resource, may be null
     * @param marketPoint the market point file resource, may be null
     * @param pra the PRA file resource, may be null
     */
    public CoreValidIdRequest(final String id,
                              final String currentRunId,
                              final OffsetDateTime timestamp,
                              final CoreValidIdFileResource cnecRam,
                              final CoreValidIdFileResource vertice,
                              final CoreValidIdFileResource cgm,
                              final CoreValidIdFileResource glsk,
                              final CoreValidIdFileResource mergedCnec,
                              final CoreValidIdFileResource marketPoint,
                              final CoreValidIdFileResource pra) {
        this(id, currentRunId, timestamp, cnecRam, vertice, cgm, glsk, mergedCnec, marketPoint, pra, false);
    }

    /**
     * Fetches the resource identifier.
     *
     * @return the resource identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current run identifier for this request.
     *
     * @return the current run identifier
     */
    public String getCurrentRunId() {
        return currentRunId;
    }

    /**
     * Request timestamp including its timezone offset.
     *
     * @return the request timestamp as an OffsetDateTime
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The CNEC-RAM file resource associated with this request.
     *
     * @return the CNEC-RAM CoreValidIdFileResource, or null if not present
     */
    public CoreValidIdFileResource getCnecRam() {
        return cnecRam;
    }

    /**
     * Gets the vertice file resource associated with this request.
     *
     * @return the vertice CoreValidIdFileResource, or null if none was provided
     */
    public CoreValidIdFileResource getVertice() {
        return vertice;
    }

    /**
     * The CGM file resource associated with this request.
     *
     * @return the CGM CoreValidIdFileResource instance, or {@code null} if not provided
     */
    public CoreValidIdFileResource getCgm() {
        return cgm;
    }

    /**
     * Gets the GLSK file resource associated with this request.
     *
     * @return the GLSK {@code CoreValidIdFileResource}, or {@code null} if not provided
     */
    public CoreValidIdFileResource getGlsk() {
        return glsk;
    }

    /**
     * Returns the merged CNEC file resource associated with this request.
     *
     * @return the merged CNEC CoreValidIdFileResource, or {@code null} if not present
     */
    public CoreValidIdFileResource getMergedCnec() {
        return mergedCnec;
    }

    /**
     * Returns the market point file resource associated with this request.
     *
     * @return the market point {@code CoreValidIdFileResource}
     */
    public CoreValidIdFileResource getMarketPoint() {
        return marketPoint;
    }

    /**
     * The PRA file resource associated with this request.
     *
     * @return the PRA file resource, or {@code null} if not present
     */
    public CoreValidIdFileResource getPra() {
        return pra;
    }

    /**
     * Indicates whether this request was launched automatically.
     *
     * @return `true` if the request was launched automatically, `false` otherwise.
     */
    public boolean getLaunchedAutomatically() {
        return launchedAutomatically;
    }

    /**
     * Provide a string representation of this object.
     *
     * @return a string containing the object's field names and values
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}