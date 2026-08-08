package ca.cabbit.fieldservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String jwtSecret,
        long accessTokenMinutes,
        long refreshTokenDays,
        String corsOrigins,
        String minioEndpoint,
        String minioAccessKey,
        String minioSecretKey,
        String minioBucket,
        long maxUploadBytes,
        boolean production) {}

