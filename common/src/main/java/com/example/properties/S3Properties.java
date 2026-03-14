package com.example.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3Properties {

    private String bucket;
    private String region;
    private Map<String, String> folders;

}