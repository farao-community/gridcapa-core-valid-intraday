/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services.dichotomy;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileExporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.powsybl.glsk.api.GlskDocument;
import com.powsybl.glsk.api.io.GlskDocumentImporters;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class DichotomyRunnerTest {

    @MockitoBean
    private CoreHubsConfiguration coreHubsConfiguration;

    @MockitoBean
    private MinioAdapter minioAdapter;

    @MockitoBean
    private FileImporter fileImporter;

    @MockitoBean
    private FileExporter fileExporter;

    @MockitoBean
    private RaoRunnerClient raoRunnerClient;

    @Autowired
    private DichotomyRunner dichotomyRunner;

    @Test
    void runTest() {
        when(coreHubsConfiguration.getCoreHubs()).thenReturn(new TestUtils.TestCoreHubReducedRealConf().getCoreHubs());
        when(minioAdapter.getFile(anyString())).thenReturn(Objects.requireNonNull(getClass().getResourceAsStream("/shift/network_12_nodes_disconnected_hvdc.xiidm")));
        final CoreValidIntradayRequest coreValidIntradayRequest = mock(CoreValidIntradayRequest.class);
        final OffsetDateTime timestamp = OffsetDateTime.of(2019, 1, 8, 2, 0, 0, 0, ZoneOffset.UTC);
        when(coreValidIntradayRequest.getTimestamp()).thenReturn(timestamp);
        when(coreValidIntradayRequest.getId()).thenReturn("id_123");
        when(coreValidIntradayRequest.getCurrentRunId()).thenReturn("run_id_123");
        final GlskDocument glskDocument = GlskDocumentImporters.importGlsk(Objects.requireNonNull(getClass().getResourceAsStream("/shift/glsk_proportional_12nodes_hvdc.xml")));
        final ShiftStudyPointData shiftStudyPointData = mock(ShiftStudyPointData.class);
        final Network network = Network.read("network_12nodes_disconnected_hvdc.uct", Objects.requireNonNull(getClass().getResourceAsStream("/shift/network_12nodes_disconnected_hvdc.uct")));
        when(shiftStudyPointData.netPositions()).thenReturn(Map.of("10YFR-RTE------C", 2000.0, "10YBE----------2", 6000.0, "10YCB-GERMANY--8", 8000.0));
        when(shiftStudyPointData.scalableZonalData()).thenReturn(glskDocument.getZonalScalable(network, timestamp.toInstant()));
        final Vertex vertex = TestUtils.getSingleVertex();
        dichotomyRunner.runDichotomy(vertex, coreValidIntradayRequest, shiftStudyPointData);
        verify(raoRunnerClient, times(4)).runRao(any());
    }
}
