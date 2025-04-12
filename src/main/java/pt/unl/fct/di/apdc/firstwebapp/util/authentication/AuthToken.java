package pt.unl.fct.di.apdc.firstwebapp.util.authentication;

import java.util.UUID;

public class AuthToken {

    public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2;
    public long creationData;
    public long expirationData;
    private String username;
    private String tokenID;

    public AuthToken() {

    }

    public AuthToken(String username) {
        this.username = username;
        this.tokenID = UUID.randomUUID().toString();
        this.creationData = System.currentTimeMillis();
        this.expirationData = this.creationData - EXPIRATION_TIME;
    }

    public boolean isValid() {
        return creationData + expirationData > System.currentTimeMillis();
    }

    public String getTokenID() {
        return tokenID;
    }

    public String getUsername() {
        return username;
    }


}
