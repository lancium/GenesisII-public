/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.virginia.vcgr.jsdl.sweep.parameters;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTargetIdentifier;
import edu.virginia.vcgr.jsdl.sweep.parameters.xpath.XPathTargetIdentifierFactory;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class DocumentNodeSweepParameter implements SweepParameter, Serializable {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "NamespaceBinding", required = false, nillable = false)
	private List<NamespaceBinding> _bindings;

	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "Match", required = true, nillable = false)
	private String _matchExpression;

	public DocumentNodeSweepParameter(String matchExpression,
			NamespaceBinding... bindings) {
		_matchExpression = matchExpression;
		_bindings = new Vector<NamespaceBinding>(bindings.length);

		for (NamespaceBinding binding : bindings)
			_bindings.add(binding);
	}

	public DocumentNodeSweepParameter(String matchExpression,
			List<NamespaceBinding> bindings) {
		_matchExpression = matchExpression;
		_bindings = new Vector<NamespaceBinding>(bindings);
	}

	public DocumentNodeSweepParameter() {
		this(null);
	}

	final public List<NamespaceBinding> bindings() {
		return _bindings;
	}

	final public String matchExpression() {
		return _matchExpression;
	}

	@Override
	final public SweepTargetIdentifier targetIdentifier() throws SweepException {
		return XPathTargetIdentifierFactory.createIdentifier(
				new NamespaceBindingsContext(_bindings), _matchExpression);
	}
}
