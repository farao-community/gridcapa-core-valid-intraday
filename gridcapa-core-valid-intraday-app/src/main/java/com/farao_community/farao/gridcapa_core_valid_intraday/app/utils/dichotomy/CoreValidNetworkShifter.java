/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy;

import com.farao_community.farao.dichotomy.api.NetworkShifter;
import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHubsConfiguration;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.ComputeUtils;
import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.modification.scalable.ScalingParameters;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowRunParameters;
import com.powsybl.openrao.commons.EICode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.farao_community.farao.dichotomy.api.results.ReasonInvalid.BALANCE_LOADFLOW_DIVERGENCE;
import static com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.ComputeUtils.getEICodeFromClusterVerticeCode;

public class CoreValidNetworkShifter implements NetworkShifter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreValidNetworkShifter.class);

    private static final double DEFAULT_PMAX = 9999.0;
    private static final double DEFAULT_PMIN = -9999.0;

    private final List<CoreHub> coreHubs;
    private final Vertex vertex;
    private final ShiftStudyPointData shiftStudyPointData;
    private final LoadFlowParameters loadFlowParameters;

    public CoreValidNetworkShifter(final CoreHubsConfiguration coreHubsConfiguration,
                                   final Vertex vertex,
                                   final ShiftStudyPointData shiftStudyPointData,
                                   final LoadFlowParameters loadFlowParameters) {
        this.coreHubs = Collections.unmodifiableList(coreHubsConfiguration.getCoreHubs());
        this.vertex = vertex;
        this.shiftStudyPointData = shiftStudyPointData;
        this.loadFlowParameters = loadFlowParameters;
    }

    @Override
    public void shiftNetwork(final double stepValue,
                             final Network network) throws GlskLimitationException, ShiftingException {
        final int vertexId = vertex.vertexId();
        LOGGER.info("Running computation study point for vertex {} ", vertexId);
        final ZonalData<Scalable> scalableZonalData = shiftStudyPointData.scalableZonalData();
        final Map<String, Double> coreNetPositions = shiftStudyPointData.netPositions();
        try {
            final Map<String, InitGenerator> initGenerators = setPminPmaxToDefaultValue(network, scalableZonalData);
            shiftNetPositionToStudyPoint(network, vertex, stepValue, scalableZonalData, coreNetPositions);
            resetInitialPminPmax(network, scalableZonalData, initGenerators);
            final LoadFlowResult result = runLoadFlow(network, loadFlowParameters);
            if (result.isFailed()) {
                LOGGER.error("Load flow computation diverged on network '{}' for vertex {}", network.getId(), vertexId);
                throw new ShiftingException("Load flow computation diverged during balancing adjustment", BALANCE_LOADFLOW_DIVERGENCE);
            }
        } catch (Exception e) {
            final String errorMessage = String.format("Error during study point shift computation on vertex %s", vertexId);
            LOGGER.error(errorMessage, vertexId, e);
            throw new ShiftingException(errorMessage, e);
        }
    }

    private void shiftNetPositionToStudyPoint(final Network network,
                                              final Vertex vertex,
                                              final double stepValue,
                                              final ZonalData<Scalable> scalableZonalData,
                                              final Map<String, Double> coreNetPositions) {
        vertex.coordinates().forEach((clusterVerticeCode, netPosition) -> {
            try {
                String zoneEiCode = getEICodeFromClusterVerticeCode(clusterVerticeCode).getAreaCode();
                final Double zoneNetPosition = coreNetPositions.getOrDefault(zoneEiCode, 0.);
                double shift = (netPosition * stepValue) - zoneNetPosition;
                LOGGER.info("Shift for zone {} with EICode {} : Study point {} | Ref prog {} | variation {}", clusterVerticeCode, zoneEiCode, netPosition, zoneNetPosition, shift);
                Scalable scalable = scalableZonalData.getData(zoneEiCode);
                if (scalable != null) {
                    ScalingParameters scalingParameters = new ScalingParameters().setPriority(ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED);
                    scalable.scale(network, shift, scalingParameters);
                }
            } catch (Exception e) {
                throw new CoreValidIntradayInvalidDataException("Error during the net position shift for zone " + clusterVerticeCode, e);
            }
        });
    }

    private Map<String, InitGenerator> setPminPmaxToDefaultValue(final Network network,
                                                                 final ZonalData<Scalable> scalableZonalData) {
        final Map<String, InitGenerator> initGenerators = new HashMap<>();

        coreHubs.stream()
                .map(ComputeUtils::getEICodeFromHub)
                .map(EICode::getAreaCode)
                .map(scalableZonalData::getData)
                .filter(Objects::nonNull)
                .flatMap(scalable -> scalable.filterInjections(network).stream()
                        .filter(Generator.class::isInstance)
                        .map(Generator.class::cast))
                .forEach(generator -> {
                    if (Double.isNaN(generator.getTargetP())) {
                        generator.setTargetP(0.);
                    }
                    final InitGenerator initGenerator = new InitGenerator(generator.getMinP(), generator.getMaxP());
                    initGenerators.put(generator.getId(), initGenerator);
                    generator.setMinP(DEFAULT_PMIN);
                    generator.setMaxP(DEFAULT_PMAX);
                });
        LOGGER.info("Generator Pmax and Pmin values are set to default for network {}", network.getNameOrId());
        return initGenerators;
    }

    private void resetInitialPminPmax(final Network network,
                                      final ZonalData<Scalable> scalableZonalData,
                                      final Map<String, InitGenerator> initGenerators) {

        coreHubs.stream()
                .map(ComputeUtils::getEICodeFromHub)
                .map(EICode::getAreaCode)
                .forEach(zoneEiCode -> {
                    Scalable scalable = scalableZonalData.getData(zoneEiCode);
                    if (scalable != null) {
                        List<Generator> generators = scalable.filterInjections(network).stream()
                                .filter(Generator.class::isInstance)
                                .map(Generator.class::cast)
                                .toList();

                        generators.forEach(generator -> {
                            generator.setMaxP(Math.max(generator.getTargetP(), initGenerators.get(generator.getId()).pMax()));
                            generator.setMinP(Math.min(generator.getTargetP(), initGenerators.get(generator.getId()).pMin()));
                        });
                    }
                });
        LOGGER.info("Generator Pmax and Pmin are reset to initial values for network {}", network.getNameOrId());
    }

    private record InitGenerator(double pMin, double pMax) {
    }

    private static LoadFlowResult runLoadFlow(final Network network,
                                              final LoadFlowParameters loadFlowParameters) {
        final LoadFlowRunParameters runParameters = new LoadFlowRunParameters()
                .setParameters(loadFlowParameters);
        return LoadFlow.run(network, runParameters);
    }
}
