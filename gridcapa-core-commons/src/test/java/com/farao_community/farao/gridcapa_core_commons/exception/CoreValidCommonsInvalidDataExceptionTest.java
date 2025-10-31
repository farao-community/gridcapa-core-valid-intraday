/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.farao_community.farao.gridcapa_core_commons.exception;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoreValidCommonsInvalidDataExceptionTest {

    @Test
    void getStatus() {
        CoreValidCommonsInvalidDataException commonsInvalidDataException = new CoreValidCommonsInvalidDataException("test message");
        Assertions.assertThat(commonsInvalidDataException)
                .isNotNull()
                .hasNoCause()
                .hasMessage("test message");
        Assertions.assertThat(commonsInvalidDataException.getStatus()).isEqualTo(400);
    }

    @Test
    void getCode() {
        final RuntimeException anotherCause = new RuntimeException("another cause");
        CoreValidCommonsInvalidDataException commonsInvalidDataException = new CoreValidCommonsInvalidDataException("test message 2", anotherCause);
        Assertions.assertThat(commonsInvalidDataException)
                .isNotNull()
                .hasCause(anotherCause)
                .hasMessage("test message 2");
        Assertions.assertThat(commonsInvalidDataException.getCode()).isEqualTo("400-InvalidDataException");
    }
}
