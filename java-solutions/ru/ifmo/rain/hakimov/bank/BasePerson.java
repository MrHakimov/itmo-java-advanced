package ru.ifmo.rain.hakimov.bank;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public abstract class BasePerson implements Person, Serializable {
    protected final String firstName;
    protected final String lastName;
    protected final String passportNumber;
    protected final Map<String, Account> accounts;

    protected BasePerson(String firstName, String lastName, String passportNumber, Map<String, Account> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportNumber = passportNumber;
        this.accounts = accounts;
    }

    /**
     * Returns person's first name.
     */
    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns person's last name.
     */
    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns person's passport number.
     */
    @Override
    public String getPassportNumber() {
        return passportNumber;
    }

    /**
     * Returns person's accounts map.
     */
    @Override
    public synchronized Map<String, Account> getAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    /**
     * Gets person's bank account by it's id.
     *
     * @param accountId id of searching account
     */
    @Override
    public synchronized Account getAccount(String accountId) {
        return accounts.get(accountId);
    }
}
