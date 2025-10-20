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
public class CoreValidIdInvalidDataException extends AbstractCoreValidIdException {

    private static final int STATUS = 400;
    private static final String CODE = "400-InvalidDataException";

    /**
     * Creates a CoreValidIdInvalidDataException with the specified detail message.
     *
     * The exception represents an invalid-data error with status 400 and code "400-InvalidDataException".
     *
     * @param message the detail message describing the invalid data error
     */
    public CoreValidIdInvalidDataException(final String message) {
        super(message);
    }

    /**
     * Creates a CoreValidIdInvalidDataException with a detail message and cause.
     *
     * @param message   the detail message describing the invalid data condition
     * @param throwable the underlying cause of this exception
     */
    public CoreValidIdInvalidDataException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Provide the HTTP status code associated with this exception.
     *
     * @return the HTTP status code for this exception (400)
     */
    @Override
    public int getStatus() {
        return STATUS;
    }

    /**
     * Provide the fixed error code for this exception.
     *
     * @return the error code string identifying this exception, e.g. "400-InvalidDataException"
     */
    @Override
    public String getCode() {
        return CODE;
    }
}