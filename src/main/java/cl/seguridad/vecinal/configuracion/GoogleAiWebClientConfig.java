package cl.seguridad.vecinal.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class GoogleAiWebClientConfig {

    @Bean(name = "googleAiWebClient")
    public WebClient googleAiWebClient(WebClient.Builder builder,
            @Value("${google.ai.api.key}") String apiKey
    ) {
        return builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // Para API Key de Google AI Studio:
                .defaultHeader("x-goog-api-key", apiKey)
                // Filtro simple para logging de errores
                .filter(ExchangeFilterFunction.ofResponseProcessor(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Error de Google AI: " + resp.statusCode() + " | Body: " + body
                                )));
                    }
                    return Mono.just(resp);
                }))
                .build();
    }
}