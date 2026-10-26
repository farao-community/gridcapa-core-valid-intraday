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

    private final Vertex vertex;
    private Double score;
    private BigDecimal ramNorm;
    private Double distanceNorm;
    private Double directionNorm;

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

    public BigDecimal getRamNorm() {
        return ramNorm;
    }

    public void setRamNorm(final BigDecimal ramNorm) {
        this.ramNorm = ramNorm;
    }

    public Double getDistanceNorm() {
        return distanceNorm;
    }

    public void setDistanceNorm(final Double distanceNorm) {
        this.distanceNorm = distanceNorm;
    }

    public Double getDirectionNorm() {
        return directionNorm;
    }

    public void setDirectionNorm(final Double directionNorm) {
        this.directionNorm = directionNorm;
    }

    @Override
    public String toString() {
        StringBuilder appender = new StringBuilder();
        vertex.coordinates().forEach((hub, val) -> appender.append(hub).append(": ").append(val).append(", "));
        return "Id: " + vertex.vertexId() + " score: " + score + " " + appender + " norm_ram: " + ramNorm + " norm_dist: " + distanceNorm + " norm_dir: " + directionNorm;
    }
}
