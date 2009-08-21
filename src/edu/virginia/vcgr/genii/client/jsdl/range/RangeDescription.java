package edu.virginia.vcgr.genii.client.jsdl.range;

import java.util.Collection;
import java.util.LinkedList;

public class RangeDescription
{
	protected Collection<RangeDescription> _descriptions =
		new LinkedList<RangeDescription>();
	
	Double _upperBound;
	Double _lowerBound;
	
	public void addDescription(RangeDescription desc)
	{
		_descriptions.add(desc);
		
		if (_lowerBound != null)
			//if current is NaN, replace with added
			if (_lowerBound.equals(Double.NaN))
				_lowerBound = desc.getLowerBound();
			//if new is not NaN, then compare
			else if ((!desc.getLowerBound().equals(Double.NaN)) && (desc.getLowerBound() < _lowerBound))
				_lowerBound = desc.getLowerBound();
		
		if (_upperBound != null)
			//if current is NaN, replace with added
			if (_upperBound.equals(Double.NaN))
				_upperBound = desc.getUpperBound();
			//if new is not NaN, then compare
			else if ((desc.getUpperBound().equals(Double.NaN)) && (desc.getUpperBound() > _lowerBound))
				_upperBound = desc.getUpperBound();
	}
	
	public RangeDescription()
	{
		_upperBound = Double.NaN;
		_lowerBound = Double.NaN;
	}
	
	public RangeDescription(Double lowerBound, Double upperBound)
	{
		_upperBound = upperBound;
		_lowerBound = lowerBound;
	}
	
	public Double getUpperBound()
	{
		return _upperBound;
	}
	
	public Double getLowerBound()
	{
		return _lowerBound;
	}
}