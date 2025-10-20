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

    public String getId() {
        return id;
    }

    public String getCurrentRunId() {
        return currentRunId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public CoreValidIdFileResource getCnecRam() {
        return cnecRam;
    }

    public CoreValidIdFileResource getVertice() {
        return vertice;
    }

    public CoreValidIdFileResource getCgm() {
        return cgm;
    }

    public CoreValidIdFileResource getGlsk() {
        return glsk;
    }

    public CoreValidIdFileResource getMergedCnec() {
        return mergedCnec;
    }

    public CoreValidIdFileResource getMarketPoint() {
        return marketPoint;
    }

    public CoreValidIdFileResource getPra() {
        return pra;
    }

    public boolean getLaunchedAutomatically() {
        return launchedAutomatically;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
