/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.domain;

import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.openrao.data.crac.io.fbconstraint.FbConstraintCreationContext;

import java.util.Map;

public record ShiftStudyPointData(Map<String, Double> netPositions,
                                  ZonalData<Scalable> scalableZonalData,
                                  FbConstraintCreationContext fbConstraintCreationContext,
                                  String jsonCracUrl,
                                  String raoParametersUrl) {
}
