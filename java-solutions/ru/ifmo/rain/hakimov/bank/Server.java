package ru.ifmo.rain.hakimov.bank;

import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private final static int PORT = 8888;
    private static final String BANK_ADDRESS = "//localhost/bank";

    private static Registry registry;
    public static PrintStream printStream = System.out;

    public static void main(final String... args) {
        final Bank bank = new RemoteBank(PORT);
        try {
            registry = LocateRegistry.createRegistry(1099);

            UnicastRemoteObject.exportObject(bank, PORT);
            registry.rebind(BANK_ADDRESS, bank);
        } catch (RemoteException e) {
            printStream.println("Can't create registry or export object: " + e.getMessage());

            return;
        }

        printStream.println("Server is up now...");
    }

    public static void downServer() {
        try {
            registry.unbind(BANK_ADDRESS);
            printStream.println("Server is down now...");
        } catch (RemoteException | NotBoundException e) {
            printStream.println("Can't down the server: " + e.getMessage());
        }
    }
}
