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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ShiftStudyPointDataTest {

    private static final Map<String, Double> NET_POSITONS = Map.of("FR", 200.0, "DE", 400.0);
    private static final ZonalData<Scalable> SCALABLE = mock(ZonalData.class);
    private static final FbConstraintCreationContext GLSK = mock(FbConstraintCreationContext.class);
    private static final String JSON_CRAC_URL = "crac_url.json";
    private static final String RAO_URL = "rao parameters.json";

    @Test
    void shiftStudyPointDataTest() {
        final ShiftStudyPointData shiftStudyPointData = new ShiftStudyPointData(NET_POSITONS, SCALABLE, GLSK, JSON_CRAC_URL, RAO_URL);
        assertThat(shiftStudyPointData.netPositions()).isEqualTo(NET_POSITONS);
        assertThat(shiftStudyPointData.scalableZonalData()).isEqualTo(SCALABLE);
        assertThat(shiftStudyPointData.fbConstraintCreationContext()).isEqualTo(GLSK);
        assertThat(shiftStudyPointData.jsonCracUrl()).isEqualTo(JSON_CRAC_URL);
        assertThat(shiftStudyPointData.raoParametersUrl()).isEqualTo(RAO_URL);
    }
}
