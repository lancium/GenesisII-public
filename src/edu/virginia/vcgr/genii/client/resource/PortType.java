package edu.virginia.vcgr.genii.client.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.utils.bvm.BitVectorMap;

public class PortType
{
	static private final FileResource KNOWN_PORT_TYPES_RESOURCE =
		new FileResource("edu/virginia/vcgr/genii/client/resource/known-porttypes.txt");
	
	static private BitVectorMap<PortType> _vectorMap;
	static private Map<QName, PortType> _knownPortTypes;
	
	static
	{
		InputStream in = null;
		_knownPortTypes = new LinkedHashMap<QName, PortType>();
		
		try
		{
			in = KNOWN_PORT_TYPES_RESOURCE.open();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			
			while ( (line = reader.readLine()) != null)
			{
				QName name = QName.valueOf(line.trim());
				_knownPortTypes.put(name, new PortType(name));
			}
			
			_vectorMap = new BitVectorMap<PortType>(_knownPortTypes.values());
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace(System.err);
			System.exit(1);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	private QName _portTypeName;
	
	private PortType(QName portTypeName)
	{
		_portTypeName = portTypeName;
	}
	
	public QName getQName()
	{
		return _portTypeName;
	}
	
	public String toString()
	{
		return _portTypeName.toString();
	}
	
	public boolean equals(PortType other)
	{
		return _portTypeName.equals(other._portTypeName);
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof PortType)
			return equals((PortType)other);
		
		return false;
	}
	
	public int hashCode()
	{
		return _portTypeName.hashCode();
	}
	
	static public PortType get(QName portType)
	{
		PortType pt = _knownPortTypes.get(portType);
		if (pt == null)
			throw new IllegalArgumentException("Port type \"" + portType + 
				"\" is unknown to the system.");
		
		return pt;
	}
	
	static public String translate(Collection<PortType> portTypes)
	{
		return _vectorMap.translate(portTypes);
	}
	
	static public String translate(PortType...portTypes)
	{
		return _vectorMap.translate(portTypes);
	}
	
	static public Collection<PortType> translate(String stringRep)
	{
		return _vectorMap.translate(stringRep);
	}
}