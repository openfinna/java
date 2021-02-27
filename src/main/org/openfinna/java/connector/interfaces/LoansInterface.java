package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.loans.Loan;

import java.util.List;

public interface LoansInterface {
    void onGetLoans(List<Loan> loans);

    void onLoanRenew(Loan loan, String status);

    void onError(Exception e);
}
