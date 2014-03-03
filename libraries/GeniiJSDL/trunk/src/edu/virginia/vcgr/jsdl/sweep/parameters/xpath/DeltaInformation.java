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
package edu.virginia.vcgr.jsdl.sweep.parameters.xpath;

import java.util.Iterator;
import java.util.TreeSet;

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
class DeltaInformation implements Cloneable
{
	static final String USER_HANDLER_KEY = "edu.virginia.vcgr.jsdl.sweep.parameters.xpath.substring-delta-information";

	private TreeSet<DeltaPoint> _deltas;

	private DeltaInformation(TreeSet<DeltaPoint> deltas)
	{
		_deltas = deltas;
	}

	DeltaInformation()
	{
		this(new TreeSet<DeltaPoint>(DeltaPoint.ORDER_COMPARATOR));
	}

	final String replace(String original, String replacement, int start, int length)
	{
		String ret;
		DeltaPoint point = null;
		Iterator<DeltaPoint> iterator = _deltas.iterator();
		int delta;

		while (iterator.hasNext()) {
			point = iterator.next();
			if (point.start() > start)
				break;

			start += point.delta();
		}

		if (length < 0) {
			ret = original.substring(0, start) + replacement;
			delta = replacement.length() - (original.length() - start);
		} else {
			ret = original.substring(0, start) + replacement + original.substring(start + length);
			delta = replacement.length() - length;
		}

		while (point != null) {
			point.shift(delta);

			if (iterator.hasNext())
				point = iterator.next();
			else
				point = null;
		}

		_deltas.add(new DeltaPoint(start, delta));
		return ret;
	}

	@Override
	final public Object clone()
	{
		TreeSet<DeltaPoint> deltas = new TreeSet<DeltaPoint>(DeltaPoint.ORDER_COMPARATOR);
		for (DeltaPoint pt : _deltas)
			deltas.add((DeltaPoint) pt.clone());

		return new DeltaInformation(deltas);
	}

	@Override
	final public String toString()
	{
		return _deltas.toString();
	}

	static UserDataHandler USER_DATA_HANDLER = new UserDataHandler()
	{
		@Override
		public void handle(short operation, String key, Object data, Node src, Node dst)
		{
			if (data != null && src != null && dst != null && src != dst)
				dst.setUserData(key, ((DeltaInformation) data).clone(), this);
		}
	};
}
