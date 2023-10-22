package com.trodix.casbinserver.autoconfigure;

import com.trodix.casbinserver.autoconfigure.properties.CasbinServerProperties;
import com.trodix.casbinserver.client.EnforcerClient;
import com.trodix.casbinserver.client.api.v1.EnforcerApi;
import com.trodix.casbinserver.client.oauth2.OAuth2Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CasbinServerProperties.class})
public class CasbinServerAutoConfiguration {

    private final CasbinServerProperties properties;

    public CasbinServerAutoConfiguration(
            CasbinServerProperties properties
    ) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public EnforcerApi autoConfigureEnforcerApi() {

        EnforcerClient client = new EnforcerClient().newBuilder()
                .baseUrl(properties.getUrl())
                .oauth2(new OAuth2Service(
                            properties.getOauth2().getTokenUri(),
                            properties.getOauth2().getClientId(),
                            properties.getOauth2().getClientSecret()
                        )
                )
                .build();

        return new EnforcerApi(client);
    }

}
