cd "$(dirname "$0")" || exit

if [ -z "$(uname -a | grep MINGW)" ]; then SEPARATOR=":"; else SEPARATOR=";"; fi

mkdir -p out
javac -encoding UTF-8 -d out TextStatistics.java Pair.java StatisticsData.java Utils.java NumericStatistics.java UsageResourceBundle_ru.java UsageResourceBundle_en.java

mv StatisticsTests.txt StatisticsTests.java
javac -encoding UTF-8 -d out -cp "out${SEPARATOR}junit-platform-console-standalone-1.6.2.jar" StatisticsTests.java
mv StatisticsTests.java StatisticsTests.txt
