package ru.ifmo.rain.hakimov.bank;

import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Client {
    public static PrintStream printStream = System.out;

    private static boolean invalidArguments(final String... args) {
        if (args == null || args.length != 5) {
            return true;
        }

        for (int i = 0; i < 5; ++i) {
            if (args[i] == null) {
                return true;
            }
        }

        return false;
    }

    private static Person getPerson(Bank bank, String firstName, String lastName, String passportNumber) throws RemoteException {
        final String fullName = firstName + " " + lastName;
        Person person = bank.getPerson(passportNumber);

        if (person == null) {
            printStream.println("Registering person: " + fullName + "...");
            person = bank.createPerson(firstName, lastName, passportNumber);
        } else {
            printStream.println(fullName + " is already registered.");
        }

        return person;
    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) LocateRegistry.getRegistry().lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            printStream.println("Bank is not bound!");
            return;
        }

        if (invalidArguments(args)) {
            printStream.println("Invalid arguments!");
            return;
        }

        final String firstName = args[0];
        final String lastName = args[1];
        final String passportNumber = args[2];
        Person person = getPerson(bank, firstName, lastName, passportNumber);

        final String accountId = args[3];

        final int diff;
        try {
            diff = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            printStream.println("Illegal balance change given!");
            return;
        }

        Account account;
        if (person.getAccount(accountId) == null) {
            printStream.println("Creating account with id " + accountId + "...");
            account = person.addAccount(accountId);
        } else {
            printStream.println("Account with id " + accountId + " already exists.");
            account = person.getAccount(accountId);
        }

        printStream.println("The account has " + account.getAmount() + " units.");
        printStream.println("Changing balance...");
        account.setAmount(account.getAmount() + diff);
        printStream.printf("%s %s with passport %s has account %s with %s units on it.%n",
                person.getFirstName(),
                person.getLastName(),
                person.getPassportNumber(),
                accountId,
                account.getAmount()
        );
    }
}
