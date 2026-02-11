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
public final class AggregatedScheduleImporter {

    private AggregatedScheduleImporter() {
        // Utility class
    }

    public static Map<String, BigDecimal> importAndExtractHourlyNetPositions(final InputStream inputStream, final OffsetDateTime targetProcessDateTime) {
        final ScheduleMarketDocument schedule = importAggregatedSchedule(inputStream);
        return extractHourlyNetPositionsFromScheduleDocument(schedule, targetProcessDateTime);
    }

    private static ScheduleMarketDocument importAggregatedSchedule(final InputStream inputStream) {
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

    private static Map<String, BigDecimal> extractHourlyNetPositionsFromScheduleDocument(final ScheduleMarketDocument schedule, final OffsetDateTime targetProcessDateTime) {
        if (schedule == null || schedule.getTimeSeries() == null) {
            throw new CoreValidIntradayInvalidDataException("Schedule or TimeSeries is null.");
        }

        return schedule.getTimeSeries().stream()
                .filter(Objects::nonNull)
                .flatMap(ts -> {
                            final List<Point> hourlyPoints = findPointsForTargetHour(ts, targetProcessDateTime);
                            final BigDecimal netPosition = computeMaxAbsoluteNetPositionFromPoints(hourlyPoints);
                            return Stream.of(new AbstractMap.SimpleEntry<>(ts.getMRID(), netPosition));
                        }
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static List<Point> findPointsForTargetHour(final TimeSeries ts, final OffsetDateTime targetProcessDateTime) {
        if (ts.getPeriod() == null || ts.getPeriod().isEmpty()) {
            throw new CoreValidIntradayInvalidDataException("Time series contains no periods.");
        }
        final OffsetDateTime periodStart = OffsetDateTime.parse(
                ts.getPeriod().getFirst().getTimeInterval()
                        .getStart()
        );
        final OffsetDateTime hourStart = targetProcessDateTime
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        final OffsetDateTime hourEnd = hourStart.plusHours(1);
        return ts.getPeriod().getFirst().getPoint() == null
                ? List.of()
                : ts.getPeriod().getFirst().getPoint().stream()
                .filter(p -> isPointWithinHour(periodStart, hourStart, hourEnd, p))
                .toList();
    }

    private static boolean isPointWithinHour(final OffsetDateTime periodStart, final OffsetDateTime hourStart, final OffsetDateTime hourEnd, final Point p) {
        final OffsetDateTime pointDateTime =
                periodStart.plusMinutes(
                        15L * (p.getPosition() - 1)
                );
        return !pointDateTime.isBefore(hourStart) && pointDateTime.isBefore(hourEnd);
    }

    private static BigDecimal computeMaxAbsoluteNetPositionFromPoints(final List<Point> points) {
        final int size = points == null ? 0 : points.size();
        if (size != 4) {
            throw new CoreValidIntradayInvalidDataException(
                    "Invalid hourly period: expected 4 points (15-minute intervals) but found " + size
            );
        }

        return points.stream()
                .map(Point::getQuantity)
                .max(Comparator.comparing(BigDecimal::abs))
                .orElseThrow();
    }
}
