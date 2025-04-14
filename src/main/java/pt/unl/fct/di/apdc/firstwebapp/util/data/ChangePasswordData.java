package pt.unl.fct.di.apdc.firstwebapp.util.data;

public class ChangePasswordData extends Data {
    public String oldPassword;
    public String newPassword;
    public String confirmPassword;
    public String username;

    public ChangePasswordData() {

    }

    public ChangePasswordData(String username, String oldPassword, String newPassword, String confirmPassword) {
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    @Override
    public boolean validInput() {
        return nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(oldPassword) &&
                nonEmptyOrBlankField(newPassword) &&
                newPassword.equals(confirmPassword);
    }
}
