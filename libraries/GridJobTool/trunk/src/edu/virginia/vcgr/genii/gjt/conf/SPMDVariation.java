package edu.virginia.vcgr.genii.gjt.conf;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class SPMDVariation {
	@XmlAttribute(name = "name")
	private String _name;

	@XmlValue
	private URI _variationURI;

	@SuppressWarnings("unused")
	private SPMDVariation() {
		// For XML deserialization only
		_name = null;
		_variationURI = null;
	}

	public SPMDVariation(String name, URI variationURI) {
		if (name == null)
			throw new IllegalArgumentException(
					"SPMDVariation name cannot be null.");
		if (variationURI == null)
			throw new IllegalArgumentException(
					"SPMDVariation URI cannot be null.");

		_name = name;
		_variationURI = variationURI;
	}

	public boolean equals(SPMDVariation other) {
		return _name.equals(other._name)
				&& _variationURI.equals(other._variationURI);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SPMDVariation)
			return equals((SPMDVariation) other);

		return false;
	}

	final public String name() {
		return _name;
	}

	final public URI variationURI() {
		return _variationURI;
	}

	@Override
	public String toString() {
		return _name;
	}
}