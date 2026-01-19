package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.ConstResultType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.CriticalBranchType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainTimeSeriesType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.IntervalType;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.PeriodType;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

public class IvaVolumesManager {
    private static final String FRENCH = "FR";
    private static final String RTE_CODE = "10YFR-RTE------C";
    private static final double FRM_MARGIN_PERCENTAGE = 0.05;

    private final List<Vertex> vertices;
    private final BigDecimal frenchMarketGlobalNetPosition;
    private final Map<String, BigDecimal> ptdfsZsByBranch;
    private final List<CriticalBranchType> criticalBranches;

    public IvaVolumesManager(final List<Vertex> vertices,
                             final ReferenceProgram refProg,
                             final Map<String, BigDecimal> ptdfsZsByBranch,
                             final FlowBasedDomainDocument fbDomainDoc) {
        this.vertices = vertices;
        // global = in regard to the whole zone, france excluded
        this.frenchMarketGlobalNetPosition = BigDecimal.valueOf(refProg.getGlobalNetPosition(RTE_CODE));
        this.ptdfsZsByBranch = ptdfsZsByBranch;
        this.criticalBranches = new ArrayList<>();

        // extract relevant critical branches from the domain document
        for (final FlowBasedDomainTimeSeriesType timeSerie : fbDomainDoc.getFlowBasedDomainTimeSeries()) {
            timeSerie.getPeriod()
                .stream()
                .map(PeriodType::getInterval)
                .forEach(intervals -> intervals
                    .stream()
                    .map(IvaVolumesManager::getCriticalBranchesFromInterval)
                    .forEach(this.criticalBranches::addAll));
        }
    }

    public Map<String, BigDecimal> computeIvaVolumes(final double riskMarginInMW) {
        final Map<String, BigDecimal> idToIva = new HashMap<>();
        for (final CriticalBranchType branch : this.criticalBranches) {
            final BigDecimal frmWithRisk = getFrm(branch).add(BigDecimal.valueOf(riskMarginInMW));
            final BigDecimal iva = this.vertices.stream()
                .filter(vertex -> getMarginFromMarket(branch, vertex).compareTo(frmWithRisk) >= 0)
                .map(vertex -> getIvaFromRao(vertex, branch))
                .max(BigDecimal::compareTo)
                .orElse(ZERO);

            idToIva.put(branch.getId(), iva);
        }
        return idToIva;
    }

    private BigDecimal getIvaFromRao(final Vertex vertex,
                                     final CriticalBranchType cnei) {
        // PLACEHOLDER, TO BE DEFINED
        return BigDecimal.TEN;
    }

    private static List<CriticalBranchType> getCriticalBranchesFromInterval(final IntervalType interval) {
        final List<CriticalBranchType> result = new ArrayList<>();

        for (final FlowBasedDomainType domain : interval.getFlowBasedDomain()) {
            if (domain.getConstraintResults() != null
                && domain.getConstraintResults().getConstraintResult() != null) {
                domain.getConstraintResults()
                    .getConstraintResult()
                    .stream()
                    .map(ConstResultType::getCriticalBranch)
                    .filter(IvaVolumesManager::isFrenchOrigin)
                    .forEach(result::add);
            }
        }
        return result;
    }

    private static boolean isFrenchOrigin(final CriticalBranchType criticalBranch) {
        return FRENCH.equals(criticalBranch.getTsoOrigin());
    }

    /**
     * ∆F∆NP = PTDFZS * (NP_Xi – NP_RefProg), the flow discrepancy caused by the shift from RefProg to the vertex Xi
     *
     * @param criticalBranch the critical branch for which we want to select the Zone-To-Slack PTDF
     * @param vertexNP       the vertex net position (NP_Xi)
     * @return ∆F∆NP
     */
    private BigDecimal getFlowGap(final CriticalBranchType criticalBranch,
                                  final BigDecimal vertexNP) {
        return ptdfsZsByBranch.getOrDefault(criticalBranch.getId(), ZERO)
            .multiply(vertexNP.subtract(frenchMarketGlobalNetPosition));
    }

    /**
     * RAM_Xi,CNEi = RAM_RefProg,CNEi – ∆F∆NP(NP_Xi)
     *
     * @param criticalBranch the branch for which we want to calculate the margin
     * @param vertex         the vertex to use for calculation
     * @return RAM_Xi,CNEi
     */
    private BigDecimal getMarginFromMarket(final CriticalBranchType criticalBranch,
                                           final Vertex vertex) {
        final BigDecimal ramRefProg = BigDecimal.valueOf(criticalBranch.getFMax()
                                                         - criticalBranch.getFRef()
                                                         - criticalBranch.getFrmMw());

        final BigDecimal frenchPosVertex = BigDecimal.valueOf(Optional.ofNullable(vertex.coordinates().get(FRENCH))
                                                                  .orElseThrow());

        return ramRefProg.subtract(getFlowGap(criticalBranch, frenchPosVertex));
    }

    private static BigDecimal getFrm(final CriticalBranchType criticalBranch) {
        return BigDecimal.valueOf(FRM_MARGIN_PERCENTAGE * criticalBranch.getFMax());
    }

}
