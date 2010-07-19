package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

abstract class BinaryBooleanExpression implements BooleanExpression
{
	protected BooleanExpression _left;
	protected BooleanExpression _right;
	
	protected BinaryBooleanExpression(BooleanExpression left,
		BooleanExpression right)
	{
		_left = left;
		_right = right;
	}
	
	static private class BinaryAndBooleanExpression 
		extends BinaryBooleanExpression
	{
		private BinaryAndBooleanExpression(BooleanExpression left,
			BooleanExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(
			FilesystemUsageInformation usageInformation)
		{
			if (!_left.evaluate(usageInformation))
				return false;
			
			return _right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s && %s)",
				_left, _right);
		}
	}
	
	static private class BinaryOrBooleanExpression 
		extends BinaryBooleanExpression
	{
		private BinaryOrBooleanExpression(BooleanExpression left,
			BooleanExpression right)
		{
			super(left, right);
		}
	
		@Override
		final public boolean evaluate(
			FilesystemUsageInformation usageInformation)
		{
			if (_left.evaluate(usageInformation))
				return true;
			
			return _right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s || %s)",
				_left, _right);
		}
	}
	
	static private class BinaryXorBooleanExpression 
		extends BinaryBooleanExpression
	{
		private BinaryXorBooleanExpression(BooleanExpression left,
			BooleanExpression right)
		{
			super(left, right);
		}
	
		@Override
		final public boolean evaluate(
			FilesystemUsageInformation usageInformation)
		{
			boolean left = _left.evaluate(usageInformation);
			boolean right = _right.evaluate(usageInformation);
			
			return (left || right) && !(left && right);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s ^^ %s)",
				_left, _right);
		}
	}
	
	static BooleanExpression createBinaryBooleanExpression(
		BooleanExpression left, BinaryBooleanOperators operator,
		BooleanExpression right)
	{
		switch (operator)
		{
			case And :
				return new BinaryAndBooleanExpression(left, right);
				
			case Or :
				return new BinaryOrBooleanExpression(left, right);
				
			case Xor :
				return new BinaryXorBooleanExpression(left, right);
		}
		
		return new ConstantBooleanExpression(true);
	}
}