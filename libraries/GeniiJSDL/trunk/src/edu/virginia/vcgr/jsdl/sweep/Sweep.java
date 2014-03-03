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
package edu.virginia.vcgr.jsdl.sweep;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.jsdl.sweep.eval.Evaluable;
import edu.virginia.vcgr.jsdl.sweep.eval.EvaluationContext;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlRootElement(namespace = SweepConstants.SWEEP_NS, name = "Sweep")
public class Sweep implements Serializable, Countable, Evaluable
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "Assignment", required = true, nillable = false)
	private List<SweepAssignment> _assignments;

	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "Sweep", required = false, nillable = false)
	private List<Sweep> _subSweeps;

	public Sweep(SweepAssignment... assignments)
	{
		_assignments = new Vector<SweepAssignment>(assignments.length);
		for (SweepAssignment assignment : assignments)
			_assignments.add(assignment);

		_subSweeps = new Vector<Sweep>();
	}

	public Sweep()
	{
		_assignments = new Vector<SweepAssignment>();
		_subSweeps = new Vector<Sweep>();
	}

	final public void addAssignment(SweepAssignment assignment)
	{
		_assignments.add(assignment);
	}

	final public void addSubSweep(Sweep subSweep)
	{
		_subSweeps.add(subSweep);
	}

	final public List<SweepAssignment> assignments()
	{
		return _assignments;
	}

	final public List<Sweep> subSweeps()
	{
		return _subSweeps;
	}

	@Override
	final public int size()
	{
		int assignmentSize = 0;
		int subSweepSize;

		if (_assignments.size() > 0)
			assignmentSize = _assignments.get(0).size();

		if (_subSweeps == null || _subSweeps.size() == 0)
			subSweepSize = 1;
		else {
			subSweepSize = 0;
			for (Sweep subSweep : _subSweeps)
				subSweepSize += subSweep.size();
		}

		return assignmentSize * subSweepSize;
	}

	@Override
	public void evaluate(EvaluationContext context) throws SweepException
	{
		boolean emittable = (_subSweeps == null) || (_subSweeps.isEmpty());
		List<Iterator<Evaluable>> concurrentAssignments = new Vector<Iterator<Evaluable>>(_assignments.size());
		Iterator<Evaluable> firstAssignment = null;

		for (SweepAssignment assignment : _assignments) {
			Iterator<Evaluable> e = assignment.iterator();
			if (firstAssignment == null)
				firstAssignment = e;

			concurrentAssignments.add(e);
		}

		while (firstAssignment.hasNext()) {
			EvaluationContext firstCopy = (EvaluationContext) context.clone();
			for (Iterator<Evaluable> i : concurrentAssignments)
				i.next().evaluate(firstCopy);

			if (emittable)
				firstCopy.emit();
			else {
				for (Sweep s : _subSweeps) {
					EvaluationContext secondCopy = (EvaluationContext) firstCopy.clone();
					s.evaluate(secondCopy);
				}
			}
		}
	}
}
