/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services.dichotomy;

import com.farao_community.farao.dichotomy.api.DichotomyEngine;
import com.farao_community.farao.dichotomy.api.index.HalfRangeFromMaxDivisionIndexStrategy;
import com.farao_community.farao.dichotomy.api.index.Index;
import com.farao_community.farao.dichotomy.api.results.DichotomyResult;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInternalException;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration.DichotomyConfiguration;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileExporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy.CoreValidNetworkShifter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy.RaoValidator;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.farao.rao_runner.api.resource.RaoSuccessResponse;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DichotomyRunner {

    private static final HalfRangeFromMaxDivisionIndexStrategy<RaoSuccessResponse> HALF_INDEX_STRATEGY_CONFIGURATION = new HalfRangeFromMaxDivisionIndexStrategy<>();
    private final DichotomyConfiguration dichotomyConfiguration;
    private final CoreHubsConfiguration coreHubsConfiguration;
    private final DateTimeFormatter networkFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm_'network.xiidm'");
    private final MinioAdapter minioAdapter;
    private final FileImporter fileImporter;
    private final FileExporter fileExporter;
    private final RaoRunnerClient raoRunnerClient;

    public DichotomyRunner(final DichotomyConfiguration dichotomyConfiguration,
                           final CoreHubsConfiguration coreHubsConfiguration,
                           final MinioAdapter minioAdapter,
                           final FileImporter fileImporter,
                           final FileExporter fileExporter,
                           final RaoRunnerClient raoRunnerClient) {
        this.dichotomyConfiguration = dichotomyConfiguration;
        this.coreHubsConfiguration = coreHubsConfiguration;
        this.minioAdapter = minioAdapter;
        this.fileImporter = fileImporter;
        this.fileExporter = fileExporter;
        this.raoRunnerClient = raoRunnerClient;
    }

    public DichotomyResult<RaoSuccessResponse> runDichotomy(final Vertex vertex,
                                                            final CoreValidIntradayRequest coreValidIntradayRequest,
                                                            final ShiftStudyPointData shiftStudyPointData) {
        final DichotomyEngine<RaoSuccessResponse> engine = buildDichotomyEngine(vertex,
                                                                                coreValidIntradayRequest,
                                                                                shiftStudyPointData);
        return engine.run(loadNetworkFromMinio(coreValidIntradayRequest.getTimestamp()));
    }

    private DichotomyEngine<RaoSuccessResponse> buildDichotomyEngine(final Vertex vertex,
                                                                     final CoreValidIntradayRequest coreValidIntradayRequest,
                                                                     final ShiftStudyPointData shiftStudyPointData) {
        return DichotomyEngine.<RaoSuccessResponse>builder()
                .withIndex(new Index<>(dichotomyConfiguration.getMinValue(), dichotomyConfiguration.getMaxValue(), dichotomyConfiguration.getPrecision()))
                .withIndexStrategy(HALF_INDEX_STRATEGY_CONFIGURATION)
                .withMaxIteration(dichotomyConfiguration.getMaxIterations())
                .withNetworkShifter(new CoreValidNetworkShifter(coreHubsConfiguration,
                                                                vertex,
                                                                shiftStudyPointData,
                                                                LoadFlowParameters.load()))
                .withNetworkValidator(new RaoValidator(coreValidIntradayRequest,
                                                       fileImporter,
                                                       fileExporter,
                                                       raoRunnerClient,
                                                       shiftStudyPointData.jsonCracUrl(),
                                                       shiftStudyPointData.raoParametersUrl()))
                .build();
    }

    private Network loadNetworkFromMinio(OffsetDateTime targetDateTime) {
        final String fileName = networkFormatter.format(targetDateTime);
        try (InputStream xiidm = minioAdapter.getFile("XIIDM/" + fileName)) {
            return Network.read(fileName, xiidm);
        } catch (IOException e) {
            throw new CoreValidIntradayInternalException("Could not load network from XIIDM file", e);
        }
    }
}
