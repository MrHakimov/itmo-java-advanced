package ru.ifmo.rain.hakimov.bank;

import java.io.Serializable;
import java.util.HashMap;

public class LocalPerson extends BasePerson implements Serializable {
    protected LocalPerson(RemotePerson person) {
        super(person.getFirstName(), person.getLastName(),
                person.getPassportNumber(), new HashMap<>(person.getAccounts()));
    }

    /**
     * Adds account to the current person.
     *
     * @param id new account's id
     */
    @Override
    public synchronized Account addAccount(String id) {
        final Account account = new RemoteAccount(passportNumber + ":" + id);
        accounts.putIfAbsent(id, account);

        return accounts.get(id);
    }
}
