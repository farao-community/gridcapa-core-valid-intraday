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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class VerticesImporter {

    public static final String VERTEX_ID_HEADER = "Vertex ID";

    private VerticesImporter() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Vertex> importVertices(final InputStream verticesStream, final List<CoreHub> coreHubs) {
        return importVertices(new InputStreamReader(verticesStream, StandardCharsets.UTF_8), coreHubs);
    }

    private static List<Vertex> importVertices(final Reader reader, final List<CoreHub> coreHubs) {

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
        } catch (final IOException | IllegalArgumentException | NullPointerException  e) {
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
}
