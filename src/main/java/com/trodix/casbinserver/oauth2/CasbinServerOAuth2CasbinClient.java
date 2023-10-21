package com.trodix.casbinserver.oauth2;

import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class CasbinServerOAuth2CasbinClient extends OAuth2CasbinClientService {

    private final String clientRegistrationId;

    private final ClientRegistrationRepository clientRegistrationRepository;

    public CasbinServerOAuth2CasbinClient(
            DefaultOAuth2AuthorizedClientManager authorizedClientManager,
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceManager,
            ClientRegistrationRepository clientRegistrationRepository,
            String clientRegistrationId
    ) {
        super(authorizedClientManager, authorizedClientServiceManager);
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.clientRegistrationId = clientRegistrationId;
    }

    public String getAccessToken() {
        String clientId = clientRegistrationRepository.findByRegistrationId(clientRegistrationId).getClientId();

        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(clientRegistrationId)
                        .principal(clientId)
                        .build();

        OAuth2AuthorizedClient authorizedClient = getOAuth2AuthorizedClientManager().authorize(authorizeRequest);
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        return accessToken.getTokenValue();
    }

}
