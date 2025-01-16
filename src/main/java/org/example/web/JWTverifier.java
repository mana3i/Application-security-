package org.example.web;

import jakarta.ejb.*;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;

@Startup
@Singleton
@LocalBean
public class JwtManagerVerifier {
    private final static String curve = "Ed25519";
    private final static Signature signatureAlgorithm;

    static {
        try {
            signatureAlgorithm = Signature.getInstance(curve);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException(e);
        }
    }
    private static final Config config = ConfigProvider.getConfig();
    private static final String JWK_ENDPOINT = config.getValue("jwt.jwk.endpoint", String.class);


    public Map<String,String> verifyToken(String token) {
        var parts = token.split("\\.");
        var header = Json.createReader(new StringReader(new String(Base64.getUrlDecoder().decode(parts[0])))).readObject();
        var kid = header.getString("kid");
        if(kid == null) {
            throw new EJBException("Invalid token");
        }
        var publicKey = getPublicKeyFromKid(kid);
        if(publicKey == null) {
            return Collections.emptyMap();
        }
        try {
            signatureAlgorithm.initVerify(publicKey);
            signatureAlgorithm.update((parts[0]+"."+parts[1]).getBytes(StandardCharsets.UTF_8));
            if(!signatureAlgorithm.verify(Base64.getUrlDecoder().decode(parts[2]))) {
                return Collections.emptyMap();
            }
            var payload = Json.createReader(new StringReader(new String(Base64.getUrlDecoder().decode(parts[1])))).readObject();
            var exp = payload.getJsonNumber("exp");
            if(exp == null) {
                throw new EJBException("Invalid token");
            }
            if(Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now())) {
                return Collections.emptyMap();
            }
            return Map.of("tenant-id",payload.getString("tenant-id"),
                    "sub",payload.getString("sub"),
                    "upn",payload.getString("upn"),
                    "scope",payload.getString("scope"),
                    "groups",payload.getJsonArray("groups").toString());
        } catch (InvalidKeyException | SignatureException e) {
            throw new EJBException(e);
        }
    }


    public PublicKey getPublicKeyFromKid(String kid) {
        try {
            String jwkUrl = JWK_ENDPOINT + "?kid=" + kid;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwkUrl))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject key = Json.createReader(new StringReader(response.body())).readObject();
                var encodedPublicKeyFromJWKX =  key.getString("x");
                var decodedPublicKey = Base64.getUrlDecoder().decode(encodedPublicKeyFromJWKX);
                var isOdd = (decodedPublicKey[decodedPublicKey.length - 1] & 255) >> 7 == 1;
                decodedPublicKey[decodedPublicKey.length - 1] &= 127;
                var i = 0;
                var j = decodedPublicKey.length -1;
                while (i<j){
                    var tmp = decodedPublicKey[i];
                    decodedPublicKey[i] = decodedPublicKey[j];
                    decodedPublicKey[j] = tmp;
                    ++i;--j;
                }
                var y = new BigInteger(1, decodedPublicKey);
                var ep = new EdECPoint(isOdd,y);
                var paramSpec = new NamedParameterSpec(curve);
                var publicKeySpec = new EdECPublicKeySpec(paramSpec,ep);
                var kf = KeyFactory.getInstance(curve);
                return kf.generatePublic(publicKeySpec);
            } else {
                throw new RuntimeException("Failed to fetch key: HTTP status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
