package org.openkirkes.java.connector.interfaces;

import org.openkirkes.java.connector.classes.models.loans.Loan;

import java.util.List;

public interface LoansInterface {
    void onGetLoans(List<Loan> loans);

    void onError(Exception e);
}
