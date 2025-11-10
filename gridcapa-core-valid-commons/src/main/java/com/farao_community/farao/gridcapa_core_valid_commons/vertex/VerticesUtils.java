/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertex;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.exception.CoreValidCommonsInvalidDataException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class VerticesUtils {

    public static final String PTDF_PREFIX = "PTDF_";
    public static final String VERTEX_ID_HEADER = "Vertex ID";

    private VerticesUtils() {
        throw new IllegalStateException("Utility class");
    }

    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
                VERTICES IMPORT
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

    public static List<Vertex> importVertices(final InputStream verticesStream,
                                              final List<CoreHub> coreHubs) {
        return importVertices(new InputStreamReader(verticesStream, StandardCharsets.UTF_8), coreHubs);
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

    /*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
                VERTICES PROJECTION
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

    public static List<Vertex> getVerticesProjectedOnDomain(final List<Vertex> vertices,
                                                            final List<? extends IFlowBasedDomainBranchData> fbDomainData) {

        final List<Vertex> projectedVertices = new ArrayList<>();
        for (final Vertex vertex : vertices) {
            final Optional<BigDecimal> deltaMinOpt = fbDomainData.stream()
                    .map(b -> delta(vertex, b))
                    .filter(delta -> delta.compareTo(BigDecimal.ONE) < 0)
                    .min(BigDecimal::compareTo);

            // given that vertex is a record class, it's immutable
            deltaMinOpt.ifPresentOrElse(deltaMin -> {
                final Map<String, Integer> coordinates = new HashMap<>(vertex.coordinates());
                coordinates.replaceAll((k, v) -> toProjectedPosition(v, deltaMin));
                projectedVertices.add(new Vertex(vertex.vertexId(), coordinates));
            }, () -> projectedVertices.add(vertex));

        }

        return projectedVertices;
    }

    private static int toProjectedPosition(final Integer netPosition,
                                           final BigDecimal delta) {
        return BigDecimal.valueOf(netPosition).multiply(delta).intValue();
    }

    public static BigDecimal delta(final Vertex vertex,
                                   final IFlowBasedDomainBranchData branchData) {
        return BigDecimal.valueOf(branchData.getAmr() + branchData.getRam0Core())
                .divide(f0Core(vertex, branchData), 15, RoundingMode.FLOOR);
    }

    public static BigDecimal f0Core(final Vertex vertex,
                                    final IFlowBasedDomainBranchData branchData) {
        //f0Core = ∑_over_hubs(PTDF*NP)
        return branchData.getPtdfValues()
                .entrySet()
                .stream()
                .map(ptdf -> ptdf.getValue().multiply(getNetPosition(ptdf.getKey(), vertex)))
                .reduce(BigDecimal::add)
                .orElseThrow();
    }

    private static BigDecimal getNetPosition(final String ptdfKey,
                                             final Vertex vertex) {
        return BigDecimal.valueOf(vertex.coordinates().get(ptdfKey.replace(PTDF_PREFIX, "")));
    }
}
