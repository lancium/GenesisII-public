package edu.virginia.vcgr.genii.client.jsdl.sweep;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.Evaluable;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.EvaluationContext;

@XmlRootElement(namespace = SweepConstants.SWEEP_NS, name = "Sweep")
public class Sweep implements Serializable, Countable, Evaluable
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "Assignment",
		required = true, nillable = false)
	private List<SweepAssignment> _assignments;
	
	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "Sweep",
		required = false, nillable = false)
	private List<Sweep> _subSweeps;

	public Sweep(SweepAssignment...assignments)
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
	
	@Override
	final public int size()
	{
		if (_assignments.size() == 0)
			return 0;
		
		return _assignments.get(0).size();
	}

	@Override
	public void evaluate(EvaluationContext context) throws SweepException
	{
		boolean emittable = (_subSweeps == null) || (_subSweeps.isEmpty());
		List<Iterator<Evaluable>> concurrentAssignments = 
			new Vector<Iterator<Evaluable>>(_assignments.size());
		Iterator<Evaluable> firstAssignment = null;
		
		for (SweepAssignment assignment : _assignments)
		{
			Iterator<Evaluable> e = assignment.iterator();
			if (firstAssignment == null)
				firstAssignment = e;
			
			concurrentAssignments.add(e);
		}
		
		while (firstAssignment.hasNext())
		{
			EvaluationContext firstCopy = (EvaluationContext)context.clone();
			for (Iterator<Evaluable> i : concurrentAssignments)
				i.next().evaluate(firstCopy);
			
			if (emittable)
				firstCopy.emit();
			else
			{
				for (Sweep s : _subSweeps)
				{
					EvaluationContext secondCopy = 
						(EvaluationContext)firstCopy.clone();
					s.evaluate(secondCopy);
				}
			}
		}
	}
}