package com.uc.ipm.s2s.config;

import com.uc.ipm.s2s.util.TlsUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Configuration
public class WebClientConfig {

    @Value("${thirdparty.base-url}")
    private String baseUrl;

    @Value("${thirdparty.mtls.enabled:false}")
    private boolean mtlsEnabled;

    @Value("${thirdparty.mtls.key-store:}")
    private String keyStorePath;

    @Value("${thirdparty.mtls.key-store-password:}")
    private String keyStorePassword;

    @Value("${thirdparty.mtls.trust-store:}")
    private String trustStorePath;

    @Value("${thirdparty.mtls.trust-store-password:}")
    private String trustStorePassword;

    @Bean
    public WebClient riskClient(ClientRegistrationRepository registrations,
                                OAuth2AuthorizedClientService clientService) throws Exception {

        var clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);

        var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth.setDefaultClientRegistrationId("risk"); // spring.security.oauth2.client.registration.risk.*

        HttpClient httpClient = HttpClient.create();
        if (mtlsEnabled) {
            var sslContext = TlsUtils.buildClientSslContext(keyStorePath, keyStorePassword, trustStorePath, trustStorePassword);
            httpClient = httpClient.secure(sslSpec -> sslSpec.sslContext(sslContext));
        }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .apply(oauth.oauth2Configuration())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                        .build())
                .build();
    }
}
