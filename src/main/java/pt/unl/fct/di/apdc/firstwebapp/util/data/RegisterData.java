package pt.unl.fct.di.apdc.firstwebapp.util.data;


import pt.unl.fct.di.apdc.firstwebapp.util.users.AccountStatus;
import pt.unl.fct.di.apdc.firstwebapp.util.users.Role;

public class RegisterData {

	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String fullName;
	public String phoneNumber;
	public boolean publicProfile;
	public String ccNumber;
	public Role role;
	public String company;
	public int companyNIF;
	public int userNIF;
	public String occupation;
	public String address;
	public AccountStatus accountStatus;




	public RegisterData() {

	}
 public RegisterData(String email, String username, String fullName, String phoneNumber, String password, String confirmation, boolean publicProfile
	, String ccNumber, String occupation, String address, Role role, String company, int companyNIF, AccountStatus accountStatus, int userNIF) {
		this.email = email;
		this.username = username;
		this.fullName = fullName;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.confirmation = confirmation;
		this.publicProfile = publicProfile;
		this.ccNumber = ccNumber;
		this.occupation = occupation;
		this.address = address;
		this.role = role;
		this.company = company;
		this.companyNIF = companyNIF;
		this.userNIF = userNIF;
		this.accountStatus = accountStatus;
	}

	private boolean nonEmptyOrBlankField(String field) {
		return field != null && !field.isBlank();
	}

	public boolean validRegistration() {
		if (this.role == null){
			this.role = Role.ENDUSER;
		}
		if(this.accountStatus == null){
			this.accountStatus = AccountStatus.INACTIVE;
		}
		return nonEmptyOrBlankField(username) &&
				phoneNumberValid() &&
				emailValid() &&
				fullNameValid() &&
				passwordValid();
	}

	private boolean phoneNumberValid() {
		return nonEmptyOrBlankField(phoneNumber);
	}

	private boolean emailValid() {
		return nonEmptyOrBlankField(email);
	}

	private boolean fullNameValid() {
		return nonEmptyOrBlankField(fullName);
	}

	private boolean passwordValid() {
		return nonEmptyOrBlankField(password) && password.equals(confirmation);
	}
}
