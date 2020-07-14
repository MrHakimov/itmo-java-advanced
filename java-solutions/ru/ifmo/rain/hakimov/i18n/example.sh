cd "$(dirname "$0")" || exit

cd out

java ru.ifmo.rain.hakimov.i18n.TextStatistics English Russian ../input.txt ../output.html
