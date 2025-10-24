/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.gridcapa_core_valid_intraday.app.services;

import com.farao_community.farao.gridcapa_core_valid_intraday.api.exception.CoreValidIntradayInvalidDataException;
import com.farao_community.farao.gridcapa_core_valid_intraday.app.configuration.UrlWhitelistConfiguration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.StringJoiner;

/**
 * @author Marc Schwitzguebel {@literal <marc.schwitzguebel_external at rte-france.com>}
 */
@Component
public class UrlValidationService {
    private final UrlWhitelistConfiguration urlWhitelistConfiguration;

    public UrlValidationService(UrlWhitelistConfiguration urlWhitelistConfiguration) {
        this.urlWhitelistConfiguration = urlWhitelistConfiguration;
    }

    public InputStream openUrlStream(String urlString) {
        if (urlWhitelistConfiguration.getWhitelist().stream().noneMatch(urlString::startsWith)) {
            StringJoiner sj = new StringJoiner(", ", "Whitelist: ", ".");
            urlWhitelistConfiguration.getWhitelist().forEach(sj::add);
            throw new CoreValidIntradayInvalidDataException(String.format("URL '%s' is not part of application's whitelisted url's %s", urlString, sj));
        }
        try {
            URL url = new URI(urlString).toURL();
            return url.openStream();
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            throw new CoreValidIntradayInvalidDataException(String.format("Cannot download FileResource file from URL '%s'", urlString), e);
        }
    }
}
