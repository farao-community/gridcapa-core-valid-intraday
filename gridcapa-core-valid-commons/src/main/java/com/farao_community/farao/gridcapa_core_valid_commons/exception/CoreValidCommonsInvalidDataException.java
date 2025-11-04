/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_valid_commons.exception;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_externe at rte-france.com>}
 */
public class CoreValidCommonsInvalidDataException extends RuntimeException  {

    private static final int STATUS = 400;
    private static final String CODE = "400-InvalidDataException";

    public CoreValidCommonsInvalidDataException(final String message) {
        super(message);
    }

    public CoreValidCommonsInvalidDataException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public int getStatus() {
        return STATUS;
    }

    public String getCode() {
        return CODE;
    }
}
