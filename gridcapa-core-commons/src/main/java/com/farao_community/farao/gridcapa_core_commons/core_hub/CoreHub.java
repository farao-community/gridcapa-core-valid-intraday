/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.gridcapa_core_commons.core_hub;

public record CoreHub(String name, String ramcep2Code, String flowbasedCode, String forecastCode, String clusterVerticeCode, boolean isHvdcHub, double coefficient) {
}
