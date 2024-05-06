package com.gl.eirs.iplocationprocessor.config;



import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {

    @Value("${file.separator.parameter}")
    private String fileSeparator;

    @Value("${delta.file.path}")
    String filePath;

    @Value("${ipType}")
    String ipType;

    @Value("${alert.url}")
    String alertUrl;

    @Value("${error.file.path}")
    String errorFilePath;
}
