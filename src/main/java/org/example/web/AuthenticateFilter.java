package org.example.web;

import jakarta.annotation.Priority;
import jakarta.ejb.EJBException;
import jakarta.json.Json;
import jakarta.json.JsonString;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Arrays;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Config config = ConfigProvider.getConfig();
    private static final String REALM = config.getValue("jwt.realm",String.class);
    private static final String CLAIM_ROLES = config.getValue("jwt.claim.roles",String.class);
    private static final String AUTHENTICATION_SCHEME = "Bearer";



    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);


        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }
        String token = authorizationHeader
                .substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            InitialContext context = new InitialContext();
            JwtManagerVerifier jwtManagerVerifier = (JwtManagerVerifier) context.lookup("java:module/JwtManagerVerifier");
            var claims = jwtManagerVerifier.verifyToken(token);
            if(!claims.isEmpty()){
                final var roles = Json.createReader(new StringReader(claims.get(CLAIM_ROLES))).readArray().getValuesAs(JsonString.class).stream().map(JsonString::getString).toList().toArray(new String[0]);
                final Principal userPrincipal = new CallerPrincipal(claims.get("sub"));
                final boolean isSecure = requestContext.getSecurityContext().isSecure();
                IdentityUtility.iAm(claims.get("sub"));
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return userPrincipal;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return Arrays.asList(roles).contains(role);
                    }

                    @Override
                    public boolean isSecure() {
                        return isSecure;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return AUTHENTICATION_SCHEME;
                    }
                });
            }
        } catch (EJBException | NamingException e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .build());
    }
}
