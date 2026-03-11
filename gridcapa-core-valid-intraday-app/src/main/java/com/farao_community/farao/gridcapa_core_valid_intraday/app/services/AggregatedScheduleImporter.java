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
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Amira Kahya {@literal <amira.kahya at rte-france.com>}
 */
public final class AggregatedScheduleImporter {
    private static final String FR_EIC = "10YFR-RTE------C";
    private static final JAXBContext JAXB_CONTEXT = initJaxbContext();

    private AggregatedScheduleImporter() {
        // Utility class
    }

    public static BigDecimal calculateFrenchNetPositionFromScheduleMarketDocument(final InputStream inputStream, final OffsetDateTime targetProcessDateTime) {
        final ScheduleMarketDocument schedule = importAggregatedSchedule(inputStream);
        return getHighestFrenchNetPositionQuantity(schedule, targetProcessDateTime);
    }

    private static ScheduleMarketDocument importAggregatedSchedule(final InputStream inputStream) {
        try {
            final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
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

    private static BigDecimal getHighestFrenchNetPositionQuantity(final ScheduleMarketDocument schedule, final OffsetDateTime targetProcessDateTime) {
        if (schedule == null || schedule.getTimeSeries() == null) {
            throw new CoreValidIntradayInvalidDataException("Schedule or TimeSeries is null.");
        }
        final Map<Integer, BigDecimal> totalQuantityByPosition = new LinkedHashMap<>();
        for (final TimeSeries ts : schedule.getTimeSeries()) {
            if (ts != null) {
                final String inDomain = ts.getInDomainMRID() != null ? ts.getInDomainMRID().getValue() : null;
                final String outDomain = ts.getOutDomainMRID() != null ? ts.getOutDomainMRID().getValue() : null;
                if (FR_EIC.equals(inDomain) || FR_EIC.equals(outDomain)) {
                    final List<Point> hourlyPoints = findPointsForTargetHour(ts, targetProcessDateTime);
                    int sign = FR_EIC.equals(outDomain) ? 1 : -1;
                    for (final Point point : hourlyPoints) {
                        final BigDecimal quantity = point.getQuantity().multiply(BigDecimal.valueOf(sign));
                        totalQuantityByPosition.put(
                                point.getPosition(),
                                totalQuantityByPosition.getOrDefault(point.getPosition(), BigDecimal.ZERO).add(quantity)
                        );
                    }
                }
            }
        }
        return totalQuantityByPosition.values().stream()
                .max(Comparator.comparing(BigDecimal::abs))
                .orElseThrow(() -> new RuntimeException("No quantities found"));
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

    private static JAXBContext initJaxbContext() {
        try {
            return JAXBContext.newInstance(ScheduleMarketDocument.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
