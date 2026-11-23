/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy;

import com.farao_community.farao.dichotomy.api.NetworkValidator;
import com.farao_community.farao.dichotomy.api.exceptions.RaoFailureException;
import com.farao_community.farao.dichotomy.api.exceptions.RaoInterruptionException;
import com.farao_community.farao.dichotomy.api.exceptions.ValidationException;
import com.farao_community.farao.dichotomy.api.results.DichotomyStepResult;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileExporter;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.services.FileImporter;
import com.farao_community.farao.rao_runner.api.resource.AbstractRaoResponse;
import com.farao_community.farao.rao_runner.api.resource.RaoFailureResponse;
import com.farao_community.farao.rao_runner.api.resource.RaoRequest;
import com.farao_community.farao.rao_runner.api.resource.RaoSuccessResponse;
import com.farao_community.farao.rao_runner.starter.RaoRunnerClient;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.raoresult.api.RaoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaoValidator implements NetworkValidator<RaoSuccessResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RaoValidator.class);

    private final CoreValidIntradayRequest coreValidIntradayRequest;
    private final FileImporter fileImporter;
    private final FileExporter fileExporter;
    private final RaoRunnerClient raoRunnerClient;
    private final String jsonCracUrl;
    private final String raoParametersUrl;
    private static final String ARTIFACTS = "ARTIFACTS";
    private int variantCounter = 0;

    public RaoValidator(final CoreValidIntradayRequest coreValidIntradayRequest,
                        final FileImporter fileImporter,
                        final FileExporter fileExporter,
                        final RaoRunnerClient raoRunnerClient,
                        final String jsonCracUrl,
                        final String raoParametersUrl) {
        this.fileImporter = fileImporter;
        this.fileExporter = fileExporter;
        this.raoRunnerClient = raoRunnerClient;
        this.coreValidIntradayRequest = coreValidIntradayRequest;
        this.jsonCracUrl = jsonCracUrl;
        this.raoParametersUrl = raoParametersUrl;
    }

    @Override
    public DichotomyStepResult<RaoSuccessResponse> validateNetwork(final Network network,
                                                                   final DichotomyStepResult<RaoSuccessResponse> lastDichotomyStepResult) throws ValidationException, RaoFailureException, RaoInterruptionException {
        String scaledNetworkDirPath = generateScaledNetworkDirPath(network);
        String scaledNetworkName = network.getNameOrId() + ".xiidm";
        String networkPresignedUrl = fileExporter.saveNetworkInArtifact(network, scaledNetworkDirPath + scaledNetworkName, "", coreValidIntradayRequest.getTimestamp());
        RaoRequest raoRequest = buildRaoRequest(networkPresignedUrl, "CORE/VALID/" + scaledNetworkDirPath);
        try {
            LOGGER.info("RAO request sent: {}", raoRequest);
            AbstractRaoResponse abstractRaoResponse = raoRunnerClient.runRao(raoRequest);
            LOGGER.info("RAO response received: {}", abstractRaoResponse);
            if (abstractRaoResponse.isRaoFailed()) {
                RaoFailureResponse failureResponse = (RaoFailureResponse) abstractRaoResponse;
                throw new ValidationException(failureResponse.getErrorMessage());
            }

            RaoSuccessResponse raoResponse = (RaoSuccessResponse) abstractRaoResponse;
            RaoResult raoResult = fileImporter.importRaoResult(raoResponse.getRaoResultFileUrl(), fileImporter.importCracFromJson(raoResponse.getCracFileUrl(), network));
            return DichotomyStepResult.fromNetworkValidationResult(raoResult, raoResponse);
        } catch (RuntimeException e) {
            throw new ValidationException("RAO run failed", e);
        }
    }

    private RaoRequest buildRaoRequest(String networkPresignedUrl,
                                       String scaledNetworkDirPath) {
        return new RaoRequest.RaoRequestBuilder()
                .withId(coreValidIntradayRequest.getId())
                .withRunId(coreValidIntradayRequest.getCurrentRunId())
                .withNetworkFileUrl(networkPresignedUrl)
                .withCracFileUrl(jsonCracUrl)
                .withRaoParametersFileUrl(raoParametersUrl)
                .withResultsDestination(scaledNetworkDirPath)
                .build();
    }

    private String generateScaledNetworkDirPath(Network network) {
        String basePath = fileExporter.makeDestinationMinioPath(coreValidIntradayRequest.getTimestamp(), ARTIFACTS);
        String variantName = network.getVariantManager().getWorkingVariantId();
        return String.format("%s/%s-%s/", basePath, ++variantCounter, variantName);
    }
}
