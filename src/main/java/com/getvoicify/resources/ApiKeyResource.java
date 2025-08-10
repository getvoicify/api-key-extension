package com.getvoicify.resources;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.UUID;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;

@Provider
public class ApiKeyResource {
  private final KeycloakSession session;
  // keycloak utility to generate random strings, anything can be used e.g. UUID,...
  private final SecretGenerator secretGenerator;
  private final EntityManager entityManager;

  public ApiKeyResource(KeycloakSession session) {
    this.session = session;
    this.entityManager = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    this.secretGenerator = SecretGenerator.getInstance();
  }

  @GET
  @Path("/check")
  @Produces("application/json")
  public Response checkApiKey(@QueryParam("apiKey") String apiKey) {
    return session
            .users()
            .searchForUserByUserAttributeStream(session.getContext().getRealm(), "api-key", apiKey)
            .findFirst()
            .isPresent()
        ? Response.ok().type(MediaType.APPLICATION_JSON).build()
        : Response.status(401).type(MediaType.APPLICATION_JSON).build();
  }

  @POST
  @Produces("application/json")
  public Response createApiKey() {
    try {
      AuthResult authResult = authenticate();
      addApiKeyAttribute(authResult.getUser().getId());
      return Response.ok().type(MediaType.APPLICATION_JSON).build();
    } catch (Exception e) {
      return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).build();
    }
  }

  @PUT
  @Path("/rotate")
  @Produces("application/json")
  public Response rotateApiKey() {
    try {
      AuthResult authResult = authenticate();
      String userId = authResult.getUser().getId();

      // Check if user has an existing API key
      if (!hasApiKey(userId)) {
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
      }

      // Create new API key (addApiKeyAttribute handles removal of old one)
      addApiKeyAttribute(userId);

      return Response.ok().type(MediaType.APPLICATION_JSON).build();
    } catch (Exception e) {
      return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).build();
    }
  }

  private AuthResult authenticate() {
    AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();

    if (authResult == null) {
      throw new NotAuthorizedException("Bearer token is missing");
    }
    return authResult;
  }

  public void addApiKeyAttribute(String userId) {
    // Remove any existing API keys first to avoid duplicates
    removeApiKeyAttribute(userId);

    String apiKey = secretGenerator.randomString(50);
    UserEntity userEntity = entityManager.find(UserEntity.class, userId);
    UserAttributeEntity attributeEntity = new UserAttributeEntity();
    attributeEntity.setName("api-key");
    attributeEntity.setValue(apiKey);
    attributeEntity.setUser(userEntity);
    attributeEntity.setId(UUID.randomUUID().toString());
    entityManager.persist(attributeEntity);
  }

  private boolean hasApiKey(String userId) {
    // Flush any pending changes to ensure we see the current state
    entityManager.flush();
    entityManager.clear();

    TypedQuery<UserAttributeEntity> query =
        entityManager.createQuery(
            "SELECT ua FROM UserAttributeEntity ua WHERE ua.user.id = :userId AND ua.name = :name",
            UserAttributeEntity.class);
    query.setParameter("userId", userId);
    query.setParameter("name", "api-key");
    List<UserAttributeEntity> results = query.getResultList();
    return !results.isEmpty();
  }

  private void removeApiKeyAttribute(String userId) {
    TypedQuery<UserAttributeEntity> query =
        entityManager.createQuery(
            "SELECT ua FROM UserAttributeEntity ua WHERE ua.user.id = :userId AND ua.name = :name",
            UserAttributeEntity.class);
    query.setParameter("userId", userId);
    query.setParameter("name", "api-key");
    List<UserAttributeEntity> results = query.getResultList();

    for (UserAttributeEntity attributeEntity : results) {
      entityManager.remove(attributeEntity);
    }
  }
}
