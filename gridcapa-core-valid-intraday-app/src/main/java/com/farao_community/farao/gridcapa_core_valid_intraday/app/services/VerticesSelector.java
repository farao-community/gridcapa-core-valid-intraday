/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecVertexRamData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.SelectedVertexData;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.ComputeUtils.computeCnecVertexProduct;
import static java.util.Comparator.comparingDouble;

@Service
public class VerticesSelector {

    private static final Comparator<SelectedVertexData> ORDER_BY_RAM_SELECTED_VERTEX = Comparator.comparing(SelectedVertexData::getNormRam);
    private static final Comparator<CnecVertexRamData> ORDER_BY_RAM = Comparator.comparing(CnecVertexRamData::ram);
    private static final Comparator<SelectedVertexData> VERTEX_SCORE_COMPARATOR = Comparator.comparingDouble(SelectedVertexData::getScore);
    private static final Comparator<SelectedVertexData> ORDER_BY_PONDERATION = VERTEX_SCORE_COMPARATOR.thenComparingInt(v -> v.getVertex().vertexId());
    private final List<CoreHub> coreHubs;

    public VerticesSelector(final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
    }

    /**
     *
     * @param projectedVertices                  the list of projected vertices
     * @param marketPoints                       the market points
     * @param cnecRamBranchData                  the cnec ram branches
     * @param parameters    the application parameters
     * @return The ordered list of maxSelectedVertices vertices through ponderated selection
     */
    public List<SelectedVertexData> selectionSynthesis(List<Vertex> projectedVertices,
                                                       ReferenceProgram marketPoints,
                                                       List<CnecRamBranchData> cnecRamBranchData,
                                                       CoreValidIntradayTaskParameters parameters) {

        final List<SelectedVertexData> selectedVertexDatas = projectedVertices.stream()
                .map(SelectedVertexData::new)
                .toList();
        final int ponderationClosest = parameters.getPonderationClosest();
        orderByDistance(selectedVertexDatas, marketPoints, ponderationToQuotient(ponderationClosest));
        final int ponderationAngle = parameters.getPonderationAngle();
        orderByAngle(selectedVertexDatas, marketPoints, ponderationToQuotient(ponderationAngle));
        final int ponderationConstrained = parameters.getPonderationConstrained();
        orderByConstrainedValue(selectedVertexDatas, cnecRamBranchData, ponderationToQuotient(ponderationConstrained));
        return selectedVertexDatas.stream()
                .sorted(ORDER_BY_PONDERATION)
                // We will take twice the limit because later we need more vertices than the limit and will apply the limit again
                .limit(parameters.getMaxSelectedVertices() * 2L)
                .toList();
    }

    /**
     * Scores the vertices based on distance
     *
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     */
    public void orderByDistance(final List<SelectedVertexData> projectedVertices,
                                final ReferenceProgram referenceProgram,
                                final Double ponderation) {

        List<SelectedVertexData> orderedList = projectedVertices.stream()
                .map(v -> vertexAndMarketDistance(referenceProgram, v))
                .sorted(comparingDouble(SelectedVertexData::getNormDistance))
                .toList();

        updateVerticesScores(ponderation, orderedList);
    }

    /**
     * Scores the vertices based on angle
     *
     * @param projectedVertices all considered vertices
     * @param referenceProgram  contains the market positions
     */
    public void orderByAngle(final List<SelectedVertexData> projectedVertices,
                             final ReferenceProgram referenceProgram,
                             final Double ponderation) {

        List<SelectedVertexData> orderedList = projectedVertices.stream()
                .map(v -> vertexAndMarketAngleDistance(referenceProgram, v))
                .sorted(comparingDouble(SelectedVertexData::getNormDirection))
                .toList();

        updateVerticesScores(ponderation, orderedList);
    }

    private static void updateVerticesScores(final Double ponderation,
                                             final List<SelectedVertexData> orderedList) {
        for (int index = 0; index < orderedList.size(); index++) {
            final SelectedVertexData vertex = orderedList.get(index);
            final Double score = vertex.getScore();
            if (score != null) {
                vertex.setScore(index * ponderation + score);
            } else {
                vertex.setScore(index * ponderation);
            }
        }
    }

    /**
     * Scores the vertices based on constrained projectedVertices with the most constrained CNEC and its calculated constrained RAM
     *
     * @param projectedVertices     all considered projectedVertices
     * @param cnecRamBranchDatas    all considered CNECs
     */
    public void orderByConstrainedValue(final List<SelectedVertexData> projectedVertices,
                                        final List<CnecRamBranchData> cnecRamBranchDatas,
                                        final Double ponderation) {

        final List<CnecRamBranchData> frenchCnecs = cnecRamBranchDatas.stream()
                .filter(CnecRamBranchData::isFrench)
                .toList();

        for (final SelectedVertexData selectedVertexData : projectedVertices) {
            final List<CnecVertexRamData> vertexRamsByCnec = new ArrayList<>();
            for (final CnecRamBranchData branch : frenchCnecs) {
                final BigDecimal cnecVertexFlow = computeCnecVertexProduct(selectedVertexData.getVertex(), branch, coreHubs);
                if (cnecVertexFlow.compareTo(BigDecimal.ZERO) > 0) {
                    vertexRamsByCnec.add(new CnecVertexRamData(branch, selectedVertexData.getVertex(), BigDecimal.valueOf(branch.getRam0Core()).subtract(cnecVertexFlow)));
                }
            }
            //for a given selectedVertexData get the lowest ram giving the most constrained CNEC
            if (!vertexRamsByCnec.isEmpty()) {
                final CnecVertexRamData minRamCnec = vertexRamsByCnec.stream()
                        .min(ORDER_BY_RAM)
                        .orElseThrow(
                                () -> new CoreValidIntradayInvalidDataException(String.format("Impossible to find worse CNEC for selectedVertexData id %s", selectedVertexData.getVertex().vertexId()))
                        );
                selectedVertexData.setNormRam(minRamCnec.ram());
            }
        }
        final List<SelectedVertexData> orderedList = projectedVertices.stream()
                .sorted(ORDER_BY_RAM_SELECTED_VERTEX)
                .toList();
        updateVerticesScores(ponderation, orderedList);
    }

    private double ponderationToQuotient(final int ponderation) {
        return ponderation / 100.0;
    }

    /**
     * @param referenceProgram        contains the market positions
     * @param selectedVertexData      the considered selectedVertexData
     * @return the selectedVertexData and its distance from the market
     */
    private SelectedVertexData vertexAndMarketDistance(final ReferenceProgram referenceProgram,
                                                       final SelectedVertexData selectedVertexData) {

        final Map<String, Integer> vertexPositions = selectedVertexData.getVertex().coordinates();

        // global distance² = sum_over_hub(k_hub * [1D distance]²)
        double sumOfWeightedSquared = 0.0;
        for (final CoreHub hub : coreHubs) {
            final Double marketPos = referenceProgram.getGlobalNetPosition(new EICode(hub.country()));
            final Integer vertexPos = vertexPositions.get(hub.clusterVerticeCode());

            if (vertexPos == null) {
                throw new IllegalStateException(
                        String.format("Vertex %d missing required coordinate for hub %s / %s",
                                      selectedVertexData.getVertex().vertexId(), hub.forecastCode(), hub.clusterVerticeCode()));
            }

            final double distanceIn1D = marketPos - vertexPos;
            final double weightedDistance = hub.coefficient() * distanceIn1D * distanceIn1D;
            sumOfWeightedSquared = sumOfWeightedSquared + weightedDistance;
        }
        selectedVertexData.setNormDistance(Math.sqrt(sumOfWeightedSquared));
        return selectedVertexData;
    }

    /**
     * @param referenceProgram contains the market positions
     * @param selectedVertexData           the considered selectedVertexData
     * @return the selectedVertexData and its distance from the market by direction and angle (uses cosinus)
     */
    private SelectedVertexData vertexAndMarketAngleDistance(final ReferenceProgram referenceProgram,
                                                            final SelectedVertexData selectedVertexData) {

        final Map<String, Integer> vertexPositions = selectedVertexData.getVertex().coordinates();

        // angle's cosinus = (scalar product of refprog . selectedVertexData) divided by (euclidian norm of refprog by selectedVertexData)
        double scalarProduct = 0.0;
        double normVertex = 0.0;
        double normMarket = 0.0;
        for (final CoreHub hub : coreHubs) {
            final Double marketPos = referenceProgram.getGlobalNetPosition(new EICode(hub.country()));
            final Integer vertexPos = vertexPositions.get(hub.clusterVerticeCode());

            if (vertexPos == null) {
                throw new IllegalStateException(
                        String.format("Vertex %d missing required coordinate for hub %s / %s",
                                      selectedVertexData.getVertex().vertexId(), hub.forecastCode(), hub.clusterVerticeCode()));
            }
            scalarProduct += marketPos * vertexPos;
            normMarket += marketPos * marketPos;
            final double vertexPosition = vertexPos;
            normVertex += vertexPosition * vertexPosition;
        }
        final double cosinus = scalarProduct / (Math.sqrt(normMarket) * Math.sqrt(normVertex));
        selectedVertexData.setNormDirection(cosinus);
        return selectedVertexData;
    }
}
