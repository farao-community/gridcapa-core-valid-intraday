/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_id.api.exception;
import com.farao_community.farao.gridcapa_core_valid_id.api.JsonApiConverter;

/**
 * Custom abstract exception to be extended by all application exceptions.
 * Any subclass may be automatically wrapped to a JSON API error message if needed
 *
 * @see JsonApiConverter
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel at rte-france.com>}
 */
public abstract class AbstractCoreValidIdException extends RuntimeException {

    /**
     * Creates a new AbstractCoreValidIdException with the specified detail message.
     *
     * @param message the detail message describing the error condition
     */
    protected AbstractCoreValidIdException(final String message) {
        super(message);
    }

    /**
     * Creates a new AbstractCoreValidIdException with the given detail message and cause.
     *
     * @param message   the detail message describing the error
     * @param throwable the cause of this exception, or {@code null} if none
     */
    protected AbstractCoreValidIdException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
 * HTTP status code associated with this exception.
 *
 * @return the HTTP-like status code representing the error condition (for example 400, 404, 500)
 */
public abstract int getStatus();

    /**
 * Provides an application-specific error code that identifies the error condition.
 *
 * @return the error code string that identifies this exception's specific error condition
 */
public abstract String getCode();

    /**
     * Provide the exception title derived from the exception message.
     *
     * @return the exception message used as the title
     */
    public final String getTitle() {
        return getMessage();
    }

    /**
     * Builds a detailed message that includes this exception's message and its nested cause.
     *
     * <p>If there is no cause, returns the exception message. If there is a cause, returns the
     * message followed by "nested exception is " and the cause; if the message is null, only the
     * nested-cause text is returned.
     *
     * @return the detail string for this exception, including nested cause information
     */
    public final String getDetails() {
        String message = getMessage();
        Throwable cause = getCause();
        if (cause == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(64);
        if (message != null) {
            sb.append(message).append("; ");
        }
        sb.append("nested exception is ").append(cause);
        return sb.toString();
    }
}