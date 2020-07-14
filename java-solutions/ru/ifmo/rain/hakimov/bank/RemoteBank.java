package ru.ifmo.rain.hakimov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RemotePerson> passportToPerson = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    public Account createAccount(final String id) throws RemoteException {
//        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    public Account getAccount(final String id) {
//        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public RemotePerson createPerson(final String firstName, final String lastName, final String passportNumber) throws RemoteException {
//        System.out.println("Registering person: " + firstName + " " + lastName
//                + ", with passport number: " + passportNumber);
        final var person = new RemotePerson(firstName, lastName, passportNumber, this);
        if (passportToPerson.putIfAbsent(passportNumber, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passportNumber);
        }
    }

    @Override
    public RemotePerson getPerson(final String passportNumber) {
//        System.out.println("Retrieving remote person with passport number " + passportNumber);
        return passportToPerson.get(passportNumber);
    }
}
