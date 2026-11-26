/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInternalException;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.raoapi.json.JsonRaoParameters;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
public class FileExporter {

    public static final String ARTIFACTS_S = "artifacts/%s";
    private static final String RAO_PARAMETERS_FILE_NAME = "raoParameters.json";
    private static final DateTimeFormatter NETWORK_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm_'network.xiidm'");
    private static final String XIIDM = "XIIDM";
    private static final String TARGET_PROCESS = "IDCC";
    private static final String MINIO_SEPARATOR = "/";
    private static final String ZONE_ID = "Europe/Paris";
    private static final String XIIDM_EXTENSION = "xiidm";
    private static final String JSON = "JSON";

    private final MinioAdapter minioAdapter;

    public FileExporter(final MinioAdapter minioAdapter) {
        this.minioAdapter = minioAdapter;
    }

    public void saveNetworkForTimestamp(final Network network, final OffsetDateTime targetDateTime) {
        try (final InputStream xiidm = getNetworkInputStreamInXiidmFormat(network)) {
            minioAdapter.uploadArtifactForTimestamp(XIIDM + MINIO_SEPARATOR + NETWORK_FORMATTER.format(targetDateTime), xiidm, TARGET_PROCESS, "", targetDateTime);
        } catch (IOException e) {
            throw new CoreValidIntradayInternalException("Could not export network XIIDM file", e);
        }
    }

    public String saveNetworkInArtifact(final Network network, final String networkFilePath, final String fileType, final OffsetDateTime processTargetDateTime) {
        try (final InputStream is = getNetworkInputStreamInXiidmFormat(network)) {
            minioAdapter.uploadArtifactForTimestamp(networkFilePath, is, TARGET_PROCESS, fileType, processTargetDateTime);
        } catch (IOException e) {
            throw new CoreValidIntradayInternalException("Error while trying to save network into artefact", e);
        }
        return minioAdapter.generatePreSignedUrl(networkFilePath);
    }

    private InputStream getNetworkInputStreamInXiidmFormat(final Network network) throws IOException {
        final MemDataSource memDataSource = new MemDataSource();
        network.write(XIIDM, new Properties(), memDataSource);
        return memDataSource.newInputStream("", XIIDM_EXTENSION);
    }

    public String saveRaoParametersAndGetUrl(final RaoParameters raoParameters) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonRaoParameters.write(raoParameters, baos, ReportNode.NO_OP);
        final String raoParametersDestinationPath = String.format(ARTIFACTS_S, RAO_PARAMETERS_FILE_NAME);
        final ByteArrayInputStream inStream = new ByteArrayInputStream(baos.toByteArray());
        minioAdapter.uploadArtifact(raoParametersDestinationPath, inStream);
        return minioAdapter.generatePreSignedUrl(raoParametersDestinationPath);
    }

    public String saveCracInJsonFormat(final Crac crac, final OffsetDateTime timestamp) {
        final MemDataSource memDataSource = new MemDataSource();
        final String jsonCracFileName = String.format("crac_%s.json", removeIllegalCharacter(timestamp.toString()));
        try (OutputStream os = memDataSource.newOutputStream(jsonCracFileName, false)) {
            crac.write(JSON, os);
        } catch (IOException e) {
            throw new CoreValidIntradayInternalException("Error while trying to save converted CRAC file.", e);
        }
        final String cracPath = String.format(ARTIFACTS_S, jsonCracFileName);
        try (InputStream is = memDataSource.newInputStream(jsonCracFileName)) {
            minioAdapter.uploadArtifact(cracPath, is);
        } catch (IOException e) {
            throw new CoreValidIntradayInternalException("Error while trying to upload converted CRAC file.", e);
        }
        return minioAdapter.generatePreSignedUrl(cracPath);
    }

    private String removeIllegalCharacter(final String url) {
        return url.replace(":", "");
    }

    public String makeDestinationMinioPath(final OffsetDateTime offsetDateTime, final String fileType) {
        final ZonedDateTime targetDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of(ZONE_ID));
        return TARGET_PROCESS + MINIO_SEPARATOR
               + targetDateTime.getYear() + MINIO_SEPARATOR
               + String.format("%02d", targetDateTime.getMonthValue()) + MINIO_SEPARATOR
               + String.format("%02d", targetDateTime.getDayOfMonth()) + MINIO_SEPARATOR
               + String.format("%02d", targetDateTime.getHour()) + "_30" + MINIO_SEPARATOR
               + fileType + MINIO_SEPARATOR;
    }
}
