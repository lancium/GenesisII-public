package edu.virginia.vcgr.genii.client.jsdl.sweep;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.EvaluationPlan;

@XmlRootElement(namespace = SweepConstants.SWEEP_NS, name = "Sweep")
public class Sweep implements Serializable, Countable
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
	
	final public Iterator<EvaluationPlan> planIterator()
	{
		if (_subSweeps == null || _subSweeps.size() == 0)
			return new SimplePlanIterator();
		else
			return new ComplexPlanIterator();
	}
	
	// This iterator is used ONLY if there are no sub-sweeps to deal with.
	private class SimplePlanIterator implements Iterator<EvaluationPlan>
	{
		private List<Iterator<EvaluationPlan>> _assignmentPlans;
		
		private SimplePlanIterator()
		{
			_assignmentPlans = new Vector<Iterator<EvaluationPlan>>(
				_assignments.size());
			for (SweepAssignment assignment : _assignments)
				_assignmentPlans.add(assignment.planIterator());
		}
		
		@Override
		final public boolean hasNext()
		{
			return _assignmentPlans.get(0).hasNext();
		}

		@Override
		final public EvaluationPlan next()
		{
			EvaluationPlan ret = new EvaluationPlan();
			
			for (Iterator<EvaluationPlan> plan : _assignmentPlans)
				ret.addPlan(plan.next());

			return ret;
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove items from this iterator.");
		}
	}
	
	private class SubPlanIterator implements Iterator<EvaluationPlan>
	{
		private Iterator<EvaluationPlan> _nextPlan;
		private Iterator<Iterator<EvaluationPlan>> _subPlans;
		
		private SubPlanIterator()
		{
			Collection<Iterator<EvaluationPlan>> subPlans = 
				new LinkedList<Iterator<EvaluationPlan>>();
			for (Sweep subSweep : _subSweeps)
				subPlans.add(subSweep.planIterator());
			_subPlans = subPlans.iterator();
			
			if (_subPlans.hasNext())
				_nextPlan = _subPlans.next();
		}
		
		@Override
		public boolean hasNext()
		{
			if (_nextPlan == null)
				return false;
			
			if (_nextPlan.hasNext())
				return true;
			
			while (_subPlans.hasNext())
			{
				_nextPlan = _subPlans.next();
				if (_nextPlan.hasNext())
					return true;
			}
			
			return false;
		}

		@Override
		public EvaluationPlan next()
		{
			if (_nextPlan == null)
				return null;
			
			if (_nextPlan.hasNext())
				return _nextPlan.next();
			
			while (_subPlans.hasNext())
			{
				_nextPlan = _subPlans.next();
				if (_nextPlan.hasNext())
					return _nextPlan.next();
			}
			
			return null;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove items from this iterator.");
		}
	}
	
	private class ComplexPlanIterator implements Iterator<EvaluationPlan>
	{
		private EvaluationPlan _nextSimplePlan = null;
		private SimplePlanIterator _simpleIterator;
		
		private ComplexPlanIterator()
		{
			_simpleIterator = new SimplePlanIterator();
			if (_simpleIterator.hasNext())
			{
				_nextSimplePlan = _simpleIterator.next();
			}
		}
		
		@Override
		final public boolean hasNext()
		{
			return _nextSimplePlan != null;
		}

		@Override
		final public EvaluationPlan next()
		{
			// TODO
			return null;
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove items from this iterator.");
		}
	}
}
