package ru.ifmo.rain.hakimov.i18n;

class NumericStatistics {
    private final StatisticsData date;
    private final StatisticsData numbers;
    private final StatisticsData currency;

    NumericStatistics(StatisticsData date, StatisticsData numbers, StatisticsData currency) {
        this.date = date;
        this.numbers = numbers;
        this.currency = currency;
    }

    public StatisticsData getDate() {
        return date;
    }

    public StatisticsData getNumbers() {
        return numbers;
    }

    public StatisticsData getCurrency() {
        return currency;
    }
}
