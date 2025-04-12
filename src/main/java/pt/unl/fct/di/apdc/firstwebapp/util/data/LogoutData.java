package pt.unl.fct.di.apdc.firstwebapp.util.data;

public class LogoutData {

    public String username;
    public String authTokenId;

    public LogoutData(String username, String authTokenId) {
        this.username = username;
        this.authTokenId = authTokenId;
    }

    public LogoutData() {

    }
}
