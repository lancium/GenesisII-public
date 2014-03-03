package test.org.morgan.inject;

import org.morgan.inject.MInject;

class SentenceGenerator {
	static final private String SENTENCE_PATTERN = "Hello, %s!";

	@MInject(name = "mem:name")
	private String _name;

	@Override
	final public String toString() {
		return String.format(SENTENCE_PATTERN, _name);
	}
}