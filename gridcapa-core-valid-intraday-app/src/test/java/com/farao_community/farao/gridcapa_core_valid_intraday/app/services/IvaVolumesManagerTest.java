package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_commons.vertex.Vertex;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.resource.CoreValidIntradayFileResource;
import com.farao_community.gridcapa_core_valid_intraday.xsd.f645.FlowBasedDomainDocument;
import com.powsybl.openrao.data.refprog.referenceprogram.ReferenceProgram;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IvaVolumesManagerTest {

    @Autowired
    private FileImporter fileImporter;
    private static final OffsetDateTime TEST_DATE_TIME = OffsetDateTime.parse("2021-07-22T22:30Z");

    private CoreValidIntradayFileResource createFileResource(final String filename, final URL resource) {
        return new CoreValidIntradayFileResource(filename, resource.toExternalForm());
    }

    @Test
    void shouldHaveZeroIfRaoNotCalled() {
        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", getClass().getResource("/20210723-FID2-632-v2-10V1001C--00264T-to-10V1001C--00085T.xml"));
        final ReferenceProgram referenceProgram = fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME);
        final CoreValidIntradayFileResource cnecRamFile = createFileResource("cnecRam", getClass().getResource("/20260119-0000-FID2-645-INIT_VIRG_REFBAL_PRES_FBPARAMS-v3.xml"));
        final FlowBasedDomainDocument flowBasedDomainDocument = fileImporter.importCnecRamFile(cnecRamFile);
        final IvaVolumesManager mgr = new IvaVolumesManager(List.of(getTestVertex(2000)), referenceProgram, Map.of(
            "CCCCCCCCCCCC", BigDecimal.valueOf(0.1),
            "fb2", BigDecimal.valueOf(0.2),
            "fb3", BigDecimal.valueOf(0.3)
        ), flowBasedDomainDocument);
        assertThat(mgr.computeIvaVolumes(100)).isNotEmpty();
        assertThat(mgr.computeIvaVolumes(100)).containsValue(ZERO);
        // add test call to mock RAO when service exists
    }

    @Test
    void shouldNotHaveZeroIfRaoCalled() {
        final CoreValidIntradayFileResource refProgFile = createFileResource("refprog", getClass().getResource("/20210723-FID2-632-v2-10V1001C--00264T-to-10V1001C--00085T.xml"));
        final ReferenceProgram referenceProgram = fileImporter.importReferenceProgram(refProgFile, TEST_DATE_TIME);
        final CoreValidIntradayFileResource cnecRamFile = createFileResource("cnecRam", getClass().getResource("/20260119-0000-FID2-645-INIT_VIRG_REFBAL_PRES_FBPARAMS-v3.xml"));
        final FlowBasedDomainDocument flowBasedDomainDocument = fileImporter.importCnecRamFile(cnecRamFile);
        final IvaVolumesManager mgr = new IvaVolumesManager(List.of(getTestVertex(-300000)), referenceProgram, Map.of(
            "CCCCCCCCCCCC", BigDecimal.valueOf(1000),
            "fb2", BigDecimal.valueOf(20),
            "fb3", BigDecimal.valueOf(30)
        ), flowBasedDomainDocument);
        assertThat(mgr.computeIvaVolumes(100)).isNotEmpty();
        assertThat(mgr.computeIvaVolumes(100)).doesNotContainValue(ZERO);
        // add test call to mock RAO when service exists
    }

    private Vertex getTestVertex(final int frValue) {
        return new Vertex(1, Map.of("FR", frValue, "BE", -1000, "AT", 500));
    }

}
