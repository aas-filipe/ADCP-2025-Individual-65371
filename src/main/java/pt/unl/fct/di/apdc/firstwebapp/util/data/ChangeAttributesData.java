package pt.unl.fct.di.apdc.firstwebapp.util.data;


import pt.unl.fct.di.apdc.firstwebapp.util.users.AccountStatus;
import pt.unl.fct.di.apdc.firstwebapp.util.users.Role;

public class ChangeAttributesData{
    public String requestUser;
    public String targetUser;

	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String fullName;
	public String phoneNumber;
	public String publicProfile;
	public String ccNumber;
	public Role role;
	public String company;
	public String companyNIF;
	public String userNIF;
	public String occupation;
	public String address;
	public AccountStatus accountStatus;




	public ChangeAttributesData() {

	}

 public ChangeAttributesData(String email, String username, String fullName, String phoneNumber, String password, String confirmation, String publicProfile
	, String ccNumber, String occupation, String address, Role role, String company, String companyNIF, AccountStatus accountStatus, String userNIF) {
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


    public boolean validInput(Role requesterRole, Role targetRole) {
        if (requesterRole == Role.ENDUSER && (!targetUser.equals(requestUser) || this.username != null || this.fullName != null || this.email != null
                || this.role != null || this.accountStatus != null )){
            return false;
        }
        if (requesterRole == Role.BACKOFFICE && (targetRole == Role.ADMIN || (targetRole == Role.BACKOFFICE && !targetUser.equals(requestUser)) || username != null || this.email != null)){
            return false;
        }
        return requesterRole != Role.PARTNER;
    }


}
