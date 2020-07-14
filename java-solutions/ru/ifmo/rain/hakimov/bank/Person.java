package ru.ifmo.rain.hakimov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    /** Returns person's first name. */
    String getFirstName() throws RemoteException;

    /** Returns person's last name. */
    String getLastName() throws RemoteException;

    /** Sets person's passport number. */
    String getPassportNumber() throws RemoteException;

    /** Gets person's bank accounts. */
    Map<String, Account> getAccounts() throws RemoteException;

    /** Adds account to the person. */
    Account addAccount(String id) throws RemoteException;

    /** Gets person's bank account by id. */
    Account getAccount(String accountId) throws RemoteException;
}
