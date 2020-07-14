cd "$(dirname "$0")" || exit

cd out || echo "Run build.sh first!"
java -cp "../junit-platform-console-standalone-1.6.2.jar:." ru.ifmo.rain.hakimov.bank.BankTests

echo "Exit code: $?"
exit $?
