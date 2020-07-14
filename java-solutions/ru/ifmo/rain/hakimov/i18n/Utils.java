package ru.ifmo.rain.hakimov.i18n;

import java.util.Comparator;
import java.util.List;

class Utils<F> {
    final List<Pair<F, String>> elements;
    final Comparator<Pair<F, String>> lengthComparator = Comparator.comparingInt(e -> e.getSecond().length());

    Utils(List<Pair<F, String>> elements) {
        this.elements = elements;
    }

    String findMin(Comparator<Pair<F, String>> comparator) {
        if (comparator == null) {
            comparator = lengthComparator;
        }

        return elements.stream().min(comparator).map(Pair::getSecond).orElse(null);
    }

    String findMax(Comparator<Pair<F, String>> comparator) {
        if (comparator == null) {
            comparator = lengthComparator;
        }

        return elements.stream().max(comparator).map(Pair::getSecond).orElse(null);
    }

    int findSize() {
        return elements.size();
    }

    int findUniqueElementsNumber() {
        return (int) elements.stream().distinct().count();
    }
}
