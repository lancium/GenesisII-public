package edu.virginia.vcgr.genii.container.rfork.sd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

/**
 * A translator that turns a piece of state into an XML element and reads from XML. In order for
 * this state translator to work, the state type MUST support XML serialization and deserialization.
 * Further, the class that sub-classes from SimpleStateResourceFork MUST have the
 * XMLStateDescription annotation present on it.
 * 
 * @author mmm2a
 */
public class XMLStateTranslator implements StateTranslator
{
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public <StateType> StateType read(SimpleStateResourceFork<StateType> originatingFork, Class<StateType> targetType,
		InputStream input) throws IOException
	{
		return (StateType) ObjectDeserializer.deserialize(new InputSource(input), targetType);
	}

	/** {@inheritDoc} */
	@Override
	public <StateType> void write(SimpleStateResourceFork<StateType> originatingFork, StateType state, OutputStream output)
		throws IOException
	{
		XMLStateDescription description = originatingFork.getClass().getAnnotation(XMLStateDescription.class);
		if (description == null)
			throw new IOException("Unable to find required XMLStateDescription annotation " + "on target fork \""
				+ originatingFork.getClass().getName() + "\".");

		String namespace = description.namespace();
		if (namespace.length() == 0)
			namespace = null;

		OutputStreamWriter writer = new OutputStreamWriter(output);
		ObjectSerializer.serialize(writer, state, new QName(namespace, description.localName()));
		writer.flush();
	}
}