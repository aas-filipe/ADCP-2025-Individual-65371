package pt.unl.fct.di.apdc.firstwebapp.util.data;

public class LoginData extends Data {

	public String username;
	public String password;

	public LoginData() {

	}

	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public boolean validInput() {
		return false;
	}
}