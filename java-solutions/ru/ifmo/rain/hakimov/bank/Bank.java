package ru.ifmo.rain.hakimov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Creates a new account for the person.
     * @param firstName first name of the person
     * @param lastName last name of the person
     * @param passportNumber passport number of the person
     * @return created or existing person.
     */
    Person createPerson(String firstName, String lastName, String passportNumber) throws RemoteException;

    /**
     * Returns person by passport number.
     * @param passportNumber passport number of the person
     * @return remote person with specified passport number or {@code null} if such person does not exists.
     */
    Person getPerson(final String passportNumber) throws RemoteException;
}
