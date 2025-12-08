package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.FlowBasedDomainBranchData;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.farao_community.farao.gridcapa_core_valid_commons.vertex.VerticesUtils.getSelectedProjectedVertices;
import static com.farao_community.farao.gridcapa_core_valid_commons.vertex.VerticesUtils.getVerticesProjectedOnDomain;
import static java.util.stream.Collectors.toMap;

public class VertexService {

    private final List<CoreHub> coreHubs;

    public VertexService(final CoreHubsConfiguration coreHubsConfiguration) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
    }

    public List<Vertex> selectVerticesWithControlZone(final List<Vertex> baseVertices,
                                                      final List<? extends FlowBasedDomainBranchData> branchesData,
                                                      final ReferenceProgram referenceProgram,
                                                      final Double radius,
                                                      final int nbOfVerticesToSelect) {

        if (baseVertices.size() < nbOfVerticesToSelect) {
            return baseVertices;
        }

        double r = radius;
        List<Vertex> selectedProjected;

        do {
            // at some point it would cover all vertices,
            // but then it would have returned earlier,
            // so no endless loop
            selectedProjected = getSelectedProjectedVertices(baseVertices,
                                                             branchesData,
                                                             coreHubs,
                                                             controlZoneSelector(referenceProgram, r));
            r += radius;
        } while (selectedProjected.size() < nbOfVerticesToSelect);

        return selectedProjected;

    }

    public List<Vertex> selectVerticesByDistance(final List<Vertex> baseVertices,
                                                 final List<? extends FlowBasedDomainBranchData> branchesData,
                                                 final ReferenceProgram referenceProgram,
                                                 final int nbOfVerticesToSelect) {

        if (baseVertices.size() < nbOfVerticesToSelect) {
            return baseVertices;
        }

        final List<Vertex> projectedVertices = getVerticesProjectedOnDomain(baseVertices,
                                                                            branchesData,
                                                                            coreHubs);
        return selectClosestVertices(projectedVertices,
                                     referenceProgram,
                                     nbOfVerticesToSelect);
    }

    /**
     * @param referenceProgram contains the market positions
     * @param radius           n-dimension sphere radius, input by user
     * @return a Predicate that checks if a vertex is within a sphere centered on market positions
     */
    private Predicate<Vertex> controlZoneSelector(final ReferenceProgram referenceProgram,
                                                  final Double radius) {
        return vertex -> {
            final Map<String, Integer> coordinates = vertex.coordinates();
            final Map<String, Double> marketPositions = getMarketPositionsByAreaCode(referenceProgram);

            for (final CoreHub coreHub : coreHubs) {
                final double marketPos = marketPositions.get(coreHub.forecastCode());
                final double vertexPos = coordinates.get(coreHub.clusterVerticeCode());

                // if any coordinate is not in the nth-dimensional circle, we don't take the vertex
                if (vertexPos < marketPos - radius || vertexPos > marketPos + radius) {
                    return false;
                }
            }

            return true;
        };
    }

    /**
     * @param allVertices      all considered vertices
     * @param referenceProgram contains the market positions
     * @param n                is how many vertices we want to select
     * @return the nth vertices closest to the global market position
     */
    private List<Vertex> selectClosestVertices(final List<Vertex> allVertices,
                                               final ReferenceProgram referenceProgram,
                                               final int n) {

        final Map<String, Double> marketPositions = getMarketPositionsByAreaCode(referenceProgram);

        return allVertices.stream()
            .map(v -> vertexAndMarketDistance(marketPositions, v))
            .sorted(Comparator.comparingDouble(Pair::getRight))
            .limit(n)
            .map(Pair::getLeft)
            .toList();

    }

    /**
     * we return a pair because we want to be able to sort by distance but still keep the vertex data
     *
     * @param marketPositions the global net positions of the market
     * @param vertex          the considered vertex
     * @return the vertex and its distance from the market
     */
    private Pair<Vertex, Double> vertexAndMarketDistance(final Map<String, Double> marketPositions,
                                                         final Vertex vertex) {

        final Map<String, Integer> vertexPositions = vertex.coordinates();

        // global distance² = sum_over_hub(k_hub * [1D distance]²)
        final Double sumOfWeightedSquared = coreHubs.stream()
            .map(coreHub -> {
                final double vertexPos = vertexPositions.get(coreHub.clusterVerticeCode());
                final double diff = marketPositions.get(coreHub.forecastCode()) - vertexPos;
                return coreHub.coefficient() * diff * diff;
            })
            .reduce(Double::sum)
            .orElseThrow();

        return Pair.of(vertex, Math.sqrt(sumOfWeightedSquared));
    }

    private Map<String, Double> getMarketPositionsByAreaCode(final ReferenceProgram referenceProgram) {
        return referenceProgram.getAllGlobalNetPositions()
            .entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey().getAreaCode(), e.getValue()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
