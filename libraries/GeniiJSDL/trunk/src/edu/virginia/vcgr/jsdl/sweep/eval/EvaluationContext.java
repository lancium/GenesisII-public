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
package edu.virginia.vcgr.jsdl.sweep.eval;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.SweepListener;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class EvaluationContext implements Cloneable
{
	private SweepListener _listener;
	private Node _document;
	private Unmarshaller _unmarshaller;

	public EvaluationContext(SweepListener listener, Unmarshaller unmarshaller, Node document)
	{
		_listener = listener;
		_unmarshaller = unmarshaller;
		_document = document;
	}

	@Override
	final public Object clone()
	{
		return new EvaluationContext(_listener, _unmarshaller, _document.cloneNode(true));
	}

	final public void emit() throws SweepException
	{
		try {
			_listener.emitSweepInstance((JobDefinition) _unmarshaller.unmarshal(new DOMSource(_document)));
		} catch (JAXBException e) {
			throw new SweepException("Unable to unmarshall sweep result into JSDL document.", e);
		}
	}

	final public Node document()
	{
		return _document;
	}
}
