package edu.virginia.vcgr.genii.client.pipe;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;

@XmlAccessorType(XmlAccessType.NONE)
public class PipeConstructionParameters
	extends ConstructionParameters implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private int DEFAULT_PIPE_SIZE = 1024 * 1024;
	
	static final public String PIPE_CONS_PARMS_NS =
		PipeConstants.PIPE_NS;
	
	@XmlElement(namespace = PIPE_CONS_PARMS_NS, name = "pipe-size", nillable = true,
		required = false)
	private Integer _pipeSize = null;
	
	final public int pipeSize()
	{
		if (_pipeSize == null)
			return DEFAULT_PIPE_SIZE;
		
		return _pipeSize;
	}
}