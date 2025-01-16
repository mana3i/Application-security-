package org.example.auth;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import tn.supcom.appsec.entities.Identity;

import java.util.Optional;

@Repository
public interface IdentityRepository extends CrudRepository<Identity, String> {
    Optional<Identity> findByEmail(String email);
    Optional<Identity> findByUsername(String username);
}