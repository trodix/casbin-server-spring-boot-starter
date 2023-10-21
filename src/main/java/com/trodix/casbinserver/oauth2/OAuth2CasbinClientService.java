package com.trodix.casbinserver.oauth2;

import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.web.context.ServletContextAware;

public abstract class OAuth2CasbinClientService {

    private final DefaultOAuth2AuthorizedClientManager authorizedClientManager;
    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceManager;

    public OAuth2CasbinClientService(
            DefaultOAuth2AuthorizedClientManager authorizedClientManager,
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceManager
    ) {
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientServiceManager = authorizedClientServiceManager;
    }

    /**
     * Renvoi la bonne implémentation de OAuth2AuthorizedClientManager en fonction du context (servlet ou non)
     * @return une instance de OAuth2AuthorizedClientManager en fonction du context de l'appel
     */
    public OAuth2AuthorizedClientManager getOAuth2AuthorizedClientManager() {
        if (ServletContextAware.class.isAssignableFrom(this.getClass())) {
            return authorizedClientManager;
        } else {
            return authorizedClientServiceManager;
        }
    }

}
