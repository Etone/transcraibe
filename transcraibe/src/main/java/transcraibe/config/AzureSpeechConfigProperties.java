package transcraibe.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("azure.speech")
public record AzureSpeechConfigProperties(
        String region,
        String key
) {
}
