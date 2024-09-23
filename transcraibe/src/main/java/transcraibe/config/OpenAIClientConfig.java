package transcraibe.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Factory
public class OpenAIClientConfig {

    OpenAIClientConfigProperties config;

    @Inject
    public OpenAIClientConfig(OpenAIClientConfigProperties config) {
        this.config = config;
    }

    @Singleton
    public OpenAIClient blockingClient() {
        return new OpenAIClientBuilder().endpoint(config.endpoint()).credential(new AzureKeyCredential(config.key())).buildClient();
    }

    @Singleton
    public OpenAIAsyncClient asyncClient() {
        return new OpenAIClientBuilder().endpoint(config.endpoint()).credential(new AzureKeyCredential(config.key())).buildAsyncClient();
    }
}
