/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;

import java.math.BigDecimal;

public class SelectedVertexData {

    final private Vertex vertex;
    private Double score;
    private BigDecimal normRam;
    private Double normDistance;
    private Double normDirection;

    public SelectedVertexData(Vertex vertex) {
        this.vertex = vertex;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(final Double score) {
        this.score = score;
    }

    public BigDecimal getNormRam() {
        return normRam;
    }

    public void setNormRam(final BigDecimal normRam) {
        this.normRam = normRam;
    }

    public Double getNormDistance() {
        return normDistance;
    }

    public void setNormDistance(final Double normDistance) {
        this.normDistance = normDistance;
    }

    public Double getNormDirection() {
        return normDirection;
    }

    public void setNormDirection(final Double normDirection) {
        this.normDirection = normDirection;
    }

    @Override
    public String toString() {
        StringBuilder appender = new StringBuilder();
        vertex.coordinates().forEach((hub, val) -> appender.append(hub).append(": ").append(val).append(", "));
        return "Id: " + vertex.vertexId() + " score: " + score + " " + appender + " norm_ram: " + normRam + " norm_dist: " + normDistance +  " norm_dir: " + normDirection;
    }
}
