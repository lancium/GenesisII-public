package edu.virginia.vcgr.genii.client.jsdl.sweep;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlSeeAlso;

import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;
import edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.DocumentNodeSweepParameter;

@XmlSeeAlso({
	DocumentNodeSweepParameter.class
})
public abstract class SweepParameter implements Serializable
{
	static final long serialVersionUID = 0L;
	
	public abstract SweepTargetIdentifier targetIdentifier() 
		throws SweepException;
}