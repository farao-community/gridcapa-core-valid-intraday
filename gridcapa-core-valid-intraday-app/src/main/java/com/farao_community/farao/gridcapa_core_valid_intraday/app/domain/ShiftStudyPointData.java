package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;

import java.util.Map;

public record ShiftStudyPointData(Network network,
                                  Map<String, Double> coreNetPositions,
                                  ZonalData<Scalable> scalableZonalData,
                                  FbConstraintCreationContext fbConstraintCreationContext,
                                  String jsonCracUrl,
                                  String raoParametersUrl) {
}
