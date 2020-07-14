cd "$(dirname "$0")" || exit

cd out || exit
java -cp . ru.ifmo.rain.hakimov.bank.Server
