/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.vertice;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VerticeImporter {

    private VerticeImporter() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Vertice> importVertices(final InputStream verticesStream, final List<CoreHub> coreHubs) {
        return importVertices(new InputStreamReader(verticesStream, StandardCharsets.UTF_8), coreHubs);
    }

    private static List<Vertice> importVertices(final Reader reader, final List<CoreHub> coreHubs) {

        try {
            final List<Vertice> vertices = new ArrayList<>();
            final Iterable<CSVRecord> csvRecords = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);
            csvRecords.forEach(csvRecord -> {
                final Map<String, Integer> positions = new HashMap<>();
                coreHubs.forEach(corehub ->
                    positions.put(corehub.clusterVerticeCode(),
                        getPosition(csvRecord, corehub))
                );
                vertices.add(new Vertice(Integer.parseInt(csvRecord.get("Vertex ID")), positions));
            });
            return vertices;
        } catch (IOException | IllegalArgumentException | NullPointerException  e) {
            throw new CoreValidCommonsInvalidDataException("Exception occurred during parsing vertice file", e);
        }
    }

    private static Integer getPosition(final CSVRecord csvRecord,
                                      final CoreHub corehub) {
        final String positionString = csvRecord.get(corehub.clusterVerticeCode());
        if (corehub.isHvdcHub() && StringUtils.isBlank(positionString)) {
            return 0;
        }
        return Integer.parseInt(positionString);
    }
}
