package code;

import com.google.gson.Gson;
import org.junit.Test;
import org.openfinna.java.OpenFinna;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.classes.models.libraries.schedule.Day;
import org.openfinna.java.connector.classes.models.libraries.schedule.SelfServicePeriod;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.interfaces.*;

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

    @Test
    public void getDescription() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        finnaClient.getResourceDescription("3amk.137069", new DescriptionInterface() {
            @Override
            public void onGetDescription(String description) {
                System.out.println("Desc: " + description);
                assert description.length() > 0;
                countDownLatch.countDown();
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

    @Test
    public void getPickupLocations() throws Exception {
        signIn();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        finnaClient.getPickupLocations(new Resource("helmet.2121045"), new PickupLocationsInterface() {
            @Override
            public void onFetchPickupLocations(List<PickupLocation> locations, HoldingDetails holdingDetails, PickupLocation defaultLocation) {
                System.out.println("Details: " + new Gson().toJson(holdingDetails));
                System.out.println("Default: " + defaultLocation);
                countDownLatch.countDown();
            }

            @Override
            public void onFetchDefaultPickupLocation(PickupLocation defaultLocation, List<PickupLocation> allLocations) {

            }

            @Override
            public void onError(Exception e) {
                System.out.println("error!");
                e.printStackTrace();
                System.exit(-1);
            }
        }, null);
        countDownLatch.await();
    }

    @Test
    public void getLibs() throws Exception {
        signIn();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        finnaClient.getLibraries(new LibrariesInterface() {
            @Override
            public void onGetLibraries(List<Library> libraries) {
                for (Library library : libraries) {
                    for (Day day : library.getDays()) {
                        for (SelfServicePeriod selfServicePeriod : day.getSelfServicePeriods()) {
                            if (selfServicePeriod.getStart() == null) {
                                System.out.println(new Gson().toJson(library));
                            }
                        }
                    }
                    //System.out.println(new Gson().toJson(library.getDays()));
                }
                countDownLatch.countDown();
            }

            @Override
            public void onGetLibrary(Library library) {

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