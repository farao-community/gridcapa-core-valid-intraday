/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.farao_community.farao.gridcapa_core_valid_id.api.exception;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public class CoreValidIdRaoException extends AbstractCoreValidIdException {
    private static final int STATUS = 500;
    private static final String CODE = "500-RaoException";

    /**
     * Creates a CoreValidIdRaoException with the specified detail message.
     *
     * @param message the detail message describing the Rao-related error
     */
    public CoreValidIdRaoException(final String message) {
        super(message);
    }

    /**
     * Create a CoreValidIdRaoException with a detail message and cause.
     *
     * @param message   the detail message explaining the error
     * @param throwable the underlying cause of this exception, or {@code null} if none
     */
    public CoreValidIdRaoException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Provides the HTTP status code associated with this exception.
     *
     * @return the numeric HTTP status code (500)
     */
    @Override
    public int getStatus() {
        return STATUS;
    }

    /**
     * Provides the standardized error code for this exception.
     *
     * @return the error code associated with this exception
     */
    @Override
    public String getCode() {
        return CODE;
    }
}