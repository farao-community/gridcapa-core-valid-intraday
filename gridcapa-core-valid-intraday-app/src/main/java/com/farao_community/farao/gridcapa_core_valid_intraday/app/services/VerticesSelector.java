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
import com.powsybl.iidm.network.Country;
import com.powsybl.openrao.commons.EICode;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.ComputeUtils.computeCnecVertexProduct;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.naturalOrder;

@Service
public class VerticesSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticesSelector.class);
    private static final Comparator<SelectedVertexData> ORDER_BY_RAM_SELECTED_VERTEX = Comparator.comparing(SelectedVertexData::getRamNorm, Comparator.nullsLast(Comparator.naturalOrder()));
    private static final Comparator<CnecVertexRamData> ORDER_BY_RAM = Comparator.comparing(CnecVertexRamData::ram);
    private static final Comparator<SelectedVertexData> VERTEX_SCORE_COMPARATOR = Comparator.comparingDouble(SelectedVertexData::getScore);
    private static final Comparator<SelectedVertexData> ORDER_BY_PONDERATION = VERTEX_SCORE_COMPARATOR.thenComparingInt(v -> v.getVertex().vertexId());
    private final List<CoreHub> coreHubs;

    public VerticesSelector(final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
    }

    /**
     *
     * @param importedVertices  the list of projected vertices
     * @param marketPoints      the market points
     * @param cnecRamBranchData the CNEC ram branches
     * @param parameters        the application parameters
     * @return The ordered list of maxSelectedVertices vertices through ponderated selection
     */
    public List<SelectedVertexData> selectionSynthesis(final List<Vertex> importedVertices,
                                                       final ReferenceProgram marketPoints,
                                                       final List<CnecRamBranchData> cnecRamBranchData,
                                                       final CoreValidIntradayTaskParameters parameters) {

        final List<SelectedVertexData> selectedVertexDatas = importedVertices.stream()
                .map(SelectedVertexData::new)
                .toList();
        final int ponderationClosest = parameters.getPonderationClosest();
        orderByDistance(selectedVertexDatas, marketPoints, cnecRamBranchData, ponderationToQuotient(ponderationClosest), parameters.shouldCalculate());
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
     * @param importedVertices all considered vertices
     * @param referenceProgram contains the market positions
     * @param ponderation      the distance ponderation for scoring
     */
    public void orderByDistance(final List<SelectedVertexData> importedVertices,
                                final ReferenceProgram referenceProgram,
                                final List<CnecRamBranchData> cnecRamBranchData,
                                final Double ponderation,
                                final boolean isCalculateDeltaNpHub) {

        final Map<String, Double> deltaNpHubMap = isCalculateDeltaNpHub
                ? getDeltaNpHubMap(coreHubs, importedVertices)
                : gerDefaultDeltaNpHubMap();
        final Map<String, Double> kHub = calculateKHub(coreHubs, cnecRamBranchData);
        List<SelectedVertexData> orderedList = importedVertices.stream()
                .map(vertex -> vertexAndMarketDistance(referenceProgram, vertex, deltaNpHubMap, kHub))
                .sorted(comparingDouble(SelectedVertexData::getDistanceNorm))
                .toList();

        final Double minNormDistance = orderedList.getFirst().getDistanceNorm();
        final Double maxNormDistance = orderedList.getLast().getDistanceNorm();
        importedVertices.forEach(vertex -> updateDistanceNormAndScore(vertex, minNormDistance, maxNormDistance, ponderation));
    }

    /**
     * Scores the vertices based on angle
     *
     * @param importedVertices all considered vertices
     * @param referenceProgram contains the market positions
     * @param ponderation      the angle ponderation for scoring
     */
    public void orderByAngle(final List<SelectedVertexData> importedVertices,
                             final ReferenceProgram referenceProgram,
                             final Double ponderation) {

        List<SelectedVertexData> orderedList = importedVertices.stream()
                .map(v -> vertexAndMarketAngleDistance(referenceProgram, v))
                .sorted(comparingDouble(SelectedVertexData::getDirectionNorm))
                .toList();

        final Double minNormDirection = orderedList.getFirst().getDirectionNorm();
        final Double maxNormDirection = orderedList.getLast().getDirectionNorm();
        importedVertices.forEach(vertex -> updateDirectionNormAndScore(vertex, minNormDirection, maxNormDirection, ponderation));
    }

    /**
     * Scores the vertices based on constrained importedVertices with the most constrained CNEC and its calculated constrained RAM
     *
     * @param importedVertices   all considered importedVertices
     * @param cnecRamBranchDatas all considered CNECs
     * @param ponderation        the constrained ponderation for scoring
     */
    public void orderByConstrainedValue(final List<SelectedVertexData> importedVertices,
                                        final List<CnecRamBranchData> cnecRamBranchDatas,
                                        final Double ponderation) {

        final List<CnecRamBranchData> frenchCnecs = cnecRamBranchDatas.stream()
                .filter(CnecRamBranchData::isFrench)
                .toList();

        for (final SelectedVertexData selectedVertexData : importedVertices) {
            final List<CnecVertexRamData> vertexRamsByCnec = new ArrayList<>();
            for (final CnecRamBranchData branch : frenchCnecs) {
                final BigDecimal cnecVertexFlow = computeCnecVertexProduct(selectedVertexData.getVertex(), branch, coreHubs);
                if (cnecVertexFlow.compareTo(BigDecimal.ZERO) > 0) {
                    final BigDecimal constrainedValueRam = BigDecimal.valueOf(branch.ram()).subtract(cnecVertexFlow);
                    vertexRamsByCnec.add(new CnecVertexRamData(branch, selectedVertexData.getVertex(), constrainedValueRam));
                }
            }
            //for a given selectedVertexData get the lowest ram giving the most constrained CNEC
            if (!vertexRamsByCnec.isEmpty()) {
                final CnecVertexRamData minRamCnec = vertexRamsByCnec.stream()
                        .min(ORDER_BY_RAM)
                        .orElseThrow(
                                () -> new CoreValidIntradayInvalidDataException(String.format("Impossible to find worse CNEC for selectedVertexData id %s", selectedVertexData.getVertex().vertexId()))
                        );
                selectedVertexData.setRamNorm(minRamCnec.ram());
            } else {
                LOGGER.info("Vertex: {}, doesn't have corresponding french CNECs", selectedVertexData.getVertex().vertexId());
            }
        }
        final List<SelectedVertexData> orderedList = importedVertices.stream()
                .sorted(ORDER_BY_RAM_SELECTED_VERTEX)
                .toList();
        final BigDecimal minNormConstrained = orderedList.getFirst().getRamNorm();
        final BigDecimal maxNormConstrained = orderedList.getLast().getRamNorm();
        importedVertices.forEach(vertex -> updateConstrainedNormAndScore(vertex, minNormConstrained, maxNormConstrained, ponderation));
    }

    private Map<String, Double> gerDefaultDeltaNpHubMap() {
        return coreHubs.stream()
                .collect(Collectors.toMap(CoreHub::clusterVerticeCode, ch -> 1d));
    }

    private Map<String, Double> getDeltaNpHubMap(final List<CoreHub> coreHubs,
                                                 final List<SelectedVertexData> importedVertices) {

        //per hub we want : (maxVerticeNpByHub - minVerticeNpByHub) / (maxVerticeNpbBFrenchHub - minVerticeNpByFrenchHub)
        final Map<String, Double> deltaNpHubMap = new HashMap<>();
        final String frenchVertexCode = coreHubs.stream()
                .filter(coreHub -> coreHub.country() == Country.FR)
                .map(CoreHub::clusterVerticeCode)
                .findFirst()
                .orElseThrow(() -> new CoreValidIntradayInvalidDataException("No French CoreHub configuration found during vertex selection! "));
        final List<Integer> frenchNps = importedVertices.stream()
                .map(vertex -> vertex.getVertex().coordinates().get(frenchVertexCode))
                .sorted(naturalOrder())
                .toList();
        final double minFrenchNp = frenchNps.getFirst();
        final double maxFrenchNp = frenchNps.getLast();

        for (final CoreHub coreHub : coreHubs) {
            final List<SelectedVertexData> vertexOrderedByNpHub = importedVertices.stream()
                    .sorted(comparingInt(vertex -> getNpHub(coreHub, vertex)))
                    .toList();
            final double minNp = getNpHub(coreHub, vertexOrderedByNpHub.getFirst());
            final double maxNp = getNpHub(coreHub, vertexOrderedByNpHub.getLast());
            final double divisor = maxFrenchNp - minFrenchNp;
            if (divisor == 0.0) {
                LOGGER.warn("avoiding division by ZERO in method getDeltaNpHubMap");
                deltaNpHubMap.put(coreHub.clusterVerticeCode(), 1d);
            } else {
                final double deltaNpHub = (maxNp - minNp) / divisor;
                deltaNpHubMap.put(coreHub.clusterVerticeCode(), deltaNpHub);
            }
        }
        return deltaNpHubMap;
    }

    private static Integer getNpHub(final CoreHub coreHub,
                                      final SelectedVertexData vertex) {
        return vertex.getVertex().coordinates().get(coreHub.clusterVerticeCode());
    }

    private Map<String, Double> calculateKHub(final List<CoreHub> coreHubs,
                                              final List<CnecRamBranchData> cnecRamBranchData) {
        final List<CnecRamBranchData> frenchBaseCaseCnecs = cnecRamBranchData.stream()
                .filter(CnecRamBranchData::isFrench)
                .filter(CnecRamBranchData::isBaseCase)
                .toList();
        final Map<String, Double> deltaNpHubMap = new HashMap<>();
        for (CoreHub hub : coreHubs) {
            BigDecimal khubValue = frenchBaseCaseCnecs.stream()
                    .map(cnec -> calculateFrenchHubPoderation(cnec, hub, coreHubs))
                    .reduce(BigDecimal::add)
                    .orElseThrow(() -> new CoreValidIntradayInvalidDataException("Impossible to compute kHub value for french CNECs "));

            deltaNpHubMap.put(hub.clusterVerticeCode(), khubValue.doubleValue());
        }
        return deltaNpHubMap;
    }

    private BigDecimal calculateFrenchHubPoderation(final CnecRamBranchData cnec,
                                                    final CoreHub hub,
                                                    final List<CoreHub> coreHubs) {

        BigDecimal hubPtdfValue = cnec.getPtdfValues().get(hub.clusterVerticeCode());
        return coreHubs.stream()
                .filter(h -> !hub.equals(h))
                .map(CoreHub::clusterVerticeCode)
                .map(code -> getAbsDifferencePtdfHubValues(cnec, code, hubPtdfValue))
                .reduce(BigDecimal::add)
                .orElseThrow(() -> new CoreValidIntradayInvalidDataException(String.format("Impossible to find ptdf values for CNEC %s", cnec.branchId())));

    }

    private static BigDecimal getAbsDifferencePtdfHubValues(final CnecRamBranchData cnec,
                                                                     final String code,
                                                                     final BigDecimal hubPtdfValue) {
        return cnec.getPtdfValues().get(code).subtract(hubPtdfValue).abs();
    }

    private void updateDistanceNormAndScore(final SelectedVertexData selectedVertexData,
                                            final Double minNormDistance,
                                            final Double maxNormDistance,
                                            final Double ponderation) {

        final double divisor = maxNormDistance - minNormDistance;
        if (divisor == 0.0) {
            selectedVertexData.setDistanceNorm(1d);
        } else {
            final Double distanceNormNormalized = (selectedVertexData.getDistanceNorm() - minNormDistance) / divisor;
            selectedVertexData.setDistanceNorm(distanceNormNormalized);
        }
        updateVerticeScore(ponderation, selectedVertexData, selectedVertexData.getDistanceNorm());
    }

    private void updateDirectionNormAndScore(final SelectedVertexData selectedVertexData,
                                             final Double minNormDirection,
                                             final Double maxNormDirection,
                                             final Double ponderation) {

        final double divisor = maxNormDirection - minNormDirection;
        if (divisor == 0.0) {
            LOGGER.warn("avoiding division by ZERO in method updateDirectionNormAndScore");
            selectedVertexData.setDirectionNorm(1d);
        } else {
            final Double directionNormNormalized = 1 - ((selectedVertexData.getDirectionNorm() - minNormDirection) / divisor);
            selectedVertexData.setDirectionNorm(directionNormNormalized);
        }

        updateVerticeScore(ponderation, selectedVertexData, selectedVertexData.getDirectionNorm());
    }

    private void updateConstrainedNormAndScore(final SelectedVertexData selectedVertexData,
                                               final BigDecimal minNormRam,
                                               final BigDecimal maxNormRam,
                                               final Double ponderation) {

        final BigDecimal numerator = selectedVertexData.getRamNorm().subtract(minNormRam);
        final BigDecimal divisor = maxNormRam.subtract(minNormRam);
        final BigDecimal ramNormNormalized = numerator.divide(divisor, RoundingMode.HALF_EVEN);
        selectedVertexData.setRamNorm(ramNormNormalized);
        updateVerticeScore(ponderation, selectedVertexData, selectedVertexData.getRamNorm().doubleValue());
    }

    private static void updateVerticeScore(final Double ponderation,
                                           final SelectedVertexData selectedVertexData,
                                           final Double normScore) {
        selectedVertexData.setScore(normScore * ponderation + Optional.ofNullable(selectedVertexData.getScore()).orElse(0D));
    }

    private double ponderationToQuotient(final int ponderation) {
        return ponderation / 100.0;
    }

    /**
     * @param referenceProgram   contains the market positions
     * @param selectedVertexData the considered selectedVertexData
     * @return the selectedVertexData and its distance from the market
     */
    private SelectedVertexData vertexAndMarketDistance(final ReferenceProgram referenceProgram,
                                                       final SelectedVertexData selectedVertexData,
                                                       final Map<String, Double> deltaNpHubMap,
                                                       final Map<String, Double> kHub) {

        final Map<String, Integer> vertexPositions = selectedVertexData.getVertex().coordinates();

        // global distance² = sum_over_hub(deltaNpHub * k_hub * [1D distance]²)
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
            final double ponderateByHubs = deltaNpHubMap.get(hub.clusterVerticeCode()) * kHub.get(hub.clusterVerticeCode());
            final double weightedDistance = ponderateByHubs * distanceIn1D * distanceIn1D;
            sumOfWeightedSquared += weightedDistance;
        }
        selectedVertexData.setDistanceNorm(Math.sqrt(sumOfWeightedSquared));
        return selectedVertexData;
    }

    /**
     * @param referenceProgram   contains the market positions
     * @param selectedVertexData the considered selectedVertexData
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
            final Integer vertexPosition = vertexPositions.get(hub.clusterVerticeCode());

            if (vertexPosition == null) {
                throw new IllegalStateException(
                        String.format("Vertex %d missing required coordinate for hub %s / %s",
                                      selectedVertexData.getVertex().vertexId(), hub.forecastCode(), hub.clusterVerticeCode()));
            }
            scalarProduct += marketPos * vertexPosition;
            normMarket += marketPos * marketPos;
            normVertex += vertexPosition * vertexPosition;
        }
        final double divisor = Math.sqrt(normMarket) * Math.sqrt(normVertex);
        if (divisor == 0.0) {
            LOGGER.warn("avoiding division by ZERO in method vertexAndMarketAngleDistance");
            selectedVertexData.setDirectionNorm(1d);
        } else {
            final double cosinus = scalarProduct / divisor;
            selectedVertexData.setDirectionNorm(cosinus);
        }
        return selectedVertexData;
    }
}
