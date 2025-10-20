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
public class CoreValidIdInternalException extends AbstractCoreValidIdException {
    private static final int STATUS = 500;
    private static final String CODE = "500-InternalException";

    /**
     * Creates a new CoreValidIdInternalException with the specified detail message.
     *
     * @param message detailed description of the internal error
     */
    public CoreValidIdInternalException(final String message) {
        super(message);
    }

    /**
     * Creates a CoreValidIdInternalException with the specified detail message and cause.
     *
     * @param message the detail message describing the internal error
     * @param throwable the underlying cause of this exception
     */
    public CoreValidIdInternalException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Get the HTTP status code associated with this exception.
     *
     * @return the HTTP status code for this exception (500)
     */
    @Override
    public int getStatus() {
        return STATUS;
    }

    /**
     * Provides the standardized error code for this exception.
     *
     * @return the error code string identifying the exception, for example "500-InternalException"
     */
    @Override
    public String getCode() {
        return CODE;
    }
}