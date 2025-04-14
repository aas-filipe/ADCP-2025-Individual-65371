package pt.unl.fct.di.apdc.firstwebapp.util.authentication;

import java.util.concurrent.ConcurrentHashMap;

public class AuthTokenStorage {

    private static final AuthTokenStorage instance = new AuthTokenStorage();
    private final ConcurrentHashMap<String, AuthToken> authTokens = new ConcurrentHashMap<>();

    public static AuthTokenStorage getInstance() {
       return instance;
    }

    private AuthTokenStorage() {
    }

    public AuthToken createToken(String username) {
        AuthToken authToken = new AuthToken(username);
        authTokens.put(username, authToken);
        return authToken;
    }

    public boolean revokeToken(String username, String tokenId){
        AuthToken authToken= authTokens.get(username);
        if(authToken != null && authToken.getTokenID().equals(tokenId)){
            authTokens.remove(username);
            return true;
        }
        return false;
    }

    public boolean useToken(String username, String tokenId){
        AuthToken authToken = authTokens.get(username);
        return authToken != null && authToken.getTokenID().equals(tokenId) && authToken.isValid();

    }

    public void logOutUser(String username){
        authTokens.remove(username);
    }
}
