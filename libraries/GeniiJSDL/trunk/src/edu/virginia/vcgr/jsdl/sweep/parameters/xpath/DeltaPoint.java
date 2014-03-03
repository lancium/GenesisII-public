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

import java.util.Comparator;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
class DeltaPoint implements Cloneable {
	private int _start;
	private int _delta;

	DeltaPoint(int start, int delta) {
		_start = start;
		_delta = delta;
	}

	final int start() {
		return _start;
	}

	final int delta() {
		return _delta;
	}

	final void shift(int delta) {
		_start += delta;
	}

	@Override
	final public Object clone() {
		return new DeltaPoint(_start, _delta);
	}

	@Override
	final public String toString() {
		return String.format("%d%+d", _start, _delta);
	}

	static Comparator<DeltaPoint> ORDER_COMPARATOR = new Comparator<DeltaPoint>() {
		@Override
		final public int compare(DeltaPoint o1, DeltaPoint o2) {
			return o1._start - o2._start;
		}
	};
}
