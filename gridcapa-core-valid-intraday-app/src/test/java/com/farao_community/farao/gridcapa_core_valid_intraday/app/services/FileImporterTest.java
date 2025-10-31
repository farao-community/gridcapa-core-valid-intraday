/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertice.Vertice;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Amira Kahya {@literal <amira.kahya at rte-france.com>}
 */
@SpringBootTest
class FileImporterTest {

    @Mock
    private UrlValidationService urlValidationService;

    @Autowired
    private FileImporter fileImporter;

    private static final OffsetDateTime TEST_DATE_TIME = OffsetDateTime.parse("2021-07-22T22:30Z");

    @Test
    void shouldImportReferenceProgramAndComputeGlobalNetPositions() {
        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", getClass().getResource("/20210723-FID2-632-v2-10V1001C--00264T-to-10V1001C--00085T.xml"));
        final ReferenceProgram referenceProgram = fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME);
        assertEquals(-50, referenceProgram.getGlobalNetPosition("10YFR-RTE------C"));
        assertEquals(-1325, referenceProgram.getGlobalNetPosition("10YCB-GERMANY--8"));
        assertEquals(225, referenceProgram.getGlobalNetPosition("10YNL----------L"));
        assertEquals(1150, referenceProgram.getGlobalNetPosition("10YBE----------2"));
    }

    @Test
    void importReferenceProgramShouldThrowCoreValidIntradayInvalidDataException() throws Exception {

        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", new URI("https://example.com/refprog.xml").toURL());

        when(urlValidationService.openUrlStream(anyString())).thenThrow(new CoreValidIntradayInvalidDataException("Connection failed"));

        CoreValidIntradayInvalidDataException exception = assertThrows(
                CoreValidIntradayInvalidDataException.class,
                () -> fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME)
        );

        assertTrue(exception.getMessage().contains("Cannot import reference program file from URL"));
    }

    @Test
    void shouldImportVerticesFromCoreHubSettings() {
        final CoreValidIntradayFileResource verticeFile = createFileResource("vertice",  getClass().getResource("/fake-vertice.csv"));
        final List<Vertice> vertices = fileImporter.importVertices(verticeFile);
        Assertions.assertThat(vertices)
                .isNotNull()
                .hasSize(4);
        final Vertice vertice = vertices.getFirst();
        Assertions.assertThat(vertice.getVerticeId()).isEqualTo(1);
        final HashMap<String, Integer> entries = new HashMap<>();
        entries.put("FR", 11);
        entries.put("BE", 111);
        entries.put("BE_AL", 0);
        entries.put("DE", 1111);
        entries.put("DE_AL", 0);
        final Map<String, Integer> positions = vertice.getPositions();
        Assertions.assertThat(positions)
                .hasSize(5)
                .containsExactlyInAnyOrderEntriesOf(entries);
    }

    @Test
    void importVerticeShouldThrowCoreValidIntradayInvalidDataException() throws Exception {

        final CoreValidIntradayFileResource verticeFile = createFileResource("vertice", new URI("https://example.com/vertice.csv").toURL());

        when(urlValidationService.openUrlStream(anyString())).thenThrow(new CoreValidIntradayInvalidDataException("Connection failed"));

        Assertions.assertThatExceptionOfType(CoreValidIntradayInvalidDataException.class)
                .isThrownBy(() -> fileImporter.importVertices(verticeFile))
                .withMessage("Cannot import vertice file from URL 'https://example.com/vertice.csv'");
    }

    private CoreValidIntradayFileResource createFileResource(final String filename, final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }
}
