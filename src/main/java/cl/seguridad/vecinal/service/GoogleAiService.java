package cl.seguridad.vecinal.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GoogleAiService {

    private final WebClient googleAiWebClient;

    public GoogleAiService(@Qualifier("googleAiWebClient") WebClient googleAiWebClient) {
        this.googleAiWebClient = googleAiWebClient;
    }

    public Mono<String> generarContenido(Map<String, Object> requestBody) {
        return googleAiWebClient
                .post()
                .uri("/v1beta/models/gemini-2.0-flash:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }
}
