package com.lcx.mcp.server.csdn.types.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "csdn.api")
public class CSDNApiProperties {

    private String cookie;

    private String categories;

}
