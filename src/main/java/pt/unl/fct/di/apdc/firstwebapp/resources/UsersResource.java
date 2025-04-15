package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.authentication.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.authentication.AuthTokenStorage;
import pt.unl.fct.di.apdc.firstwebapp.util.data.*;
import pt.unl.fct.di.apdc.firstwebapp.util.users.AccountStatus;
import pt.unl.fct.di.apdc.firstwebapp.util.users.Role;
import pt.unl.fct.di.apdc.firstwebapp.util.users.UserPermissions;
import pt.unl.fct.di.apdc.firstwebapp.util.users.listables.EnduserListable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
            Key indexKey = datastore.newKeyFactory().setKind("Index").newKey(registerData.email);
            Entity index = datastore.get(indexKey);
            Entity user = datastore.get(userKey);

            if (user != null)
                return Response.status(Response.Status.BAD_REQUEST).entity("User already exists.").build();
            if (index != null)
                return Response.status(Response.Status.BAD_REQUEST).entity("This email is already associated to an account.").build();

            index = Entity.newBuilder(indexKey)
                    .set("username", registerData.username)
                    .build();

            Entity.Builder builder = Entity.newBuilder(userKey)
                    .set(USERNAME, registerData.username)
                    .set(PASSWORD, DigestUtils.sha512Hex(registerData.password))
                    .set(EMAIL, registerData.email)
                    .set(PHONE, registerData.phoneNumber)
                    .set(PUBLIC_PROFILE, BooleanValue.of(registerData.publicProfile))
                    .set(FULL_NAME, registerData.fullName)
                    .set(ROLE, registerData.role.name())
                    .set(ACCOUNT_STATUS, registerData.accountStatus.name());

            setOptional(builder, CC, registerData.ccNumber);
            setOptional(builder, NIF, LongValue.of(registerData.userNIF));
            setOptional(builder, COMPANY, registerData.company);
            setOptional(builder, COMPANY_NIF, LongValue.of(registerData.companyNIF));
            setOptional(builder, OCCUPATION, registerData.occupation);
            setOptional(builder, ADDRESS, registerData.address);

            user = builder.build();

            datastore.put(index);
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
    @POST
    @Path(LOGIN_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(LoginData loginData) {
        Log.info("Logining user " + loginData.username);
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(loginData.username);
            Entity user = datastore.get(userKey);
            if (user == null) {
                Log.warning("User " + loginData.username + " not found.");
                return Response.status(Response.Status.NOT_FOUND).entity("Username or password wrong.").build();
            }
            AccountStatus accountStatus = AccountStatus.valueOf(user.getString(ACCOUNT_STATUS));
            if (!UserPermissions.isActive(accountStatus)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("This account has been deactivated. Please contact an administrator.").build();
            }
            if (!DigestUtils.sha512Hex(loginData.password).equals(user.getString(PASSWORD))) {
                Log.warning("User " + loginData.username + " does not match password.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Username or password wrong.").build();
            }
            AuthToken token = authTokenStorage.createToken(loginData.username);
            return Response.ok().entity(" Successfully logged in user " + loginData.username + ". Here is the session token: " + token.getTokenID()).build();
        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * Logs the user out.
     *
     * @param tokenId
     * @param username
     * @return NO_CONTENT if the user was logged out successfully, FORBIDDEN if the authToken isn't valid
     */
    @POST
    @Path(LOGOUT_PATH)
    public Response logOut(@HeaderParam("Authorization") String tokenId, @QueryParam("username") String username) {
        if (tokenId == null || tokenId.isEmpty() || username == null || username.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found or invalid session").build();
        }
        if (authTokenStorage.revokeToken(username, tokenId)) {

            return Response.ok().entity("User " + username + " logged out.").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Couldn't logout, invalid session").build();
        }
    }


    /**
     * Changes the role of a user.
     *
     * @param targetUsername target userId whose role will be changed.
     * @param username
     * @param tokenId
     * @param role
     * @return NO_CONTENT if the operation was successful, FORBIDDEN if the token isn't valid or if the user doesn't have
     * the clearance to make the change to that user or that role, NOT_FOUND if the target user doesn't exist
     * or BAD_REQUEST if the newRole isn't valid.
     */
    @POST
    @Path(CHANGE_ROLE_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(@HeaderParam("Authorization") String tokenId, @QueryParam("targetUsername") String targetUsername,
                               @QueryParam("username") String username, @QueryParam("role") String role) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        Key targetKey = datastore.newKeyFactory().setKind("User").newKey(targetUsername);
        try {
            Entity user = datastore.get(userKey);
            Entity target = datastore.get(targetKey);

            Role newRole;
            try {
                newRole = Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid role.").build();
            }

            if (user == null || target == null)
                return Response.status(Response.Status.NOT_FOUND).entity("User or Target not found.").build();

            if (!authTokenStorage.useToken(username, tokenId))
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid session.").build();

            Role userRole = Role.valueOf(user.getString(ROLE));
            Role targetRole = Role.valueOf(target.getString(ROLE));

            if (!UserPermissions.changeRoleOrDeleteUser(userRole, targetRole)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not enough permissions.").build();
            }

            target = Entity.newBuilder(target)
                    .set("role", newRole.name())
                    .build();

            datastore.update(target);
            return Response.ok().entity("User " + targetUsername + " role has been changed to: " + role).build();

        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * Changes the account state of a user.
     *
     * @param tokenId
     * @param targetUsername
     * @param username
     * @param status
     * @return NO_CONTENT if the operation was successful, FORBIDDEN if the token isn't valid or if the user doesn't have
     * the clearance to make the change to that user or account states in general, NOT_FOUND if the target user doesn't exist
     * BAD_REQUEST if the newAccountState isn't valid.
     */
    @POST
    @Path(CHANGE_ACCOUNT_STATE_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeAccountState(@HeaderParam("Authorization") String tokenId, @QueryParam("targetUsername") String targetUsername,
                                       @QueryParam("username") String username, @QueryParam("status") String status) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        Key targetKey = datastore.newKeyFactory().setKind("User").newKey(targetUsername);

        try {
            Entity user = datastore.get(userKey);
            Entity target = datastore.get(targetKey);

            AccountStatus newAccountStatus;
            try {
                newAccountStatus = AccountStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid account status.").build();
            }

            if (user == null || target == null)
                return Response.status(Response.Status.NOT_FOUND).entity("User or Target not found.").build();

            if (!authTokenStorage.useToken(username, tokenId))
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid session.").build();

            Role userRole = Role.valueOf(user.getString(ROLE));
            Role targetRole = Role.valueOf(target.getString(ROLE));

            if (!UserPermissions.changeStatus(userRole, targetRole)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not enough permissions.").build();
            }

            if (status.equals(AccountStatus.INACTIVE.toString())) {
                authTokenStorage.logOutUser(targetUsername);
            }

            target = Entity.newBuilder(target)
                    .set("account_status", newAccountStatus.name())
                    .build();

            datastore.update(target);
            return Response.ok().entity("User " + targetUsername + " status " + " has been changed to: " + status).build();

        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * Removes a user from the app. Either the userID or the
     *
     * @param tokenId
     * @param targetUsername
     * @param username
     * @param targetEmail
     * @return NO_CONTENT if the operation was successful, FORBIDDEN if the token isn't valid or if the user doesn't have
     * clearance to remove that user or BAD_REQUEST if neither the userId nor the accountEmail were provided.
     */
    @POST
    @Path(REMOVE_USER_PATH)
    public Response removeUser(@HeaderParam("Authorization") String tokenId, DeleteUserData deleteUserData) {

        if (!deleteUserData.validInput()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
        }
        String targetUsername = deleteUserData.targetUsername;
        if (targetUsername == null || targetUsername.isBlank()) {
            Key targetKey = datastore.newKeyFactory().setKind("Index").newKey(deleteUserData.targetEmail);
            targetUsername = datastore.get(targetKey).getString("username");
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(deleteUserData.username);
        Key targetKey = datastore.newKeyFactory().setKind("User").newKey(targetUsername);
        try {
            Entity user = datastore.get(userKey);
            Entity target = datastore.get(targetKey);

            if (user == null || target == null)
                return Response.status(Response.Status.NOT_FOUND).entity("User or Target not found.").build();

            if (!authTokenStorage.useToken(deleteUserData.username, tokenId))
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid session.").build();

            Role userRole = Role.valueOf(user.getString(ROLE));
            Role targetRole = Role.valueOf(target.getString(ROLE));

            if (!UserPermissions.changeRoleOrDeleteUser(userRole, targetRole)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not enough permissions.").build();
            }

            authTokenStorage.logOutUser(targetUsername);
            datastore.delete(targetKey);
            return Response.ok().entity("User " + targetUsername + " has been deleted.").build();

        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * Lists all users and parameters that the user that invoked the request has access to.
     *
     * @param tokenId
     * @param username
     * @return OK with a List of users and their info if the operation was successful, FORBIDDEN if the token isn't valid
     */
    @POST
    @Path(LIST_USERS_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@HeaderParam("Authorization") String tokenId, @QueryParam("username") String username) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        try {
            Entity user = datastore.get(userKey);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User " + username + " doesn't exist.").build();
            }
            if (!authTokenStorage.useToken(username, tokenId)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid session.").build();
            }

            Role userRole = Role.valueOf(user.getString(ROLE));
            switch (userRole) {
                case Role.ADMIN -> {
                    List<Entity> queryResult = new LinkedList<>();
                    Query<Entity> query = Query.newEntityQueryBuilder()
                            .setKind("User").build();
                    datastore.run(query).forEachRemaining(queryResult::add);
                    return Response.ok().entity(gson.toJson(queryResult)).build();
                }
                case Role.BACKOFFICE -> {
                    List<Entity> queryResult = new LinkedList<>();
                    Query<Entity> query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.PropertyFilter.eq(ROLE, Role.ENDUSER.name()))
                            .build();
                    datastore.run(query).forEachRemaining(queryResult::add);
                    return Response.ok().entity(gson.toJson(queryResult)).build();
                }
                case Role.ENDUSER -> {
                    List<EnduserListable> queryResult = new LinkedList<>();
                    Query<Entity> query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(StructuredQuery.CompositeFilter.and(
                                    StructuredQuery.PropertyFilter.eq(ROLE, Role.ENDUSER.name()),
                                    StructuredQuery.PropertyFilter.eq(ACCOUNT_STATUS, AccountStatus.ACTIVE.name()),
                                    StructuredQuery.PropertyFilter.eq(PUBLIC_PROFILE, "true")))
                            .build();
                    datastore.run(query).forEachRemaining(entity -> {
                        EnduserListable result = new EnduserListable(entity.getString(USERNAME), entity.getString(EMAIL), entity.getString(FULL_NAME));
                        queryResult.add(result);
                    });
                    return Response.ok().entity(gson.toJson(queryResult)).build();
                }
                default -> {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Not enough permissions").build();
                }
            }
        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Changes the attributes of the targetted user
     *
     * @param tokenId
     * @param targetUserId
     * @param username
     * @param newUserDetails
     * @return
     */
    @Path(CHANGE_ACCOUNT_ATTRIBUTES_PATH)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountAttributes(@HeaderParam("Authorization") String tokenId, ChangeAttributesData changes) {
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(changes.requestUser);
        Key targetKey = datastore.newKeyFactory().setKind("User").newKey(changes.targetUser);

        try {
            Entity user = datastore.get(userKey);
            Entity target = datastore.get(targetKey);

            if (user == null || target == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User or Target not found.").build();
            }

            if (!authTokenStorage.useToken(changes.requestUser, tokenId)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid session.").build();
            }
            Role userRole = Role.valueOf(user.getString(ROLE));
            Role targetRole = Role.valueOf(target.getString(ROLE));

            if (changes.validInput(userRole, targetRole)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not enough permissions.").build();
            }

            Entity.Builder builder = Entity.newBuilder(target);
            setNullable(builder, EMAIL, changes.email);
            setNullable(builder, USERNAME, changes.username);
            setNullable(builder, PASSWORD, changes.password);
            setNullable(builder, ROLE, changes.role);
            setNullable(builder, ACCOUNT_STATUS, changes.accountStatus);
            setNullable(builder, PHONE, changes.phoneNumber);
            if (changes.publicProfile.equals("true") || changes.publicProfile.equals("false")) {
                setNullable(builder, PUBLIC_PROFILE, Boolean.parseBoolean(changes.publicProfile));
            }
            setNullable(builder, CC, changes.ccNumber);
            setNullable(builder, ADDRESS, changes.address);
            setNullable(builder, COMPANY, changes.company);
            setNullable(builder, NIF, changes.companyNIF);
            setNullable(builder, COMPANY, changes.company);
            setNullable(builder, ACCOUNT_STATUS, changes.accountStatus);
            setNullable(builder, PHONE, changes.phoneNumber);

            Entity updatedUser = builder.build();
            datastore.put(updatedUser);
            return Response.ok(updatedUser).entity("User " + changes.targetUser + " has been uodated.").build();

        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * @param tokenId
     * @param changePasswordData
     * @return
     */
    @Path(CHANGE_PASSWORD_PATH)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(@HeaderParam("Authorization") String tokenId, ChangePasswordData changePasswordData) {
        if (!changePasswordData.validInput()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid or empty parameter.").build();
        }
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(changePasswordData.username);

        try {
            Entity user = datastore.get(userKey);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User does not exist.").build();
            }

            if (!authTokenStorage.useToken(changePasswordData.username, tokenId)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid session.").build();
            }

            user = Entity.newBuilder(user)
                    .set(PASSWORD, DigestUtils.sha512Hex(changePasswordData.newPassword))
                    .build();

            datastore.update(user);
            return Response.ok().entity("User " + changePasswordData.username + " password has been updated").build();

        } catch (DatastoreException e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getReason()).build();
        } catch (Exception e) {
            Log.log(Level.ALL, e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    private void setOptional(Entity.Builder builder, String paramName, Object paramValue) {
        if (paramValue != null) {
            builder.set(paramName, (Value<?>) paramValue);
        } else {
            builder.setNull(paramName);
        }
    }

    private void setNullable(Entity.Builder builder, String paramName, Object paramValue) {
        if (paramValue != null) {
            builder.set(paramName, (Value<?>) paramValue);
        }
    }
}

