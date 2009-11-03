
package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters;

import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepParameter;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;
import edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.xpath.XPathTargetIdentifierFactory;

public class DocumentNodeSweepParameter extends SweepParameter
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "NamespaceBinding",
		required = false, nillable = false)
	private List<NamespaceBinding> _bindings;
	
	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "Match",
		required = true, nillable = false)
	private String _matchExpression;
	
	public DocumentNodeSweepParameter(String matchExpression,
		NamespaceBinding...bindings)
	{
		_matchExpression = matchExpression;
		_bindings = new Vector<NamespaceBinding>(bindings.length);
		
		for (NamespaceBinding binding : bindings)
			_bindings.add(binding);
	}
	
	public DocumentNodeSweepParameter(String matchExpression,
		List<NamespaceBinding> bindings)
	{
		_matchExpression = matchExpression;
		_bindings = new Vector<NamespaceBinding>(bindings);
	}
	
	public DocumentNodeSweepParameter()
	{
		this(null);
	}
	
	@Override
	final public SweepTargetIdentifier targetIdentifier() throws SweepException
	{
		return XPathTargetIdentifierFactory.createIdentifier(
			new NamespaceBindingsContext(_bindings), _matchExpression);
	}
}