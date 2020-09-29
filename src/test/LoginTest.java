import com.google.gson.Gson;
import org.junit.Test;
import org.openkirkes.java.connector.KirkesClient;
import org.openkirkes.java.connector.classes.UserAuthentication;
import org.openkirkes.java.connector.classes.models.User;
import org.openkirkes.java.connector.interfaces.LoginInterface;

import java.util.concurrent.CountDownLatch;

public class LoginTest {


    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final static String username = "";
    private final static String password = "";

    @Test
    public void loginTestNoData() throws InterruptedException {

        System.out.println("Login...");

        KirkesClient kirkesClient = new KirkesClient();
        kirkesClient.login(new UserAuthentication(username, password), false, new LoginInterface() {
            @Override
            public void onError(Exception e) {
                System.out.println("FAILED!!");
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onLogin(User user) {
                System.out.println("Login Successfully!");
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    @Test
    public void loginTestWithData() throws InterruptedException {

        System.out.println("Login...");

        KirkesClient kirkesClient = new KirkesClient();
        kirkesClient.login(new UserAuthentication(username, password), true, new LoginInterface() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                System.out.println("FAILED!!");
                countDownLatch.countDown();
            }

            @Override
            public void onLogin(User user) {
                System.out.println("Login Successfully!");
                System.out.println("User Data:");
                System.out.println(new Gson().toJson(user));
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }


}
