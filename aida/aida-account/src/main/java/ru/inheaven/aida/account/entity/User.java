package ru.inheaven.aida.account.entity;

import ru.inheaven.aida.common.entity.AbstractEntity;

/**
 * inheaven on 19.04.2016.
 */
public class User extends AbstractEntity {
    private Long clientId;

    private String login;
    private String passport;
    private UserRole role;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
