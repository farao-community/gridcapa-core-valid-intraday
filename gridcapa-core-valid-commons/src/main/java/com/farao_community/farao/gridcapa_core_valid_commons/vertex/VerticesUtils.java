/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubUtils;
import com.farao_community.farao.gridcapa_core_valid_commons.exception.CoreValidCommonsInvalidDataException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TWO;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;

public final class VerticesUtils {

    public static final String VERTEX_ID_HEADER = "Vertex ID";

    private VerticesUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Vertex> importVertices(final InputStream verticesStream,
                                              final List<CoreHub> coreHubs) {
        return importVertices(new InputStreamReader(verticesStream, StandardCharsets.UTF_8), coreHubs);
    }

    public static List<Vertex> getVerticesProjectedOnDomain(final List<Vertex> baseVertices,
                                                            final List<? extends IFlowBasedDomainBranchData> fbDomainData,
                                                            final List<CoreHub> coreHubs) {

        final Map<String, String> flowBasedToVertexCodeMap = CoreHubUtils.getFlowBasedToVertexCodeMap(coreHubs);
        final List<Vertex> newVertices = new ArrayList<>();

        for (final Vertex vertex : baseVertices) {
            final Optional<BigDecimal> deltaMinOpt = fbDomainData.stream()
                    .map(branch -> delta(vertex, branch, flowBasedToVertexCodeMap))
                    .filter(delta -> delta.compareTo(ONE) < 0)
                    .min(BigDecimal::compareTo);

            newVertices.add(deltaMinOpt.map(delta -> projectedVertex(vertex, delta)).orElse(vertex));
        }

        return newVertices;
    }

    private static Vertex projectedVertex(final Vertex vertex,
                                          final BigDecimal delta) {
        // given that vertex is a record class, it's immutable
        final Map<String, Integer> coordinates = new HashMap<>(vertex.coordinates());
        coordinates.replaceAll((k, v) -> toProjectedPosition(v, delta));
        return new Vertex(vertex.vertexId(), coordinates);
    }

    private static List<Vertex> importVertices(final Reader reader,
                                               final List<CoreHub> coreHubs) {

        try {
            final List<Vertex> vertices = new ArrayList<>();
            final Iterable<CSVRecord> csvRecords = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);
            csvRecords.forEach(csvRecord -> {
                final Map<String, Integer> positions = coreHubs.stream().collect(Collectors.toMap(
                        CoreHub::clusterVerticeCode,
                        coreHub -> getCoordinate(csvRecord, coreHub)));
                vertices.add(new Vertex(Integer.parseInt(csvRecord.get(VERTEX_ID_HEADER)), positions));
            });
            return vertices;
        } catch (final IOException | IllegalArgumentException | NullPointerException e) {
            throw new CoreValidCommonsInvalidDataException("Exception occurred during parsing vertices file", e);
        }
    }

    /**
     * Set a coordinate to zero only if it is an empty HVDC coordinate.
     * This is the only case where a coordinate should be empty or blank.
     * Otherwise, should launch an exception.
     */
    private static Integer getCoordinate(final CSVRecord csvRecord,
                                         final CoreHub corehub) {
        final String coordinateString = csvRecord.get(corehub.clusterVerticeCode());
        if (corehub.isHvdcHub() && StringUtils.isBlank(coordinateString)) {
            return 0;
        }
        return Integer.parseInt(coordinateString);
    }

    private static int toProjectedPosition(final Integer netPosition,
                                           final BigDecimal delta) {
        return BigDecimal.valueOf(netPosition).multiply(delta).intValue();
    }

    private static BigDecimal delta(final Vertex vertex,
                                    final IFlowBasedDomainBranchData branchData,
                                    final Map<String, String> fbToVertexCode) {

        final BigDecimal f0Core = f0Core(vertex, branchData, fbToVertexCode);

        if (f0Core.equals(ZERO)) {
            // f0Core = 0 => delta '=' ∞
            // so we just have to return something > 1 as to do nothing here
            return TWO;
        }

        return BigDecimal.valueOf(branchData.getAmr())
                .add(BigDecimal.valueOf(branchData.getRam0Core()))
                .divide(f0Core, 15, FLOOR);
    }

    private static BigDecimal f0Core(final Vertex vertex,
                                     final IFlowBasedDomainBranchData branchData,
                                     final Map<String, String> fbToVertexCode) {
        //f0Core = ∑_over_hubs(PTDF*NP)
        return branchData.getPtdfValues()
                .entrySet()
                .stream()
                .map(ptdf -> getFlowOnHub(ptdf, vertex, fbToVertexCode))
                .reduce(BigDecimal::add)
                .orElseThrow();
    }

    private static BigDecimal getFlowOnHub(final Map.Entry<String, BigDecimal> ptdf,
                                           final Vertex vertex,
                                           final Map<String, String> fbToVertexCode) {
        final String countryCode = fbToVertexCode.get(ptdf.getKey());
        return ptdf.getValue().multiply(getNetPosition(countryCode, vertex));
    }

    private static BigDecimal getNetPosition(final String countryCode,
                                             final Vertex vertex) {
        return BigDecimal.valueOf(vertex.coordinates().getOrDefault(countryCode, 0));
    }
}
