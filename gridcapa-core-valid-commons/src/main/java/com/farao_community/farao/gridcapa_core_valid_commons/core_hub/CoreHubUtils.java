package com.farao_community.farao.gridcapa_core_valid_commons.core_hub;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class CoreHubUtils {
    private CoreHubUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, String> getFlowBasedToVertexCodeMap(final List<CoreHub> coreHubs) {
        return coreHubs.stream().collect(toMap(CoreHub::flowbasedCode,
                                               CoreHub::clusterVerticeCode));
    }
}
