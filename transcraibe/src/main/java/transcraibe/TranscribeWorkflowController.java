package transcraibe;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import transcraibe.summarize.SummarizeService;
import transcraibe.transcribe.TranscribeService;

@Controller
public class TranscribeWorkflowController {

    private final Logger log = LoggerFactory.getLogger(TranscribeWorkflowController.class);

    private final TranscribeService transcriber;
    private final SummarizeService summarizer;

    @Inject
    public TranscribeWorkflowController(TranscribeService transcriber, SummarizeService summarizer) {
        this.transcriber = transcriber;
        this.summarizer = summarizer;
    }

    @Post(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    @ExecuteOn(TaskExecutors.BLOCKING)
    public String transcribe(StreamingFileUpload episode) {
        log.info("Transcribing episode");
        var stream = episode.asInputStream();
        return transcriber.transcribe(stream);
    }

    @Post(value = "/summarize", consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
    public Mono<String> summarize(@Body String transcript) {
        log.info("Summarize transcript");
        return summarizer.summarize(transcript);
    }

}
