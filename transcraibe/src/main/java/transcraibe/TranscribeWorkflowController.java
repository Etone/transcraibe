package transcraibe;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import transcraibe.summarize.SummarizeService;

@Controller
public class TranscribeWorkflowController {

    private final Logger log = LoggerFactory.getLogger(TranscribeWorkflowController.class);

    private final SummarizeService summarizer;

    @Inject
    public TranscribeWorkflowController(SummarizeService summarizer) {
        this.summarizer = summarizer;
    }

    @Post(value = "/summarize", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
    public Mono<String> summarize(@Body String transcript) {
        return summarizer.summarize(transcript);
    }

}
