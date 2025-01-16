package org.example.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tn.supcom.appsec.entities.Identity;
import tn.supcom.appsec.repositories.TenantRepository;
import tn.supcom.appsec.services.IdentityServices;

import java.io.InputStream;
import java.util.Objects;

@Path("/")
public class IdentityRegistration {


    @Inject
    IdentityServices identityServices;
    @Inject
    TenantRepository tenantRepository;

    @GET
    @Path("/register/authorize")
    @Produces(MediaType.TEXT_HTML)
    public Response authorizeRegistration(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();
        var clientId = params.getFirst("client_id");
        if (clientId == null || clientId.isEmpty()) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        var tenant = tenantRepository.findByName(clientId);
        if (tenant == null) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        String redirectUri = params.getFirst("redirect_uri");
        if (tenant.getRedirectUri() != null && !tenant.getRedirectUri().isEmpty()) {
            if (redirectUri != null && !redirectUri.isEmpty() && !tenant.getRedirectUri().equals(redirectUri)) {
                return informUserAboutError("redirect_uri is pre-registred and should match");
            }
        } else {
            if (redirectUri == null || redirectUri.isEmpty()) {
                return informUserAboutError("redirect_uri is not pre-registred and should be provided");
            }
        }
        StreamingOutput stream = output -> {
            try (InputStream is = Objects.requireNonNull(getClass().getResource("/signup.html")).openStream()){
                output.write(is.readAllBytes());
            }
        };
        return Response.ok(stream)
                .location(uriInfo.getBaseUri().resolve("/register"))
                .build();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response register(@FormParam("username")String username,
                             @FormParam("email")String email,
                             @FormParam("password")String password
    ) {

    }

    @POST
    @Path("/register/activate")
    public Response activate(@FormParam("code") String code
    ) {
        try {
            identityServices.activateIdentity(code);
            return Response.ok().build();
        } catch (Exception e){
            return informUserAboutError(e.getMessage());
        }
    }

    private Response informUserAboutError(String error) {
        return Response.status(Response.Status.BAD_REQUEST).entity("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <title>Error</title>
                </head>
                <body>
                <aside class="container">
                    <p>%s</p>
                </aside>
                </body>
                </html>
                """.formatted(error)).build();
    }


}
