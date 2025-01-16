package org.example.auth;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import tn.supcom.appsec.entities.Identity;
import tn.supcom.appsec.enums.Role;
import tn.supcom.appsec.repositories.IdentityRepository;
import tn.supcom.appsec.security.Argon2Utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class IdentityServices {

    @Inject
    IdentityRepository identityRepository;
    @Inject
    Argon2Utils argon2Utils;
    @Inject
    EmailService emailService;

    private final Map<String, Pair<String, LocalDateTime>> activationCodes = new HashMap<>();

    public void registerIdentity(String username, String password, String email){

        if(identityRepository.findByUsername(username).isPresent()){
            throw new EJBException("Identity with username " + username + " already exists");
        }
        if(identityRepository.findByEmail(email).isPresent()){
            throw new EJBException("Identity with email " + email + " already exists");
        }
        Identity identity = new Identity();
        identity.setUsername(username);
        identity.setPassword(password);
        identity.setEmail(email);
        identity.setCreationDate(LocalDateTime.now().toLocalDate().toString());
        identity.setRoles(Role.R_P00.getValue());
        identity.setScopes("resource:read,resource:write");
        identity.hashPassword(identity.getPassword(), argon2Utils);
        identityRepository.save(identity);
        String activationCode = GenerateActivationCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5); // Set expiration time
        activationCodes.put(activationCode, Pair.of(identity.getEmail(),expirationTime));


    }

    public void activateIdentity(String code) {
        if (activationCodes.containsKey(code)) {
            Pair<String, LocalDateTime> codeDetails = activationCodes.get(code);
            LocalDateTime expirationTime = codeDetails.getRight();
            String email = codeDetails.getLeft();
            Identity identity = identityRepository.findByEmail(email).orElse(null);
            if (LocalDateTime.now().isAfter(expirationTime)) {
                activationCodes.remove(code);
                identityRepository.delete(identity);
                throw new EJBException("Activation code expired");
            }

            if (identity !=null) {
                identity.setAccountActivated(true);
                identityRepository.save(identity);
                activationCodes.remove(code);
            } else {
                throw new EJBException("Identity not found.");
            }
        }else {
            throw new EJBException("Activation code not found");
        }
    }

    private String GenerateActivationCode() {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

}







