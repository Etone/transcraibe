package transcraibe.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("azure.openai.client")
public record OpenAIClientConfigProperties(
        String key,
        String endpoint
) {
}
