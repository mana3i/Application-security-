package org.example.auth;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import tn.supcom.appsec.security.Argon2Utils;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;


@Entity("identities")
public class Identity implements Serializable, Principal {
    @Id
    @Column("id")
    private String id;
    @Column("username")
    private String username;
    @Column
    private String email;
    @Column("password")
    private String password;
    @Column("creationDate")
    private String  creationDate;
    @Column("role")
    private Long roles;
    @Column("scopes")
    private String scopes;
    @Column("isAccountActivated")
    private boolean isAccountActivated;

    public String getId() {return id;}

    public String getUsername() {return username;}

    public String getEmail() {return email;}

    public String getPassword() {return password;}

    public String getCreationDate() {return creationDate;}

    public Long getRoles() {return roles;}

    public String getScopes() {return scopes;}

    public boolean getAccountActivated() {return isAccountActivated;}

    public void setUsername(String username) {this.username = username;}

    public void setEmail(String email) {this.email = email;}

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreationDate(String creationDate) {this.creationDate = creationDate;}

    public void setRoles(Long roles) {this.roles = roles;}

    public void setScopes(String scopes) {this.scopes = scopes;}

    public void setAccountActivated(boolean accountActivated) {this.isAccountActivated = accountActivated;}

    public Identity() {
        this.id = UUID.randomUUID().toString();
        this.isAccountActivated = false;
    }

    public Identity(String id, String username, String password, String creationDate, Long roles, boolean isAccountActivated) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.creationDate = creationDate;
        this.roles = roles;
        this.isAccountActivated = isAccountActivated;
    }

    @Override
    public String toString() {
        return "Identity{" +
                "id='" + id + '\'' +
                "username='" + username + '\'' +
                "password='" + password + '\'' +
                "creationDate=" + creationDate +
                "roles=" + roles +
                "scopes=" + scopes +
                "accoutActivated=" + isAccountActivated +
                '}';
    }

    @Override
    public String getName() {
        return username;
    }

    public void hashPassword(String password, Argon2Utils argonUtility) {
        this.password = argonUtility.hash(password.toCharArray());
    }

}
