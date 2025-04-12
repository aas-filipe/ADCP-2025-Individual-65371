package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.datastore.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.data.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.data.LogoutData;
import pt.unl.fct.di.apdc.firstwebapp.util.data.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.authentication.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.authentication.AuthTokenStorage;
import pt.unl.fct.di.apdc.firstwebapp.util.users.AccountStatus;
import pt.unl.fct.di.apdc.firstwebapp.util.users.Role;
import pt.unl.fct.di.apdc.firstwebapp.util.users.User;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UsersResource {
    static final String USER_ID = "/{userId}";
    static final String REGISTER_PATH = "/register";
    static final String LOGIN_PATH = "/login";
    static final String LOGOUT_PATH = "/logout";
    static final String CHANGE_ROLE_PATH = "/change-role";
    static final String CHANGE_ACCOUNT_STATE_PATH = "/change-account-state";
    static final String REMOVE_USER_PATH = "/remove-user";
    static final String LIST_USERS_PATH = "/list-users";
    static final String CHANGE_ACCOUNT_ATTRIBUTES_PATH = "/change-account";
    static final String CHANGE_PASSWORD_PATH = "/change-password";

    private static final String USERNAME = "username";
    private static final String PASSWORD = "pwd";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String PUBLIC_PROFILE = "public_profile";
    private static final String FULL_NAME = "full_name";
    private static final String ROLE = "role";
    private static final String ACCOUNT_STATUS = "account_status";
    private static final String CC = "cc";
    private static final String NIF = "nif";
    private static final String COMPANY = "company";
    private static final String COMPANY_NIF = "company_nif";
    private static final String OCCUPATION = "occupation";
    private static final String ADDRESS = "address";


    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger Log = Logger.getLogger(UsersResource.class.getName());
    private static final Gson gson = new Gson();

    private final AuthTokenStorage authTokenStorage = AuthTokenStorage.getInstance();

    /**
     * Registers the user.
     *
     * @param registerData users register information.
     * @return NO_CONTENT if the user was registered, BAD_REQUEST if the fields are not filled properly or if the user already exists
     * , INTERNAL_SERVER_ERROR if something went wrong.
     */
    @Path(REGISTER_PATH)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(RegisterData registerData) {
        Log.info("Registering user " + registerData.username);

        try {
            if (!registerData.validRegistration())
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(registerData.username);
            Entity user = datastore.get(userKey);

            if (user != null)
                return Response.status(Response.Status.BAD_REQUEST).entity("User already exists.").build();

            if (registerData.role == null) {
                registerData.role = Role.ENDUSER;
            }
            if (registerData.accountStatus == null) {
                registerData.accountStatus = AccountStatus.INACTIVE;
            }

            user = Entity.newBuilder(userKey)
                    .set(USERNAME, registerData.username)
                    .set(PASSWORD, DigestUtils.sha512Hex(registerData.password))
                    .set(EMAIL, registerData.email)
                    .set(PHONE, registerData.phoneNumber)
                    .set(PUBLIC_PROFILE, registerData.publicProfile)
                    .set(FULL_NAME, registerData.fullName)
                    .set(ROLE, registerData.role.name())
                    .set(ACCOUNT_STATUS, registerData.accountStatus.name())
                    .build();

            user = buildOptionParam(user, CC, registerData.ccNumber);
            user = buildOptionParam(user, NIF, registerData.userNIF);
            user = buildOptionParam(user, COMPANY, registerData.company);
            user = buildOptionParam(user, COMPANY_NIF, registerData.companyNIF);
            user = buildOptionParam(user, OCCUPATION, registerData.occupation);
            user = buildOptionParam(user, ADDRESS, registerData.address);

            datastore.put(user);
            Log.info("User " + registerData.username + " registered.");

            return Response.ok().build();
        } catch (DatastoreException e) {

            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Logs the user in.
     *
     * @param loginData users login information.
     * @return OK with the authToken if the user logged in successfully, BAD_REQUEST if the login was ill formatted,
     * NOT_FOUND if the user doesn't exist and FORBIDDEN if the password is wrong
     */
    public Response loginUser(LoginData loginData) {
        return Response.ok().build();
    }


    /**
     * Logs the user out.
     *
     * @param logoutData token with the user's login information
     * @return NO_CONTENT if the user was logged out successfully, FORBIDDEN if the authToken isn't valid
     */
    public Response logOut(LogoutData logoutData) {
        return Response.ok().build();
    }


    /**
     * Changes the role of a user.
     * @param authToken token with the user's login information.
     * @param targetUserId target userId whose role will be changed.
     * @param newRole
     * @return NO_CONTENT if the operation was successful, FORBIDDEN if the token isn't valid or if the user doesn't have
     * the clearance to make the change to that user or that role, NOT_FOUND if the target user doesn't exist
     * or BAD_REQUEST if the newRole isn't valid.
     */
    public Response changeRole(AuthToken authToken, String targetUserId, String newRole) {



        return null;
    }


    /**
     * Changes the account state of a user.
     * @param authToken token with the user's login information.
     * @param targetUserId target userId whose account state will be changed
     * @param newAccountState
     * @return NO_CONTENT if the operation was successful, FORBIDDEN if the token isn't valid or if the user doesn't have
     * the clearance to make the change to that user or account states in general, NOT_FOUND if the target user doesn't exist
     * BAD_REQUEST if the newAccountState isn't valid.
     */
    public Response changeAccountState(AuthToken authToken, String targetUserId, String newAccountState) {
        return null;
    }


    /**
     * Removes a user from the app. Either the userID or the
     * @param authToken token with the user's login information.
     * @param targetUserId userId of the user that's going to be removed.
     * @param accountEmail email of the users that's going to be removed.
     * @return NO_CONTENT if the operation was successful, FORBIDDEN if the token isn't valid or if the user doesn't have
     * clearance to remove that user or BAD_REQUEST if neither the userId nor the accountEmail were provided.
     */
    public Response removeUser(AuthToken authToken, String targetUserId, String accountEmail) {
        return null;
    }


    /**
     * Lists all users and parameters that the user that invoked the request has access to.
     * @param authToken token with the user's login information.
     * @return OK with a List of users and their info if the operation was successful, FORBIDDEN if the token isn't valid
     */
    public Response listUsers(AuthToken authToken) {
        return null;
    }

    /**
     * Changes the attributes of the targetted user
     * @param targetUserId
     * @param authToken
     * @param newUserDetails
     * @return
     */
    public Response changeAccountAtributes(AuthToken authToken, User newUserDetails, String targetUserId) {
        return null;
    }


    public Response changePassword(AuthToken authToken, String currentPassword, String newPassword) {
        return null;
    }


    private Entity buildOptionParam(Entity entity, String paramName, Object paramValue) {
        if (paramValue != null) {
            return Entity.newBuilder(entity)
                    .set(paramName, (Value<?>) paramValue)
                    .build();
        } else {
            return Entity.newBuilder(entity)
                    .setNull(paramName).build();
        }

    }
}

