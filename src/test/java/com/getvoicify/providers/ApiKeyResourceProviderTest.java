package com.getvoicify.providers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@Testcontainers
public class ApiKeyResourceProviderTest {

    @Container
    private static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
            .withProviderClassesFrom("target/classes");

    @Test
    public void shouldFailWithoutApiKey() {
        givenSpec().when().get("/check").then().statusCode(401);
    }

    @Test
    public void shouldFailToCreateWithNoBearerToken() {
        givenSpec().when().post().then().statusCode(401);
    }

    @Test
    public void shouldCreateApiKey() {
        Keycloak keycloakClient = keycloakContainer.getKeycloakAdminClient();
        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        givenSpec()
                .auth()
                .oauth2(accessTokenResponse.getToken())
                .when()
                .post()
                .then()
                .statusCode(200);
    }

    private RequestSpecification givenSpec() {
        return given().baseUri(keycloakContainer.getAuthServerUrl()).basePath("/realms/master/" + ApiKeyResourceProviderFactory.PROVIDER_ID);
    }
}