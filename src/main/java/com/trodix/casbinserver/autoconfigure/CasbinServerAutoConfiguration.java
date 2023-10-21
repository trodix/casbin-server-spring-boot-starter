package com.trodix.casbinserver.autoconfigure;

import com.trodix.casbinserver.autoconfigure.properties.CasbinServerProperties;
import com.trodix.casbinserver.client.EnforcerClient;
import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.casbinserver.oauth2.CasbinServerOAuth2CasbinClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@Configuration
@EnableConfigurationProperties({CasbinServerProperties.class})
@AutoConfigureAfter({OAuth2ClientAutoConfiguration.class})
public class CasbinServerAutoConfiguration {

    private final CasbinServerProperties properties;

    private final ClientRegistrationRepository clientRegistrationRepository;

    public CasbinServerAutoConfiguration(
            ClientRegistrationRepository clientRegistrationRepository,
            CasbinServerProperties properties
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public CasbinServerOAuth2CasbinClient autoConfigureCasbinServerOAuth2Client(
            DefaultOAuth2AuthorizedClientManager authorizedClientManager,
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceManager
    ) {
        return new CasbinServerOAuth2CasbinClient(
                authorizedClientManager,
                authorizedClientServiceManager,
                clientRegistrationRepository,
                properties.getClientRegistrationId()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceOAuth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService
    ) {
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .refreshToken()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultOAuth2AuthorizedClientManager defaultOAuth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository
    ) {
        return new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public EnforcerApi autoConfigureEnforcerApi(CasbinServerOAuth2CasbinClient autoConfigureCasbinServerOAuth2Client) {

        String token = autoConfigureCasbinServerOAuth2Client.getAccessToken();

        EnforcerClient client = new EnforcerClient().newBuilder()
                .header("Authorization", "Bearer " + token)
                .baseUrl(properties.getUrl())
                .build();

        return new EnforcerApi(client);
    }

}
