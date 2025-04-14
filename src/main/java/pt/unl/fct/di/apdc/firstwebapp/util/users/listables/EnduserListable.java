package pt.unl.fct.di.apdc.firstwebapp.util.users.listables;

public class EnduserListable {
    public String username;
    public String email;
    public String fullname;

    public EnduserListable(String username, String email, String fullname) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
    }
    public EnduserListable() {}
}
