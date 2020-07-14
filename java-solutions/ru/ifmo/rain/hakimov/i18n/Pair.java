package ru.ifmo.rain.hakimov.i18n;

class Pair<F, S> {
	private final F first;
	private final S second;

	Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return this.first;
	}

	public S getSecond() {
		return this.second;
	}
}
