/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingDouble;

public class VerticesSelector {
    private final List<CoreHub> coreHubs;

    public VerticesSelector(final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
    }

    /**
     *
     * @param projectedVertices    all considered vertices
     * @param referenceProgram     contains the market positions
     * @param radius               the n-sphere radius
     * @param nbOfVerticesToSelect how many vertices do we want
     * @param fallBackOnClosest    if we don't have the expected number, do we switch to closest or leave it as is
     * @return selected vertices with n-sphere method
     */
    public List<Vertex> selectVerticesWithinNSphere(final List<Vertex> projectedVertices,
                                                    final ReferenceProgram referenceProgram,
                                                    final Double radius,
                                                    final int nbOfVerticesToSelect,
                                                    final boolean fallBackOnClosest) {

        if (projectedVertices.size() < nbOfVerticesToSelect) {
            return projectedVertices;
        }

        final List<Vertex> selectedProjected = projectedVertices
            .stream()
            .filter(vertex -> isInNSphere(vertex, referenceProgram, radius))
            .toList();

        if (!fallBackOnClosest) {
            return selectedProjected;
        } else {
            final List<Vertex> verticesToConsider = selectedProjected.size() > nbOfVerticesToSelect ?
                selectedProjected : projectedVertices;
            return selectClosestVertices(verticesToConsider, referenceProgram, nbOfVerticesToSelect);
        }

    }

    /**
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     * @param n                 is how many vertices we want to select
     * @return the nth vertices closest to the global market position
     */
    public List<Vertex> selectClosestVertices(final List<Vertex> projectedVertices,
                                              final ReferenceProgram referenceProgram,
                                              final int n) {

        if (projectedVertices.size() < n) {
            return projectedVertices;
        }

        return projectedVertices.stream()
            .map(v -> vertexAndMarketDistance(referenceProgram, v))
            .sorted(comparingDouble(Pair::getRight))
            .limit(n)
            .map(Pair::getLeft)
            .toList();

    }

    /**
     * @param vertex           the considered vertex
     * @param referenceProgram contains the market positions
     * @param radius           n-dimension sphere radius, input by user
     * @return if a vertex is within an n-sphere centered on market positions
     */
    private boolean isInNSphere(final Vertex vertex,
                                final ReferenceProgram referenceProgram,
                                final double radius) {
        return vertexAndMarketDistance(referenceProgram, vertex).getRight() <= radius;
    }

    /**
     * we return a pair because we want to be able to sort by distance but still keep the vertex data
     *
     * @param referenceProgram contains the market positions
     * @param vertex           the considered vertex
     * @return the vertex and its distance from the market
     */
    private Pair<Vertex, Double> vertexAndMarketDistance(final ReferenceProgram referenceProgram,
                                                         final Vertex vertex) {

        final Map<String, Integer> vertexPositions = vertex.coordinates();

        // global distance² = sum_over_hub(k_hub * [1D distance]²)
        double sumOfWeightedSquared = 0.0;
        for (final CoreHub hub : coreHubs) {
            final Double marketPos = referenceProgram.getGlobalNetPosition(hub.forecastCode());
            final Integer vertexPos = vertexPositions.get(hub.clusterVerticeCode());

            if (vertexPos == null) {
                throw new IllegalStateException(
                    String.format("Vertex %d missing required coordinate for hub %s / %s",
                                  vertex.vertexId(), hub.forecastCode(), hub.clusterVerticeCode()));
            }

            final double distanceIn1D = marketPos - vertexPos;
            final double weightedDistance = hub.coefficient() * distanceIn1D * distanceIn1D;
            sumOfWeightedSquared = sumOfWeightedSquared + weightedDistance;
        }

        return Pair.of(vertex, Math.sqrt(sumOfWeightedSquared));
    }

}
