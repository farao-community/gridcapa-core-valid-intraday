/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy;

import com.farao_community.farao.dichotomy.api.exceptions.RaoFailureException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileExporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.rao_runner.api.resource.RaoFailureResponse;
import com.farao_community.farao.rao_runner.api.resource.RaoSuccessResponse;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.raoresult.api.RaoResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RaoValidatorTest {

    private final CoreValidIntradayRequest coreValidIntradayRequest = mock(CoreValidIntradayRequest.class);
    private final FileImporter fileImporter = mock(FileImporter.class);
    private final FileExporter fileExporter = mock(FileExporter.class);
    private final RaoRunnerClient raoRunnerClient = mock(RaoRunnerClient.class);

    @Test
    void validateNetworkTestOk() throws ValidationException, RaoInterruptionException, RaoFailureException {
        final OffsetDateTime timestamp = OffsetDateTime.of(2019, 1, 8, 2, 0, 0, 0, ZoneOffset.UTC);
        when(coreValidIntradayRequest.getTimestamp()).thenReturn(timestamp);
        when(coreValidIntradayRequest.getId()).thenReturn("id_123");
        when(coreValidIntradayRequest.getCurrentRunId()).thenReturn("run_id_123");
        final Network network = Network.read("network_12nodes_disconnected_hvdc.uct",
                                             Objects.requireNonNull(getClass().getResourceAsStream("/shift/network_12nodes_disconnected_hvdc.uct")));

        final RaoSuccessResponse abstractRaoResponse = mock(RaoSuccessResponse.class);
        when(raoRunnerClient.runRao(any())).thenReturn(abstractRaoResponse);
        when(abstractRaoResponse.isRaoFailed()).thenReturn(false);
        final RaoResult raoResult = mock(RaoResult.class);
        when(fileImporter.importRaoResult(any(), any())).thenReturn(raoResult);
        when(raoResult.isSecure(any())).thenReturn(true);
        RaoValidator raoValidator = new RaoValidator(coreValidIntradayRequest,
                                                     fileImporter,
                                                     fileExporter,
                                                     raoRunnerClient,
                                                     "testJsonUrl",
                                                     "testRaoUrl");

        final DichotomyStepResult<RaoSuccessResponse> raoSuccessResponseDichotomyStepResult = raoValidator.validateNetwork(network, null);
        Assertions.assertThat(raoSuccessResponseDichotomyStepResult).isNotNull();
    }

    @Test
    void validateNetworkTestKo1() throws ValidationException, RaoInterruptionException, RaoFailureException {
        final OffsetDateTime timestamp = OffsetDateTime.of(2019, 1, 8, 2, 0, 0, 0, ZoneOffset.UTC);
        when(coreValidIntradayRequest.getTimestamp()).thenReturn(timestamp);
        when(coreValidIntradayRequest.getId()).thenReturn("id_123");
        when(coreValidIntradayRequest.getCurrentRunId()).thenReturn("run_id_123");
        final Network network = Network.read("network_12nodes_disconnected_hvdc.uct",
                                             Objects.requireNonNull(getClass().getResourceAsStream("/shift/network_12nodes_disconnected_hvdc.uct")));

        final RaoFailureResponse abstractRaoResponse = mock(RaoFailureResponse.class);
        when(raoRunnerClient.runRao(any())).thenReturn(abstractRaoResponse);
        when(abstractRaoResponse.isRaoFailed()).thenReturn(true);
        RaoValidator raoValidator = new RaoValidator(coreValidIntradayRequest,
                                                     fileImporter,
                                                     fileExporter,
                                                     raoRunnerClient,
                                                     "testJsonUrl",
                                                     "testRaoUrl");

        Assertions.assertThatThrownBy(() -> raoValidator.validateNetwork(network, null)).doesNotThrowAnyExceptionExcept(ValidationException.class);
    }
}
