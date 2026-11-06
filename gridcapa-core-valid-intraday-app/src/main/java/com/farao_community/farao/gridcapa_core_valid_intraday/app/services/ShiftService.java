package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayRequest;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.StudyPointResult;
import com.farao_community.farao.rao_runner.api.resource.RaoRequest;
import com.powsybl.glsk.commons.CountryEICode;
import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ShiftService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiftService.class);

    public RaoRequest computeStudyPointShift(final Vertex vertex,
                                             final ShiftStudyPointData shiftStudyPointData,
                                             final CoreValidIntradayRequest coreValidIntradayRequest) {
        final int vertexId = vertex.vertexId();
        LOGGER.info("Running computation study point for vertex {} ", vertexId);
        final StudyPointResult studyPointResult = new StudyPointResult(vertex);
        final Network network = shiftStudyPointData.network();
        final ZonalData<Scalable> scalableZonalData = shiftStudyPointData.scalableZonalData();
        final Map<String, Double> coreNetPositions = shiftStudyPointData.coreNetPositions();
        final String jsonCracUrl = shiftStudyPointData.jsonCracUrl();
        final String raoParametersUrl = shiftStudyPointData.raoParametersUrl();
        final String initialVariant = network.getVariantManager().getWorkingVariantId();
        final String newVariant = initialVariant + "_" + vertexId;
        network.getVariantManager().cloneVariant(initialVariant, newVariant);
        network.getVariantManager().setWorkingVariant(newVariant);
        RaoRequest raoRequest = null;
        try {

            Map<String, InitGenerator> initGenerators = setPminPmaxToDefaultValue(network, scalableZonalData);
            NetPositionsHandler.shiftNetPositionToStudyPoint(network, studyPoint, scalableZonalData, coreNetPositions);
            resetInitialPminPmax(network, scalableZonalData, initGenerators);
            String shiftedCgmUrl = fileExporter.saveShiftedCgm(network, studyPoint);
            studyPoint.getStudyPointResult().setShiftedCgmUrl(shiftedCgmUrl);
            String raoDirPath = String.format("%s/artifacts/RAO-%s-%s/", minioAdapter.getProperties().getBasePath(), timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'_'HH-mm")), studyPoint.getVerticeId());
            // For rao logs dispatcher, the rao request should correspond to the core valid request
            raoRequest = new RaoRequest.RaoRequestBuilder()
                    .withId(coreValidRequesttId)
                    .withRunId(coreValidRunId)
                    .withNetworkFileUrl(shiftedCgmUrl)
                    .withCracFileUrl(jsonCracUrl)
                    .withRaoParametersFileUrl(raoParametersUrl)
                    .withResultsDestination(raoDirPath)
                    .build();

        }catch (Exception e) {
            LOGGER.error("Error during study point {} computation", vertexId, e);
            studyPointResult.setStatus(StudyPointResult.Status.ERROR);
        } finally {
            network.getVariantManager().setWorkingVariant(initialVariant);
            network.getVariantManager().removeVariant(newVariant);
        }
        return raoRequest;
    }

    private Map<String, InitGenerator> setPminPmaxToDefaultValue(Network network, ZonalData<Scalable> scalableZonalData) {
        Map<String, InitGenerator> initGenerators = new HashMap<>();
        CoreAreasId.getCountriesId().stream()
                .map(zone -> new CountryEICode(Country.valueOf(zone)).getCode())
                .map(scalableZonalData::getData)
                .filter(Objects::nonNull)
                .flatMap(scalable -> scalable.filterInjections(network).stream()
                        .filter(Generator.class::isInstance)
                        .map(Generator.class::cast))
                .forEach(generator -> {
                    if (Double.isNaN(generator.getTargetP())) {
                        generator.setTargetP(0.);
                    }
                    InitGenerator initGenerator = new InitGenerator();
                    initGenerator.setpMin(generator.getMinP());
                    initGenerator.setpMax(generator.getMaxP());
                    initGenerators.put(generator.getId(), initGenerator);
                    generator.setMinP(DEFAULT_PMIN);
                    generator.setMaxP(DEFAULT_PMAX);
                });
        LOGGER.info("Pmax and Pmin are set to default values for network {}", network.getNameOrId());
        return initGenerators;
    }

    private void resetInitialPminPmax(Network network, ZonalData<Scalable> scalableZonalData, Map<String, InitGenerator> initGenerators) {
        CoreAreasId.getCountriesId().forEach(zone -> {
            String zoneEiCode = new CountryEICode(Country.valueOf(zone)).getCode();
            Scalable scalable = scalableZonalData.getData(zoneEiCode);
            if (scalable != null) {
                List<Generator> generators = scalable.filterInjections(network).stream()
                        .filter(Generator.class::isInstance)
                        .map(Generator.class::cast)
                        .toList();

                generators.forEach(generator -> {
                    generator.setMaxP(Math.max(generator.getTargetP(), initGenerators.get(generator.getId()).getpMax()));
                    generator.setMinP(Math.min(generator.getTargetP(), initGenerators.get(generator.getId()).getpMin()));
                });
            }
        });
        LOGGER.info("Pmax and Pmin are reset to initial values for network {}", network.getNameOrId());
    }

    private record InitGenerator(double pMin, double pMax) {}

}
