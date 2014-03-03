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
package edu.virginia.vcgr.jsdlval;

import java.util.Deque;
import java.util.LinkedList;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
class XMLDocumentPathImpl implements XMLDocumentPath {
	private Deque<QName> _path;

	private XMLDocumentPathImpl(Deque<QName> path) {
		if (path != null)
			_path = new LinkedList<QName>(path);
		else
			_path = new LinkedList<QName>();
	}

	XMLDocumentPathImpl() {
		this(null);
	}

	final QName formQNameFromPrevious(String localName) {
		if (_path.isEmpty())
			return new QName(localName);

		QName previous = _path.peek();
		String ns = previous.getNamespaceURI();
		String prefix = previous.getPrefix();

		return new QName(ns == null ? XMLConstants.NULL_NS_URI : ns, localName,
				prefix == null ? XMLConstants.DEFAULT_NS_PREFIX : prefix);
	}

	final void push(QName pathElement) {
		_path.push(pathElement);
	}

	final void push(String localName) {
		push(new QName(localName));
	}

	final void push(String ns, String localName) {
		push(new QName(ns, localName));
	}

	final void push(String ns, String localName, String prefix) {
		push(new QName(ns, localName, prefix));
	}

	final void pop() {
		_path.pop();
	}

	@Override
	final public QName currentElement() {
		return _path.peek();
	}

	@Override
	final public QName[] currentPath() {
		return _path.toArray(new QName[_path.size()]);
	}

	@Override
	final public String toString() {
		StringBuilder builder = new StringBuilder();
		for (QName element : _path) {
			if (builder.length() != 0)
				builder.append("/");

			builder.append(element);
		}

		return builder.toString();
	}

	@Override
	final public Object clone() {
		return new XMLDocumentPathImpl(_path);
	}
}
