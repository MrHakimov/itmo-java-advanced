package ru.ifmo.rain.hakimov.i18n;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.stream.Stream;

public class TextStatistics {
    public static final String[] supportedLanguages = {"en", "ru"};
    private final String analysingText;
    private final Locale locale;

    @SuppressWarnings("unused")
    public TextStatistics(String analysingText) {
        this.analysingText = analysingText;

        Locale.Category category = Locale.Category.DISPLAY;
        this.locale = Locale.getDefault(category);
    }

    public TextStatistics(String analysingText, Locale locale) {
        this.analysingText = analysingText;
        this.locale = locale;
    }

    public StatisticsData calculateStatistics(BreakIterator breakingIterator) {
        breakingIterator.setText(analysingText);

        int leftBound = breakingIterator.first();
        int rightBound;

        StatisticsData resultingData = new StatisticsData();
        Set<String> tokensSet = new TreeSet<>();
        final Collator comparator = Collator.getInstance(locale);

        int lengthsSum = 0;
        while ((rightBound = breakingIterator.next()) != BreakIterator.DONE) {
            String token = analysingText.substring(leftBound, rightBound).trim();

            if (!token.isEmpty() && !token.equals("\n")) {
                resultingData.elementsNumber++;

                tokensSet.add(token);

                resultingData.minElement = (resultingData.minElement == null ||
                        comparator.compare(token, resultingData.minElement) < 0 ? token : resultingData.minElement);
                resultingData.minLengthElement = (resultingData.minLengthElement == null || token.length() <
                        resultingData.minLengthElement.length() ? token : resultingData.minLengthElement);

                resultingData.maxElement = (resultingData.maxElement == null ||
                        comparator.compare(resultingData.maxElement, token) < 0 ? token : resultingData.maxElement);
                resultingData.maxLengthElement = (resultingData.maxLengthElement == null || token.length() >
                        resultingData.maxLengthElement.length() ? token : resultingData.maxLengthElement);

                lengthsSum += token.length();
            }

            leftBound = rightBound;
        }

        resultingData.averageValue = (double) lengthsSum / resultingData.elementsNumber;
        resultingData.uniqueElementsNumber = tokensSet.size();

        return resultingData;
    }

    private <T> void setInformation(List<Pair<T, String>> elements,
                                    Comparator<Pair<T, String>> comparator,
                                    StatisticsData statisticsData) {
        if (elements.isEmpty()) {
            return;
        }

        final Utils<T> utils = new Utils<>(elements);
        statisticsData.minElement = utils.findMin(comparator);
        statisticsData.minLengthElement = utils.findMin(null);

        statisticsData.maxElement = utils.findMax(comparator);
        statisticsData.maxLengthElement = utils.findMax(null);

        statisticsData.elementsNumber = utils.findSize();
        statisticsData.uniqueElementsNumber = utils.findUniqueElementsNumber();
    }

    private double findAverageValue(List<Pair<Double, String>> elements, int elementsNumber) {
        if (elements.isEmpty()) {
            return 0;
        } else {
            return elements.stream().map(Pair::getFirst).reduce(Double::sum).get() / elementsNumber;
        }
    }

    private double findAverageValue(List<Pair<Number, String>> elements) {
        if (elements.isEmpty()) {
            return 0;
        } else {
            return elements.stream().map(e -> e.getFirst().doubleValue()).reduce(Double::sum).get() / elements.size();
        }
    }

    private void parseDate(final Calendar date, final DateFormat dateParser, final String token,
                           List<Pair<Calendar, String>> datesList, List<Pair<Double, String>> numbersList) {
        try {
            date.setTime(dateParser.parse(token));
            datesList.add(new Pair<>(date, token));
            numbersList.add(new Pair<>((double) date.get(Calendar.DAY_OF_MONTH), token));
            numbersList.add(new Pair<>((double) (date.get(Calendar.MONTH) + 1), token));
            numbersList.add(new Pair<>((double) date.get(Calendar.YEAR), token));
        } catch (ParseException ignored) {
            // No operations
        }
    }

    private void parseCurrency(final NumberFormat currencyParser, final String token,
                               List<Pair<Number, String>> currenciesList, List<Pair<Double, String>> numbersList) {
        try {
            final Number currency = currencyParser.parse(token);
            currenciesList.add(new Pair<>(currency, token));
            numbersList.add(new Pair<>(currency.doubleValue(), token));
        } catch (ParseException ignored) {
            // No operations
        }
    }

    private void parseNumber(final NumberFormat numberParser, final String token, List<Pair<Double, String>> numbersList) {
        try {
            final Number number = numberParser.parse(token);
            numbersList.add(new Pair<>(number.doubleValue(), token));
        } catch (ParseException ignored) {
            // No operations
        }
    }

    public NumericStatistics calculateNumbersStatistics() {
        StatisticsData dateStatistics = new StatisticsData();
        StatisticsData numbersStatistics = new StatisticsData();
        StatisticsData currencyStatistics = new StatisticsData();

        final DateFormat dateParser = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        final NumberFormat numberParser = NumberFormat.getNumberInstance(locale);
        final NumberFormat currencyParser = NumberFormat.getCurrencyInstance(locale);

        final BreakIterator breakingIterator = BreakIterator.getLineInstance(locale);
        breakingIterator.setText(analysingText);
        int leftBound = breakingIterator.first();
        int rightBound = breakingIterator.next();

        List<Pair<Calendar, String>> datesList = new ArrayList<>();
        List<Pair<Double, String>> numbersList = new ArrayList<>();
        List<Pair<Number, String>> currenciesList = new ArrayList<>();

        while (rightBound != BreakIterator.DONE) {
            final String token = analysingText.substring(leftBound, rightBound).trim();
            final Calendar date = Calendar.getInstance();

            parseDate(date, dateParser, token, datesList, numbersList);
            parseCurrency(currencyParser, token, currenciesList, numbersList);
            parseNumber(numberParser, token, numbersList);

            leftBound = rightBound;
            rightBound = breakingIterator.next();
        }

        final Comparator<Pair<Number, String>> currencyComparator = (e1, e2) ->
                ((int) Math.signum(e1.getFirst().doubleValue() - e2.getFirst().doubleValue()));
        final Comparator<Pair<Calendar, String>> dateComparator = (e1, e2) ->
                (e1.getFirst().before(e2.getFirst()) ? -1 : (e1.getFirst().after(e2.getFirst()) ? 1 : 0));
        final Comparator<Pair<Double, String>> numberComparator = (e1, e2) ->
                ((int) Math.signum(e1.getFirst() - e2.getFirst()));

        setInformation(currenciesList, currencyComparator, currencyStatistics);
        setInformation(datesList, dateComparator, dateStatistics);
        setInformation(numbersList, numberComparator, numbersStatistics);

        currencyStatistics.averageValue = findAverageValue(currenciesList);
        numbersStatistics.averageValue = findAverageValue(numbersList, numbersStatistics.elementsNumber);

        return new NumericStatistics(dateStatistics, numbersStatistics, currencyStatistics);
    }

    private static boolean invalidArguments(String[] args) {
        if (args == null || args.length < 4) {
            return true;
        }

        for (String arg : args) {
            if (arg == null) {
                return true;
            }
        }

        return false;
    }

    private static Locale findLocale(String localeString) {
        final Optional<Locale> result = Arrays.stream(Locale.getAvailableLocales()).filter(locale ->
                locale.getDisplayName().trim().equals(localeString.trim())).findFirst();

        if (!result.isPresent()) {
            System.err.println("The given locale is unsupported!");
            Arrays.stream(Locale.getAvailableLocales()).map(Locale::getDisplayName).sorted().forEach(System.err::println);
            System.exit(1);
        }
        return result.get();
    }

    private static int getLength(String s) {
        return (s == null ? 0 : s.length());
    }

    public static void main(String[] args) {
        if (invalidArguments(args)) {
            System.err.println("The given arguments are not valid!");
            return;
        }

        final int argsLength = args.length;
        final String inputFileName = args[argsLength - 2];
        final String outputFileName = args[argsLength - 1];
        StringBuilder localesString = new StringBuilder();
        for (int i = 0; i < argsLength - 2; i++) {
            localesString.append(args[i]).append("$");
        }

        final String[] locales = localesString.toString().split("\\)\\$");

        String firstLocaleString;
        String secondLocaleString;
        if (locales.length >= 2) {
            firstLocaleString = locales[0].replaceAll("\\$", " ") + ")";

            if (locales[1].contains("(")) {
                secondLocaleString = locales[1].replaceAll("\\$", " ") + ")";
            } else {
                secondLocaleString = locales[1].replaceAll("\\$", " ");
            }
        } else {
            String[] tmpLocales = localesString.toString().split("\\$");
            firstLocaleString = tmpLocales[0];

            StringBuilder tmp = new StringBuilder();
            for (int i = 2; i < tmpLocales.length; i++) {
                tmp.append(" ").append(tmpLocales[i]);
            }

            secondLocaleString = tmpLocales[1] + tmp;
        }

        final Locale firstLocale = findLocale(firstLocaleString);
        final Locale secondLocale = findLocale(secondLocaleString);

        StringBuilder content = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(inputFileName), StandardCharsets.UTF_8)) {
            stream.forEach(s -> content.append(s).append("\n"));
        } catch (InvalidPathException e) {
            System.err.println("Incorrect file!");
            return;
        } catch (IOException e) {
            System.err.println("Error occured while reading from file!\n" + e.getMessage());
        }

        final String language = secondLocale.getLanguage();
        if (!Arrays.asList(supportedLanguages).contains(language)) {
            System.err.println("Specified language for output is not supported!");
        }

        final ResourceBundle bundle = ResourceBundle.getBundle("ru.ifmo.rain.hakimov.i18n.UsageResourceBundle_" + language);

        final TextStatistics resultingData = new TextStatistics(content.toString(), firstLocale);
        final StatisticsData wordsStatistics = resultingData.calculateStatistics(
                BreakIterator.getWordInstance(firstLocale));
        final StatisticsData linesStatistics = resultingData.calculateStatistics(
                BreakIterator.getLineInstance(firstLocale));
        final StatisticsData sentencesStatistics = resultingData.calculateStatistics(
                BreakIterator.getSentenceInstance(firstLocale));

        final NumericStatistics numericStatistics = resultingData.calculateNumbersStatistics();
        final StatisticsData currencyStatistics = numericStatistics.getCurrency();
        final StatisticsData dateStatistics = numericStatistics.getDate();
        final StatisticsData numbersStatistics = numericStatistics.getNumbers();

        final double[] limits = {0, 1, 2};
        final String[] uniqueNumber = {bundle.getString("unique0"), bundle.getString("unique1"), bundle.getString("unique2")};
        final ChoiceFormat form = new ChoiceFormat(limits, uniqueNumber);

        String result = MessageFormat.format(
                "<html>\n" +
                        "	<head>\n" +
                        "		<meta charset=\"utf-8\">\n" +
                        "		<title>Text statistics</title>\n" +
                        "	</head>\n" +
                        "	<body>\n" +
                        "		<h1>{0}: {1}</h1>\n" +
                        "\n" +
                        "		<p><b>{2}:</b>\n" +
                        "		<br>{3} {4}: {5}\n" +
                        "		<br>{6} {7}: {8}\n" +
                        "		<br>{9} {10}: {11}\n" +
                        "		<br>{12} {13}: {14}\n" +
                        "		<br>{15} {16}: {17}\n" +
                        "		<br>{18} {19}: {20}\n" +
                        "		</p>\n" +
                        "\n" +
                        "		<p><b>{21}:</b>\n" +
                        "		<br>{22} {23}: {24} ({25} {26})\n" +
                        "		<br>{27} {28}: {29}\n" +
                        "		<br>{30} {31}: {32}\n" +
                        "		<br>{33} {34} {35}: {36} ({37})\n" +
                        "		<br>{38} {39} {40}: {41} ({42})\n" +
                        "		<br>{43} {44} {45}: {46}\n" +
                        "		</p>\n" +
                        "\n" +
                        "		<p><b>{47}:</b>\n" +
                        "		<br>{48} {49}: {50} ({51} {52})\n" +
                        "		<br>{53} {54}: {55}\n" +
                        "		<br>{56} {57}: {58}\n" +
                        "		<br>{59} {60} {61}: {62} ({63})\n" +
                        "		<br>{64} {65} {66}: {67} ({68})\n" +
                        "		<br>{69} {70} {71}: {72}\n" +
                        "		</p>\n" +
                        "\n" +
                        "		<p><b>{73}:</b>\n" +
                        "		<br>{74} {75}: {76} ({77} {78})\n" +
                        "		<br>{79} {80}: {81}\n" +
                        "		<br>{82} {83}: {84}\n" +
                        "		<br>{85} {86} {87}: {88} ({89})\n" +
                        "		<br>{90} {91} {92}: {93} ({94})\n" +
                        "		<br>{95} {96} {97}: {98}\n" +
                        "		</p>\n" +
                        "\n" +
                        "		<p><b>{99}:</b>\n" +
                        "		<br>{100} {101}: {102} ({103} {104})\n" +
                        "		<br>{105} {106}: {107}\n" +
                        "		<br>{108} {109}: {110}\n" +
                        "		<br>{111} {112} {113}: {114} ({115})\n" +
                        "		<br>{116} {117} {118}: {119} ({120})\n" +
                        "		<br>{121} {122}: {123}\n" +
                        "		</p>\n" +
                        "\n" +
                        "		<p><b>{124}:</b>\n" +
                        "		<br>{125} {126}: {127} ({128} {129})\n" +
                        "		<br>{130} {131}: {132}\n" +
                        "		<br>{133} {134}: {135}\n" +
                        "		<br>{136} {137} {138}: {139} ({140})\n" +
                        "		<br>{141} {142} {143}: {144} ({145})\n" +
                        "		<br>{146} {147}: {148}\n" +
                        "		</p>\n" +
                        "\n" +
                        "		<p><b>{149}:</b>\n" +
                        "		<br>{150} {151}: {152} ({153} {154})\n" +
                        "		<br>{155} {156}: {157}\n" +
                        "		<br>{158} {159}: {160}\n" +
                        "		<br>{161} {162} {163}: {164} ({165})\n" +
                        "		<br>{166} {167} {168}: {169} ({170})\n" +
                        "		</p>\n" +
                        "	</body>\n" +
                        "</html>\n",

                bundle.getString("analyse"),
                inputFileName,

                bundle.getString("info"),
                bundle.getString("Number"),
                bundle.getString("words"),
                wordsStatistics.elementsNumber,
                bundle.getString("Number"),
                bundle.getString("sentences"),
                sentencesStatistics.elementsNumber,
                bundle.getString("Number"),
                bundle.getString("lines"),
                linesStatistics.elementsNumber,
                bundle.getString("Number"),
                bundle.getString("dates"),
                dateStatistics.elementsNumber,
                bundle.getString("Number"),
                bundle.getString("numbers2"),
                numbersStatistics.elementsNumber,
                bundle.getString("Number"),
                bundle.getString("currency"),
                currencyStatistics.elementsNumber,

                bundle.getString("sentencesStatistics"),
                bundle.getString("Number"),
                bundle.getString("sentences"),
                sentencesStatistics.elementsNumber,
                sentencesStatistics.uniqueElementsNumber,
                form.format(sentencesStatistics.uniqueElementsNumber),
                bundle.getString("min"),
                bundle.getString("sentence"),
                sentencesStatistics.minElement,
                bundle.getString("max"),
                bundle.getString("sentence"),
                sentencesStatistics.maxElement,
                bundle.getString("min2"),
                bundle.getString("length"),
                bundle.getString("sentences2"),
                getLength(sentencesStatistics.minLengthElement),
                sentencesStatistics.minLengthElement,
                bundle.getString("max2"),
                bundle.getString("length"),
                bundle.getString("sentences2"),
                getLength(sentencesStatistics.maxLengthElement),
                sentencesStatistics.maxLengthElement,
                bundle.getString("average2"),
                bundle.getString("length"),
                bundle.getString("sentences2"),
                (int) sentencesStatistics.averageValue,

                bundle.getString("wordsStatistics"),
                bundle.getString("Number"),
                bundle.getString("words"),
                wordsStatistics.elementsNumber,
                wordsStatistics.uniqueElementsNumber,
                form.format(wordsStatistics.uniqueElementsNumber),
                bundle.getString("min"),
                bundle.getString("word2"),
                wordsStatistics.minElement,
                bundle.getString("max"),
                bundle.getString("word2"),
                wordsStatistics.maxElement,
                bundle.getString("min2"),
                bundle.getString("length"),
                bundle.getString("word"),
                getLength(wordsStatistics.minLengthElement),
                wordsStatistics.minLengthElement,
                bundle.getString("max2"),
                bundle.getString("length"),
                bundle.getString("word"),
                getLength(wordsStatistics.maxLengthElement),
                wordsStatistics.maxLengthElement,
                bundle.getString("average2"),
                bundle.getString("length"),
                bundle.getString("word"),
                (int) wordsStatistics.averageValue,

                bundle.getString("linesStatistics"),
                bundle.getString("Number"),
                bundle.getString("lines"),
                linesStatistics.elementsNumber,
                linesStatistics.uniqueElementsNumber,
                form.format(linesStatistics.uniqueElementsNumber),
                bundle.getString("min2"),
                bundle.getString("line2"),
                linesStatistics.minElement,
                bundle.getString("max2"),
                bundle.getString("line2"),
                linesStatistics.maxElement,
                bundle.getString("min2"),
                bundle.getString("length"),
                bundle.getString("line"),
                getLength(linesStatistics.minLengthElement),
                linesStatistics.minLengthElement,
                bundle.getString("max2"),
                bundle.getString("length"),
                bundle.getString("line"),
                getLength(linesStatistics.maxLengthElement),
                linesStatistics.maxLengthElement,
                bundle.getString("average2"),
                bundle.getString("length"),
                bundle.getString("line"),
                (int) linesStatistics.averageValue,

                bundle.getString("currencyStatistics"),
                bundle.getString("Number"),
                bundle.getString("currency"),
                currencyStatistics.elementsNumber,
                currencyStatistics.uniqueElementsNumber,
                form.format(currencyStatistics.uniqueElementsNumber),
                bundle.getString("min2"),
                bundle.getString("sum"),
                currencyStatistics.minElement,
                bundle.getString("max2"),
                bundle.getString("sum"),
                currencyStatistics.maxElement,
                bundle.getString("min2"),
                bundle.getString("length"),
                bundle.getString("currency"),
                getLength(currencyStatistics.minLengthElement),
                currencyStatistics.minLengthElement,
                bundle.getString("max2"),
                bundle.getString("length"),
                bundle.getString("currency"),
                getLength(currencyStatistics.maxLengthElement),
                currencyStatistics.maxLengthElement,
                bundle.getString("average2"),
                bundle.getString("sum"),
                currencyStatistics.averageValue,

                bundle.getString("numbersStatistics"),
                bundle.getString("Number"),
                bundle.getString("numbers2"),
                numbersStatistics.elementsNumber,
                numbersStatistics.uniqueElementsNumber,
                form.format(numbersStatistics.uniqueElementsNumber),
                bundle.getString("min"),
                bundle.getString("number"),
                numbersStatistics.minElement,
                bundle.getString("max"),
                bundle.getString("number"),
                numbersStatistics.maxElement,
                bundle.getString("min2"),
                bundle.getString("length"),
                bundle.getString("numbers"),
                getLength(numbersStatistics.minLengthElement),
                numbersStatistics.minLengthElement,
                bundle.getString("max2"),
                bundle.getString("length"),
                bundle.getString("numbers"),
                getLength(numbersStatistics.maxLengthElement),
                numbersStatistics.maxLengthElement,
                bundle.getString("average"),
                bundle.getString("number"),
                numbersStatistics.averageValue,

                bundle.getString("datesStatistics"),
                bundle.getString("Number"),
                bundle.getString("dates"),
                dateStatistics.elementsNumber,
                dateStatistics.uniqueElementsNumber,
                form.format(dateStatistics.uniqueElementsNumber),
                bundle.getString("min2"),
                bundle.getString("date2"),
                dateStatistics.minElement,
                bundle.getString("max2"),
                bundle.getString("date2"),
                dateStatistics.maxElement,
                bundle.getString("min2"),
                bundle.getString("length"),
                bundle.getString("date"),
                getLength(dateStatistics.minLengthElement),
                dateStatistics.minLengthElement,
                bundle.getString("max2"),
                bundle.getString("length"),
                bundle.getString("date"),
                getLength(dateStatistics.maxLengthElement),
                dateStatistics.maxLengthElement
        );

        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFileName));
            writer.write(result);
            writer.close();
        } catch (IOException e) {
            System.err.println("Unable to write to output file!");
        }
    }
}
