package com.getvoicify.providers;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class ApiKeyResourceProviderFactory implements RealmResourceProviderFactory {
    public static final String PROVIDER_ID = "api-key";
    public RealmResourceProvider create(KeycloakSession session) {
        return new ApiKeyResourceProvider(session);
    }

    public void init(Config.Scope config) {}

    public void postInit(KeycloakSessionFactory factory) {}

    public void close() {}

    public String getId() {
        return PROVIDER_ID;
    }
}
