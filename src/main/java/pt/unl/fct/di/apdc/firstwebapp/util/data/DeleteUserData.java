package pt.unl.fct.di.apdc.firstwebapp.util.data;


public class DeleteUserData extends Data {
    public String username;
    public String targetUsername;
    public String targetEmail;

    public DeleteUserData() {}

    public DeleteUserData(String username, String targetUsername, String targetEmail) {
        this.username = username;
        this.targetUsername = targetUsername;
        this.targetEmail = targetEmail;
    }


    @Override
    public boolean validInput() {
        return (nonEmptyOrBlankField(username) ||
                nonEmptyOrBlankField(targetEmail)) &&
                nonEmptyOrBlankField(targetUsername);
    }

    public String getTargetKey(){
        if (targetUsername != null)
            return targetUsername;
        return targetEmail;
    }
}
