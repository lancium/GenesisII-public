package edu.virginia.g3.fsview;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = FSViewXMLConstants.NS, name = "FSViewConnection")
public class FSViewConnectionInformation implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = FSViewXMLConstants.NS, name = "connection-uri", nillable = false, required = true)
	private URI _connectionURI;

	@XmlAttribute(name = "read-only")
	private boolean _readOnly;

	@XmlElements({
			@XmlElement(namespace = FSViewXMLConstants.NS, name = "anonymous", nillable = true, type = AnonymousAuthenticationInformation.class),
			@XmlElement(namespace = FSViewXMLConstants.NS, name = "username-password", nillable = true, type = UsernamePasswordAuthenticationInformation.class),
			@XmlElement(namespace = FSViewXMLConstants.NS, name = "domain-username-password", nillable = true, type = DomainUsernamePassswordAuthenticationInformation.class) })
	private FSViewAuthenticationInformation _authenticationInformation;

	public FSViewConnectionInformation() {
		this(null, null);
	}

	public FSViewConnectionInformation(URI connectionURI,
			FSViewAuthenticationInformation authInfo) {
		_connectionURI = connectionURI;
		_authenticationInformation = authInfo;
	}

	public boolean readOnly() {
		return _readOnly;
	}

	public void readOnly(boolean readOnly) {
		_readOnly = readOnly;
	}

	public FSViewSession openSession() throws IOException {
		FSViewFactory factory = FSViewFactories.factory(_connectionURI);
		return factory.openSession(_connectionURI, _authenticationInformation,
				_readOnly);
	}

	final public String shortName() {
		return _connectionURI.toString();
	}

	@Override
	final public String toString() {
		return String.format("%s[read-only: %s]\n%s", _connectionURI,
				_readOnly, _authenticationInformation);
	}
}