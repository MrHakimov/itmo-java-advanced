package ru.ifmo.rain.hakimov.bank;

import java.rmi.RemoteException;
import java.util.HashMap;

public class RemotePerson extends BasePerson {
    private final Bank bank;

    public RemotePerson(String firstName, String lastName, String passportNumber, Bank bank) {
        super(firstName, lastName, passportNumber, new HashMap<>());
        this.bank = bank;
    }

    /**
     * Adds account to the person.
     *
     * @param id new account's id
     */
    @Override
    public synchronized Account addAccount(String id) throws RemoteException {
        final Account account = bank.createAccount(this.passportNumber + ":" + id);
        accounts.putIfAbsent(id, account);

        return account;
    }
}
