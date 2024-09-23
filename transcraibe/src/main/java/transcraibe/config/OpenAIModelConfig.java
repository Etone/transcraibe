package transcraibe.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;

@ConfigurationProperties("azure.openai.model")
public record OpenAIModelConfig(
        @NonNull String gpt,
        @NonNull String whisper
) {
}