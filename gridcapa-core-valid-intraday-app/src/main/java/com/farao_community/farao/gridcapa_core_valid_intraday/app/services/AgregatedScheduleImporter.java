/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import _351.iec62325.tc57wg16._451_2.scheduledocument._5._1.Point;
import _351.iec62325.tc57wg16._451_2.scheduledocument._5._1.ScheduleMarketDocument;
import _351.iec62325.tc57wg16._451_2.scheduledocument._5._1.TimeSeries;
import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Amira Kahya {@literal <amira.kahya at rte-france.com>}
 */
public final class AgregatedScheduleImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgregatedScheduleImporter.class);

    private AgregatedScheduleImporter() {
        // Utility class
    }

    public static Map<String, BigDecimal> importAndExtractHourlyPn(final InputStream inputStream, final OffsetDateTime targetProcessDateTime) {
        ScheduleMarketDocument schedule = importAgregatedSchedule(inputStream);
        return extractHourlyPnFromScheduleDocument(schedule, targetProcessDateTime);
    }

    private static ScheduleMarketDocument importAgregatedSchedule(final InputStream inputStream) {
        try {
            final JAXBContext context = JAXBContext.newInstance(ScheduleMarketDocument.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final JAXBElement<ScheduleMarketDocument> root =
                    unmarshaller.unmarshal(
                            new StreamSource(inputStream),
                            ScheduleMarketDocument.class
                    );

            return root.getValue();
        } catch (final Exception e) {
            throw new CoreValidIntradayInvalidDataException("Cannot unmarshal ScheduleMarketDocument", e);
        }
    }

    private static Map<String, BigDecimal> extractHourlyPnFromScheduleDocument(final ScheduleMarketDocument schedule, final OffsetDateTime targetProcessDateTime) {
        if (schedule == null || schedule.getTimeSeries() == null) {
            LOGGER.warn("Schedule or TimeSeries is null.");
            return Collections.emptyMap();
        }

        return schedule.getTimeSeries().stream()
                .filter(Objects::nonNull)
                .flatMap(ts -> {
                    try {
                        List<Point> hourlyPoints = findPointsForTargetHour(ts, targetProcessDateTime);
                        BigDecimal constrainedPn = computeConstrainedPnFromPoints(hourlyPoints);
                        return Stream.of(new AbstractMap.SimpleEntry<>(ts.getMRID(), constrainedPn));
                    } catch (final Exception e) {
                        LOGGER.error("Skipping TimeSeries {} due to exception: {}", ts.getMRID(), e.getMessage(), e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static List<Point> findPointsForTargetHour(final TimeSeries ts, final OffsetDateTime targetProcessDateTime) {

        OffsetDateTime periodStart = OffsetDateTime.parse(
                ts.getPeriod().getFirst().getTimeInterval()
                        .getStart()
        );

        OffsetDateTime hourStart = targetProcessDateTime
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        OffsetDateTime hourEnd = hourStart.plusHours(1);

        return ts.getPeriod().getFirst().getPoint().stream()
                .filter(p -> {
                    OffsetDateTime pointDateTime =
                            periodStart.plusMinutes(
                                    15 * (p.getPosition() - 1)
                            );
                    return !pointDateTime.isBefore(hourStart) && pointDateTime.isBefore(hourEnd);
                })
                .toList();
    }

    private static BigDecimal computeConstrainedPnFromPoints(final List<Point> points) {

        if (points == null || points.size() != 4) {
            throw new CoreValidIntradayInvalidDataException(
                    "Invalid hourly period: expected 4 points but found " + points.size()
            );
        }

        return points.stream()
                .map(Point::getQuantity)
                .max(Comparator.comparing(BigDecimal::abs))
                .orElseThrow();
    }
}
