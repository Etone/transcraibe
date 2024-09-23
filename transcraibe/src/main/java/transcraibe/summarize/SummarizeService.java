package transcraibe.summarize;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import transcraibe.config.OpenAIModelConfig;

import java.util.ArrayList;
import java.util.List;


@Singleton
public class SummarizeService {

    private final OpenAIModelConfig modelConfig;
    private final OpenAIAsyncClient client;

    Logger log = LoggerFactory.getLogger(SummarizeService.class);

    private static final String SUMMARY_SYSTEM_PROMT = """
            You are a helpful assistant build to summarize and help answer questions about a podcast episode.
            You always summarize the provided user text and nothing else. The summary always follows to following format

            1. Wie lautet der Titel der Folge?
            Generate a Title of the Episode here. The title has the following format:
            TechSnippet #xxx: [A question you generate] - mit [The name of the quest]

            2. Wie lautet der Untertitel der Folge?
            Generate a subtitle here

            3. Thema/Beschreibung des Inhalts
            Generate a small summary here

            4. Relevanz
            Answer the question, who might want to hear this episode of the podcast? Which kind of professionals could profit from this episode

            5. Mehrwert
            Answer the question "Whats the value delivered to the targeted audience?"

            6. Gibt es wichtige Keywords?
            Generate a list of at most 10 Keywords for this episode

            The provided text is always a transcript of a podcast episode.
            An Entry in this transcript has the following form:
            first line: start and end of this entry as timestamp
            second line: Speaker who is speaking
            third line: The transcribed audio
            Entries are seperated by a blank line

            You always answer in german!
            """;

    @Inject
    public SummarizeService(OpenAIModelConfig modelConfig, OpenAIAsyncClient client) {
        this.modelConfig = modelConfig;
        this.client = client;
    }

    public Mono<String> summarize(String transcript) {
        log.info("Summarizing Transcript");
        log.debug("Transcript = {}", transcript);

        var context = prepareContext(transcript);

        var completion = client.getChatCompletions(modelConfig.gpt(), new ChatCompletionsOptions(context));

        return completion.map(ChatCompletions::getChoices)
                .map(list -> list.stream().findFirst().orElseThrow().getMessage().getContent());
    }

    private static List<ChatRequestMessage> prepareContext(String transcript) {
        var messages = new ArrayList<ChatRequestMessage>();
        messages.add(new ChatRequestSystemMessage(SUMMARY_SYSTEM_PROMT));
        messages.add(new ChatRequestUserMessage(transcript));
        return messages;
    }
}
