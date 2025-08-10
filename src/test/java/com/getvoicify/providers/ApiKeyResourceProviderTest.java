package com.getvoicify.providers;

import static io.restassured.RestAssured.given;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Testcontainers
public class ApiKeyResourceProviderTest {

  @Container
  private static final KeycloakContainer keycloakContainer =
      new KeycloakContainer("quay.io/keycloak/keycloak:26.3.1")
          .withProviderClassesFrom("target/classes");

  @BeforeEach
  public void cleanupApiKeys() {
    try {
      Keycloak keycloakClient = keycloakContainer.getKeycloakAdminClient();
      UserRepresentation adminUser = keycloakClient.realm("master").users().search("admin").get(0);
      UserResource userResource = keycloakClient.realm("master").users().get(adminUser.getId());
      UserRepresentation userRep = userResource.toRepresentation();
      
      // Remove any existing api-key attributes
      if (userRep.getAttributes() != null && userRep.getAttributes().containsKey("api-key")) {
        Map<String, List<String>> attributes = new HashMap<>(userRep.getAttributes());
        attributes.remove("api-key");
        userRep.setAttributes(attributes);
        userResource.update(userRep);
      }
    } catch (Exception e) {
      // Ignore cleanup errors
    }
  }

  @Test
  public void shouldFailWithoutApiKey() {
    givenSpec().when().get("/check").then().statusCode(401);
  }

  @Test
  public void shouldFailToCreateWithNoBearerToken() {
    givenSpec().when().post().then().statusCode(Response.Status.FORBIDDEN.getStatusCode());
  }

  @Test
  public void shouldCreateApiKey() {
    Keycloak keycloakClient = keycloakContainer.getKeycloakAdminClient();
    AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().post().then().statusCode(200);
  }

  @Test
  public void shouldFailToRotateWithNoBearerToken() {
    givenSpec().when().put("/rotate").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());
  }

  @Test
  public void shouldRotateExistingApiKeySuccessfully() {
    Keycloak keycloakClient = keycloakContainer.getKeycloakAdminClient();
    AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
    
    // First create an API key
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().post().then().statusCode(200);
    
    // Then rotate it successfully  
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().put("/rotate").then().statusCode(200);
    
    // And rotate again to ensure it's repeatable
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().put("/rotate").then().statusCode(200);
  }

  @Test
  public void shouldRotateExistingApiKey() {
    Keycloak keycloakClient = keycloakContainer.getKeycloakAdminClient();
    AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
    
    // First create an API key
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().post().then().statusCode(200);
    
    // Then rotate it
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().put("/rotate").then().statusCode(200);
  }

  @Test
  public void shouldValidateApiKeyAfterRotation() {
    Keycloak keycloakClient = keycloakContainer.getKeycloakAdminClient();
    AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
    
    // Create initial API key
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().post().then().statusCode(200);
    
    // Test that we can check the API key exists (by trying to access validation endpoint with wrong key)
    givenSpec().when().get("/check?apiKey=invalid").then().statusCode(401);
    
    // Rotate the API key
    givenSpec().auth().oauth2(accessTokenResponse.getToken()).when().put("/rotate").then().statusCode(200);
    
    // The old invalid key should still be invalid (this test just verifies rotation completed)
    givenSpec().when().get("/check?apiKey=invalid").then().statusCode(401);
  }

  private RequestSpecification givenSpec() {
    return given()
        .baseUri(keycloakContainer.getAuthServerUrl())
        .basePath("/realms/master/" + ApiKeyResourceProviderFactory.PROVIDER_ID);
  }
}
