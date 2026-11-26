/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils.createFileResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class FileExporterTest {

    private static final String RESULT_OK = "resultOK";
    private static final OffsetDateTime TIMESTAMP = OffsetDateTime.of(2019, 1, 8, 2, 0, 0, 0, ZoneOffset.UTC);

    @MockitoBean
    private MinioAdapter minioAdapter;

    @Autowired
    private FileImporter fileImporter;

    @Autowired
    private FileExporter fileExporter;

    @Test
    void saveNetworkForTimestampTest() {
        final CoreValidIntradayFileResource networkFile = createFileResource("network", Objects.requireNonNull(getClass().getResource("/20210723_0030.uct")));
        final Network network = fileImporter.importNetwork(networkFile);
        fileExporter.saveNetworkForTimestamp(network, TIMESTAMP);
        verify(minioAdapter, times(1)).uploadArtifactForTimestamp(anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void saveNetworkInArtifactTest() {
        final CoreValidIntradayFileResource networkFile = createFileResource("network", Objects.requireNonNull(getClass().getResource("/20210723_0030.uct")));
        final Network network = fileImporter.importNetwork(networkFile);
        when(minioAdapter.generatePreSignedUrl(anyString())).thenReturn(RESULT_OK);
        final String testResult = fileExporter.saveNetworkInArtifact(network, "network/file/path", "TEST", TIMESTAMP);
        assertThat(testResult).isEqualTo(RESULT_OK);
    }

    @Test
    void saveRaoParametersAndGetUrlTest() {
        final RaoParameters raoParameters = new RaoParameters(ReportNode.NO_OP);
        when(minioAdapter.generatePreSignedUrl(anyString())).thenReturn(RESULT_OK);
        final String testResult = fileExporter.saveRaoParametersAndGetUrl(raoParameters);
        assertThat(testResult).isEqualTo(RESULT_OK);
    }

    @Test
    void saveCracInJsonFormatTest() {
        final OffsetDateTime testDateTimeCrac = OffsetDateTime.parse("2021-07-22T22:30Z");
        final CoreValidIntradayFileResource networkFile = createFileResource("network", Objects.requireNonNull(getClass().getResource("/20210723_0030.uct")));
        final Network network = fileImporter.importNetwork(networkFile);
        final CoreValidIntradayFileResource cracFile = createFileResource("mergedCnec", Objects.requireNonNull(getClass().getResource("/20210723-F666.xml")));
        final FbConstraintCreationContext crac = fileImporter.importMergedCnec(cracFile, network, testDateTimeCrac);
        when(minioAdapter.generatePreSignedUrl(anyString())).thenReturn(RESULT_OK);
        final String result = fileExporter.saveCracInJsonFormat(crac.getCrac(), testDateTimeCrac);
        assertThat(result).isEqualTo(RESULT_OK);
    }

    @Test
    void makeDestinationMinioPathTest() {
        //winter time
        final String pathTest1 = fileExporter.makeDestinationMinioPath(TIMESTAMP, "TEST");
        assertThat(pathTest1).isEqualTo("IDCC/2019/01/08/03_00/TEST/");
        //Daylight saving time
        final OffsetDateTime timestamp2 = OffsetDateTime.of(2022, 7, 14, 20, 0, 0, 0, ZoneOffset.UTC);
        final String pathTest2 = fileExporter.makeDestinationMinioPath(timestamp2, "DSL_TEST");
        assertThat(pathTest2).isEqualTo("IDCC/2022/07/14/22_00/DSL_TEST/");
    }
}
