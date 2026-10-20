/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils;

import com.farao_community.farao.gridcapa_core_valid_commons.core_hub.CoreHub;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CnecRamBranchData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.CoreValidIntradayTaskParameters;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.VertexToFrenchCnecs;
import org.slf4j.Logger;
import org.yaml.snakeyaml.util.Tuple;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Optional;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Comparator.comparing;

public final class DichotomyMethodFinder {

    private DichotomyMethodFinder() {
        // utility class
    }

    public static List<VertexToFrenchCnecs> getFrenchCnecsDefiningVertices(final List<Vertex> filteredVertices,
                                                                           final List<CnecRamBranchData> cnecs,
                                                                           final List<CoreHub> coreHubs,
                                                                           final CoreValidIntradayTaskParameters taskParameters,
                                                                           final Logger businessLogger) {

        return filteredVertices.stream()
                .map(vertex -> vertexToFrenchCnecs(vertex, cnecs, coreHubs, taskParameters))
                .filter(vfc -> filterVertexWithNoFrenchCnecs(vfc, businessLogger))
                .limit(taskParameters.getMaxSelectedVertices())
                .toList();
    }

    private static VertexToFrenchCnecs vertexToFrenchCnecs(final Vertex vertex,
                                                           final List<CnecRamBranchData> cnecs,
                                                           final List<CoreHub> coreHubs,
                                                           final CoreValidIntradayTaskParameters taskParameters) {

        final List<CnecRamBranchData> marginFilteredCnecs = filterCnecsByMarginForVertex(vertex, cnecs, coreHubs, taskParameters.getRamMargin());
        final List<CnecRamBranchData> frenchNonLimitedCnecs = getFrenchNonLimitingCnecs(marginFilteredCnecs);
        final List<CnecRamBranchData> nonFrenchLimitingCnecs = getNonFrenchLimitingCnecs(marginFilteredCnecs);
        final List<CnecRamBranchData> frenchCorrespondingCnecs = nonFrenchLimitingCnecs.stream()
                .map(nonFrenchCnec -> getVertexCorrepondingFrenchCnec(nonFrenchCnec, frenchNonLimitedCnecs, coreHubs, taskParameters.getMergeAngle()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return new VertexToFrenchCnecs(vertex, frenchCorrespondingCnecs);
    }

    private static boolean filterVertexWithNoFrenchCnecs(final VertexToFrenchCnecs vfc,
                                                         final Logger businessLogger) {
        if (vfc.frenchCnecs().isEmpty()) {
            businessLogger.info("Vertex with id: {}, has no corresponding french Cnecs with correct direction", vfc.vertex().vertexId());
            return false;
        }
        return true;
    }

    private static Optional<CnecRamBranchData> getVertexCorrepondingFrenchCnec(final CnecRamBranchData nonFrenchCnec,
                                                                               final List<CnecRamBranchData> frenchNonlimitedCnecs,
                                                                               final List<CoreHub> coreHubs,
                                                                               final int mergeAngle) {
        return frenchNonlimitedCnecs.stream()
                .map(frCnec -> computeAngleBetweenCnecs(nonFrenchCnec, frCnec, coreHubs))
                .max(comparing(Tuple::_1))
                .filter(t -> t._1().compareTo(BigDecimal.valueOf(mergeAngle)) < 0)
                .map(Tuple::_2);

    }

    private static Tuple<BigDecimal, CnecRamBranchData> computeAngleBetweenCnecs(final CnecRamBranchData nonFrenchCnec,
                                                                                 final CnecRamBranchData frCnec,
                                                                                 final List<CoreHub> coreHubs) {
        final BigDecimal angle = coreHubs.stream()
                .map(CoreHub::flowbasedCode)
                .map(fbc -> nonFrenchCnec.getPtdfValues().get(fbc).multiply(frCnec.getPtdfValues().get(fbc)))
                .reduce(BigDecimal::add)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Cnecs %S and %S are missing required coordinates for angle computation",
                                      frCnec.branchId(), nonFrenchCnec.branchId())
                ));

        final BigDecimal frCnecNorm = computeNorm(nonFrenchCnec, coreHubs);
        final BigDecimal nonFrCnecNorm = computeNorm(frCnec, coreHubs);
        return new Tuple<>(angle.multiply(frCnecNorm).multiply(nonFrCnecNorm), frCnec);
    }

    private static BigDecimal computeNorm(final CnecRamBranchData cnec,
                                          final List<CoreHub> coreHubs) {
        final BigDecimal norme = coreHubs.stream()
                .map(ch -> cnec.getPtdfValues().get(ch.flowbasedCode()))
                .map(ptdfVal -> ptdfVal.multiply(ptdfVal))
                .reduce(BigDecimal::add)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Cannot compute norm of Cnec: %S",
                                      cnec.branchId())
                ));
        return norme.sqrt(MathContext.DECIMAL32);
    }

    private static List<CnecRamBranchData> getNonFrenchLimitingCnecs(final List<CnecRamBranchData> marginFilteredCnecs) {
        return marginFilteredCnecs.stream()
                .filter(cnec -> !cnec.isFrench() && cnec.isLimiting())
                .toList();
    }

    private static List<CnecRamBranchData> getFrenchNonLimitingCnecs(final List<CnecRamBranchData> marginFilteredCnecs) {
        return marginFilteredCnecs.stream()
                .filter(cnec -> cnec.isFrench() && !cnec.isLimiting())
                .toList();
    }

    private static List<CnecRamBranchData> filterCnecsByMarginForVertex(final Vertex vertex,
                                                                        final List<CnecRamBranchData> cnecs,
                                                                        final List<CoreHub> coreHubs,
                                                                        final int marginRam) {
        return cnecs.stream()
                .filter(cnec -> marginVertexCnec(vertex, cnec, coreHubs) < marginRam)
                .toList();
    }

    // Computes distance (margin) between Cnec and Vertex
    private static int marginVertexCnec(final Vertex vertex,
                                        final CnecRamBranchData cnec,
                                        final List<CoreHub> coreHubs) {

        final BigDecimal cnecVerticeProduct = coreHubs.stream()
                .map(ch -> cnec.getPtdfValues().get(ch.flowbasedCode()).multiply(BigDecimal.valueOf(vertex.coordinates().get(ch.clusterVerticeCode()))))
                .reduce(BigDecimal::add)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Vertex %d and Cnec %S missing required coordinates for margin computation",
                                      vertex.vertexId(), cnec.branchId())
                ));
        return cnec.ram() - cnecVerticeProduct.setScale(0, HALF_EVEN).intValue();
    }

}
