import org.junit.Test;
import org.openkirkes.java.connector.KirkesClient;
import org.openkirkes.java.connector.interfaces.LoginInterface;

import java.util.concurrent.CountDownLatch;

public class LoginTest {


    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Test
    public void loginTestNoData() throws InterruptedException {

        System.out.println("Login...");

        KirkesClient kirkesClient = new KirkesClient();
        kirkesClient.login("", "", false, new LoginInterface() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                System.out.println("FAILED!!");
                countDownLatch.countDown();
            }

            @Override
            public void onLogin() {
                System.out.println("Login Successfully!");
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();


    }


}
