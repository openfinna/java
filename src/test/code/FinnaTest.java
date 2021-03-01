package code;

import com.google.gson.Gson;
import org.junit.Test;
import org.openfinna.java.OpenFinna;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.interfaces.LoansInterface;
import org.openfinna.java.connector.interfaces.LoginInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FinnaTest {

    private final Creds creds = new Gson().fromJson(new FileReader(new File("src/test/resources", "creds.json")), Creds.class);
    private final FinnaClient finnaClient = OpenFinna.newClient();

    public FinnaTest() throws FileNotFoundException {

    }

    public void signIn() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        finnaClient.login(new UserAuthentication(new UserType(creds.getType(), ""), creds.getUsername(), creds.getPassword()), false, new LoginInterface() {
            @Override
            public void onError(Exception e) {
                System.out.println("error!");
                e.printStackTrace();
                System.exit(-1);
            }

            @Override
            public void onLogin(UserAuthentication userAuthentication, User user) {
                System.out.println("Signed in!");
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    @Test
    public void getLoans() throws Exception {
        signIn();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        finnaClient.getLoans(new LoansInterface() {
            @Override
            public void onGetLoans(List<Loan> loans) {
                System.out.println("Loans:");
                for (Loan loan : loans) {
                    System.out.println(new Gson().toJson(loan));
                }
                countDownLatch.countDown();
            }

            @Override
            public void onLoanRenew(Loan loan, String status) {

            }

            @Override
            public void onError(Exception e) {
                System.out.println("error!");
                e.printStackTrace();
                System.exit(-1);
            }
        });
        countDownLatch.await();
    }
}