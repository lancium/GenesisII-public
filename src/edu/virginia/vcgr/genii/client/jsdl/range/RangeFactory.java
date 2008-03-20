package edu.virginia.vcgr.genii.client.jsdl.range;

import java.util.Collection;
import java.util.LinkedList;

import org.ggf.jsdl.Boundary_Type;
import org.ggf.jsdl.Exact_Type;
import org.ggf.jsdl.RangeValue_Type;
import org.ggf.jsdl.Range_Type;

abstract class CompositeRangeExpression implements RangeExpression
{
	protected Collection<RangeExpression> _expressions =
		new LinkedList<RangeExpression>();
	
	public void addExpression(RangeExpression expr)
	{
		_expressions.add(expr);
	}
}

class CompositeAndRangeExpression extends CompositeRangeExpression
{
	public boolean matches(double testValue)
	{
		for (RangeExpression expr : _expressions)
		{
			if (!expr.matches(testValue))
				return false;
		}
		
		return true;
	}
}

class CompositeOrRangeExpression extends CompositeRangeExpression
{
	public boolean matches(double testValue)
	{
		for (RangeExpression expr : _expressions)
		{
			if (expr.matches(testValue))
				return true;
		}
		
		return false;
	}
}

class ExactValueRangeExpression implements RangeExpression
{
	private double _value;
	private Double _epsilon;
	
	public ExactValueRangeExpression(Exact_Type exact)
	{
		_value = exact.get_value();
		_epsilon = exact.getEpsilon();
	}
	
	public boolean matches(double testValue)
	{
		if (_epsilon != null)
			return Math.abs(testValue - _value) <= _epsilon;
		
		return testValue == _value;
	}
}

abstract class BoundRangeExpression implements RangeExpression
{
	protected double _bound;
	protected boolean _exclusive;
	
	public BoundRangeExpression(Boundary_Type boundary)
	{
		_bound = boundary.get_value();
		Boolean exclusive = boundary.getExclusiveBound();
		_exclusive = (exclusive == null) ? false : true;
	}
}

class LowerBoundRangeExpression extends BoundRangeExpression
{
	public LowerBoundRangeExpression(Boundary_Type boundary)
	{
		super(boundary);
	}
	
	public boolean matches(double testValue)
	{
		if (_exclusive)
			return testValue > _bound;
			
		return testValue >= _bound;
	}
}

class UpperBoundRangeExpression extends BoundRangeExpression
{
	public UpperBoundRangeExpression(Boundary_Type boundary)
	{
		super(boundary);
	}
	
	public boolean matches(double testValue)
	{
		if (_exclusive)
			return testValue < _bound;
			
		return testValue <= _bound;
	}
}

class FullyBoundedRangeExpression extends CompositeAndRangeExpression
{
	public FullyBoundedRangeExpression(Range_Type range)
	{
		addExpression(new LowerBoundRangeExpression(range.getLowerBound()));
		addExpression(new UpperBoundRangeExpression(range.getUpperBound()));
	}
}

public class RangeFactory
{
	static public RangeExpression parse(RangeValue_Type rvt)
	{
		if (rvt == null)
			return null;
		
		CompositeOrRangeExpression ret = new CompositeOrRangeExpression();
		
		Exact_Type []etArray = rvt.getExact();
		Boundary_Type lower = rvt.getLowerBoundedRange();
		Boundary_Type upper = rvt.getUpperBoundedRange();
		Range_Type []rangeArray = rvt.getRange();
		
		if (etArray != null)
		{
			for (Exact_Type et : etArray)
				ret.addExpression(new ExactValueRangeExpression(et));
		}
		
		if (lower != null)
			ret.addExpression(new LowerBoundRangeExpression(lower));
		if (upper != null)
			ret.addExpression(new UpperBoundRangeExpression(upper));
		
		if (rangeArray != null)
		{
			for (Range_Type rt : rangeArray)
			{
				ret.addExpression(new FullyBoundedRangeExpression(rt));
			}
		}
		
		return ret;
	}
}