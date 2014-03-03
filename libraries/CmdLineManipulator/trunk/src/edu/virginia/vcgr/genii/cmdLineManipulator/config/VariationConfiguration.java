package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulator;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulators;

@XmlAccessorType(XmlAccessType.NONE)
public class VariationConfiguration implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "type", required = true)
	private String _variationType = null;

	@XmlAttribute(name = "name", required = true)
	private String _variationName = null;

	@XmlAnyElement
	private Collection<Element> _any = new ArrayList<Element>();

	@XmlTransient
	private Object _variationConfiguration = null;

	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller u, Object parent)
			throws JAXBException, CmdLineManipulatorException, IOException {
		if (_any.size() > 1)
			throw new JAXBException(
					"A CmdLine Manipulator configuration must have "
							+ "0 or 1 variation configurations, not more.");

		if (_any.size() == 0)
			_variationConfiguration = null;
		else {
			CmdLineManipulator<?> manipulator = CmdLineManipulators
					.getCmdLineManipulator(_variationType);
			Class<?> configType = manipulator.variationConfigurationType();
			JAXBContext context = JAXBContext.newInstance(configType);
			u = context.createUnmarshaller();
			_variationConfiguration = u.unmarshal(_any.iterator().next(),
					configType).getValue();
		}

		_any = null;
	}

	@SuppressWarnings("unused")
	private boolean beforeMarshal(Marshaller m) throws JAXBException,
			ParserConfigurationException {
		_any = new ArrayList<Element>();

		if (_variationConfiguration != null) {
			JAXBContext context = JAXBContext
					.newInstance(_variationConfiguration.getClass());
			Marshaller mm = context.createMarshaller();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			mm.marshal(_variationConfiguration, doc);
			_any.add(doc.getDocumentElement());
		}

		return true;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(_variationType);
		out.writeObject(_variationName);
		out.writeObject(_variationConfiguration);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		_variationType = (String) in.readObject();
		_variationName = (String) in.readObject();
		_variationConfiguration = in.readObject();

		_any = null;
	}

	@SuppressWarnings("unused")
	private void readObjectNoData() throws ObjectStreamException {
		throw new StreamCorruptedException();
	}

	final public String variationName() {
		return _variationName;
	}

	final public void variationName(String newName) {
		_variationName = newName;
	}

	final public String variationType() {
		return _variationType;
	}

	final public void variationType(String newType) {
		_variationType = newType;
	}

	final public Object variationConfigurtaion() {
		return _variationConfiguration;
	}

	final public void variationConfigurtaion(Object conf) {
		_variationConfiguration = conf;
	}

}