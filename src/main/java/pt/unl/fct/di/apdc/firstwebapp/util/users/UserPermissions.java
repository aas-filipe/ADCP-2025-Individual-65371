package pt.unl.fct.di.apdc.firstwebapp.util.users;

import pt.unl.fct.di.apdc.firstwebapp.util.users.Role;

public class UserPermissions {


    public static boolean changeRoleOrDeleteUser(Role role, Role targetRole) {
        return role == Role.ADMIN || role == Role.BACKOFFICE && (targetRole == Role.PARTNER || targetRole == Role.ENDUSER);
    }


    public static boolean changeStatus(Role role, Role targetRole) {
        return role == Role.ADMIN || role == Role.BACKOFFICE && targetRole != Role.ADMIN;
    }


    public static boolean isActive(AccountStatus userAccountStatus) {
        return userAccountStatus == AccountStatus.ACTIVE;
    }
}
