/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.dichotomy;

import com.farao_community.farao.dichotomy.api.exceptions.GlskLimitationException;
import com.farao_community.farao.dichotomy.api.exceptions.ShiftingException;
import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.domain.ShiftStudyPointData;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.utils.TestUtils;
import com.powsybl.glsk.api.GlskDocument;
import com.powsybl.glsk.api.io.GlskDocumentImporters;
import com.powsybl.glsk.commons.ZonalData;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreValidNetworkShifterTest {

    @Test
    void  shiftNetworkTest() throws GlskLimitationException, ShiftingException {
        final OffsetDateTime offsetDateTime = OffsetDateTime.of(2019, 1, 8, 2, 0, 0, 0, ZoneOffset.UTC);
        TestUtils.TestCoreHubReducedRealConf coreHubConf = new TestUtils.TestCoreHubReducedRealConf();
        Vertex vertex = new Vertex(1, Map.of("FR", 1200, "BE", 2600, "DE", 3500));
        ShiftStudyPointData shiftStudyPointData = mock(ShiftStudyPointData.class);
        final GlskDocument glskDocument = GlskDocumentImporters.importGlsk(Objects.requireNonNull(getClass().getResourceAsStream("/shift/glsk_proportional_12nodes_hvdc.xml")));
        when(shiftStudyPointData.netPositions()).thenReturn(Map.of("10YFR-RTE------C", 2000.0, "10YBE----------2", 6000.0, "10YCB-GERMANY--8", 8000.0));

        CoreValidNetworkShifter coreValidNetworkShifter = new CoreValidNetworkShifter(coreHubConf, vertex, shiftStudyPointData, LoadFlowParameters.load());

        final Network network = Network.read("network_12nodes_disconnected_hvdc.uct", Objects.requireNonNull(getClass().getResourceAsStream("/shift/network_12nodes_disconnected_hvdc.uct")));
        final ZonalData<Scalable> zonalScalable = glskDocument.getZonalScalable(network, offsetDateTime.toInstant());

        final Optional<Double> sumBeforeShift = network.getGeneratorStream().map(Generator::getTargetP).reduce(Double::sum);
        when(shiftStudyPointData.scalableZonalData()).thenReturn(zonalScalable);
        final VariantManager variantManager = network.getVariantManager();
        final String workingVariantId = variantManager.getWorkingVariantId();
        final String scaledBy1 = workingVariantId + "-ScaledBy-1";
        variantManager.cloneVariant(workingVariantId, scaledBy1);
        variantManager.setWorkingVariant(scaledBy1);
        coreValidNetworkShifter.shiftNetwork(1.0, network);
        final Optional<Double> sumAfterFirstShift = network.getGeneratorStream().map(Generator::getTargetP).reduce(Double::sum);
        assertThat(sumBeforeShift.isPresent()).isTrue();
        assertThat(sumAfterFirstShift.isPresent()).isTrue();
        assertThat(sumAfterFirstShift.get()).isEqualTo(sumBeforeShift.get() - 8700.0, offset(0.001));

        final String scaledBy05 = workingVariantId + "-ScaledBy-0-5";
        variantManager.cloneVariant(workingVariantId, scaledBy05);
        variantManager.setWorkingVariant(scaledBy05);
        coreValidNetworkShifter.shiftNetwork(0.5, network);
        final Optional<Double> sumAfterSecondShift = network.getGeneratorStream().map(Generator::getTargetP).reduce(Double::sum);
        assertThat(sumAfterSecondShift.isPresent()).isTrue();
        assertThat(sumAfterSecondShift.get()).isEqualTo(sumBeforeShift.get() - 12350.0, offset(0.001));

    }

}
