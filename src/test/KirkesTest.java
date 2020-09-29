import com.google.gson.Gson;
import org.junit.Test;
import org.openkirkes.java.connector.KirkesClient;
import org.openkirkes.java.connector.classes.UserAuthentication;
import org.openkirkes.java.connector.classes.models.User;
import org.openkirkes.java.connector.classes.models.loans.Loan;
import org.openkirkes.java.connector.interfaces.LoansInterface;
import org.openkirkes.java.connector.interfaces.LoginInterface;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class KirkesTest {


    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final static String username = "";
    private final static String password = "";
    private final static KirkesClient kirkesClient = new KirkesClient();

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

    @Test
    public void getLoans() throws InterruptedException {
        System.out.println("Fetching loans...");

        kirkesClient.getLoans(new LoansInterface() {
            @Override
            public void onGetLoans(List<Loan> loans) {
                System.out.println("Fetched successfully!");
                System.out.println("Loans:");
                System.out.println(new Gson().toJson(loans));
                countDownLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                System.out.println("FAILED!!");
                countDownLatch.countDown();
            }

        });
        countDownLatch.await();
    }


}
