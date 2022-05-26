package io.github.sskorol.testcases;

import io.github.sskorol.config.XmlConfig;
import lombok.Data;

@Data
public class MetaDataContainer {
    private final boolean isDriverCreated;
    private final boolean isWebDriverWaitCreated;
    private final boolean isChromeDevToolsServiceCreated;
    private final XmlConfig config;
}
