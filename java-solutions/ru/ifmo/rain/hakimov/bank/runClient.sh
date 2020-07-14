cd "$(dirname "$0")" || exit

cd out || exit
java -cp . ru.ifmo.rain.hakimov.bank.Client $@

# Example: bash runClient.sh John Hakimov 777P777 fst 1000000
