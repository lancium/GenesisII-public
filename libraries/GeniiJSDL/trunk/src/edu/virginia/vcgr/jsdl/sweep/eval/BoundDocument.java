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

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;

import edu.virginia.vcgr.jsdl.JobDefinition;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class BoundDocument {
	static private DocumentBuilder _builder;

	static {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			_builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unable to create XML Node Builder.", e);
		}
	}

	private Binder<Node> _binder;

	private Node _document;
	private JobDefinition _jobDefinition;

	public BoundDocument(Binder<Node> binder, JobDefinition jobDefinition)
			throws JAXBException {
		_binder = binder;
		_jobDefinition = jobDefinition;
		_document = _builder.newDocument();
		_binder.marshal(_jobDefinition, _document);
	}

	final Binder<Node> binder() {
		return _binder;
	}

	final Node document() {
		return _document;
	}

	final JobDefinition jobDefinition() {
		return _jobDefinition;
	}
}
