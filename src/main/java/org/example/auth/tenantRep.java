package org.example.auth;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import tn.supcom.appsec.entities.Tenant;


@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {
    Tenant findByName(String name);