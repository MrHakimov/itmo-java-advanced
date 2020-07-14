cd "$(dirname "$0")" || exit

mkdir -p out
javac -d out Account.java Bank.java BasePerson.java Client.java LocalPerson.java \
              Person.java RemoteAccount.java RemoteBank.java RemotePerson.java Server.java

mv BankTests.txt BankTests.java
javac -d out -cp "out:junit-platform-console-standalone-1.6.2.jar" BankTests.java

mv BankTests.java BankTests.txt
