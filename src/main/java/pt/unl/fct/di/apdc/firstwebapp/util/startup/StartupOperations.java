package pt.unl.fct.di.apdc.firstwebapp.util.startup;

import com.google.cloud.datastore.*;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.users.AccountStatus;
import pt.unl.fct.di.apdc.firstwebapp.util.users.Role;

@WebListener
public class StartupOperations implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        Key rootkey = datastore.newKeyFactory().setKind("User").newKey("root");
        try {
            Entity root = datastore.get(rootkey);
            root = Entity.newBuilder(rootkey)
                    .set("username", "root")
                    .set("pwd", DigestUtils.sha512Hex("root"))
                    .set("public_profile", BooleanValue.of(false))
                    .set("account_status", AccountStatus.ACTIVE.name())
                    .set("role", Role.ADMIN.name())
                    .setNull("phone")
                    .setNull("email")
                    .setNull("full_name")
                    .setNull("cc")
                    .setNull("nif")
                    .setNull("company")
                    .setNull("company_nif")
                    .setNull("occupation")
                    .setNull("address")
                    .build();
            datastore.put(root);

        } catch (DatastoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
