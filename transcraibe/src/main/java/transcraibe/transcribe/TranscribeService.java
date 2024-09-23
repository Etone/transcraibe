package transcraibe.transcribe;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.audio.*;
import com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber;
import com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriptionCanceledEventArgs;
import com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriptionEventArgs;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transcraibe.config.AzureSpeechConfigProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

@Singleton
public class TranscribeService {

    private final AzureSpeechConfigProperties speechConfig;
    private static final Logger log = LoggerFactory.getLogger(TranscribeService.class);
    private static final Semaphore blockRecoginizerSemaphore = new Semaphore(0);

    private static final StringBuilder resultBuilder = new StringBuilder();

    @Inject
    public TranscribeService(AzureSpeechConfigProperties speechConfig) {
        this.speechConfig = speechConfig;
    }

    public String transcribe(InputStream audio) {

        try (var pushStream = AudioInputStream.createPushStream(
                AudioStreamFormat.getCompressedFormat(AudioStreamContainerFormat.MP3)
        );
             var config = SpeechConfig.fromSubscription(speechConfig.key(), speechConfig.region());

             var audioInput = AudioConfig.fromStreamInput(pushStream);
             var conversationTranscriber = setupTranscriber(config, audioInput, TranscribeService::onSuccess, TranscribeService::onCanceled)
        ) {

            conversationTranscriber.startTranscribingAsync().get();

            pushToStream(audio, pushStream);
            //Block waiting for completion
            blockRecoginizerSemaphore.acquire();
            conversationTranscriber.stopTranscribingAsync().get();

        } catch (ExecutionException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        return resultBuilder.toString();
    }

    private static void pushToStream(InputStream audio, PushAudioInputStream pushStream) throws IOException {
        // Fill PushStream from InputStream
        byte[] readBuffer = new byte[4096];

        int bytesRead;
        while ((bytesRead = audio.read(readBuffer)) != -1) {
            if (bytesRead == readBuffer.length) {
                pushStream.write(readBuffer);
            } else {
                // Last buffer read from the WAV file is likely to have less bytes
                pushStream.write(Arrays.copyOfRange(readBuffer, 0, bytesRead));
            }
        }
        // When everything is pushed, close this stream to make sure the transcription ends
        pushStream.close();
    }

    private ConversationTranscriber setupTranscriber(
            SpeechConfig config,
            AudioConfig audioInput,
            EventHandler<ConversationTranscriptionEventArgs> recognizedHandler,
            EventHandler<ConversationTranscriptionCanceledEventArgs> canceledHandler
    ) {
        config.setSpeechRecognitionLanguage("de-de");

        var transcriber = new ConversationTranscriber(config, audioInput);

        // Subscribes to events.
        transcriber.sessionStarted.addEventListener((_s, _e) -> log.debug("Session started"));
        transcriber.sessionStopped.addEventListener((_s, _e) -> log.debug("Session stopped"));

        transcriber.transcribed.addEventListener(recognizedHandler);
        transcriber.canceled.addEventListener(canceledHandler);

        return transcriber;
    }

    private static void onSuccess(Object _s, ConversationTranscriptionEventArgs event) {
        if (event.getResult().getReason() == ResultReason.RecognizedSpeech) {
            log.info("Recognized Speech");
            log.debug("Speaker = {}", event.getResult().getSpeakerId());
            log.debug("Text = {}", event.getResult().getText());
            /*
            Output Format

            SPEAKER-ID (e.g. Guest-01)
            Transcription

            SPEAKER-ID (e.g. Guest-02)
            Transcription
             */
            resultBuilder.append(event.getResult().getSpeakerId());
            resultBuilder.append(System.lineSeparator());
            resultBuilder.append(event.getResult().getText());
            resultBuilder.append(System.lineSeparator());
            resultBuilder.append(System.lineSeparator());

        }
    }

    private static void onCanceled(Object _s, ConversationTranscriptionCanceledEventArgs event) {
        log.info("Recognition canceled: Reason={}", event.getReason());
        if (event.getReason() == CancellationReason.Error) {
            log.error("Error while transcribing");
            log.error("ErrorCode={}", event.getErrorCode());
            log.error("ErrorDetails={}", event.getErrorDetails());
        }
        // Canceled due to end of Stream => finished, release semaphore
        blockRecoginizerSemaphore.release();
    }
}

