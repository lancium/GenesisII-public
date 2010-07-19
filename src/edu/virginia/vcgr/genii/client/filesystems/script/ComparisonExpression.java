package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

abstract class ComparisonExpression implements BooleanExpression
{
	protected NumericValueExpression _left;
	protected NumericValueExpression _right;
	
	protected ComparisonExpression(NumericValueExpression left,
		NumericValueExpression right)
	{
		_left = left;
		_right = right;
	}
	
	static private class LessComparisonExpression extends ComparisonExpression
	{
		private LessComparisonExpression(NumericValueExpression left,
			NumericValueExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(FilesystemUsageInformation usageInformation)
		{
			return _left.evaluate(usageInformation) < 
				_right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s < %s)",
				_left, _right);
		}
	}
	
	static private class LessEqualsComparisonExpression extends ComparisonExpression
	{
		private LessEqualsComparisonExpression(NumericValueExpression left,
			NumericValueExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(FilesystemUsageInformation usageInformation)
		{
			return _left.evaluate(usageInformation) <= 
				_right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s <= %s)",
				_left, _right);
		}
	}
	
	static private class NotEqualsComparisonExpression extends ComparisonExpression
	{
		private NotEqualsComparisonExpression(NumericValueExpression left,
			NumericValueExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(FilesystemUsageInformation usageInformation)
		{
			return _left.evaluate(usageInformation) != 
				_right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s != %s)",
				_left, _right);
		}
	}
	
	static private class EqualsComparisonExpression extends ComparisonExpression
	{
		private EqualsComparisonExpression(NumericValueExpression left,
			NumericValueExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(FilesystemUsageInformation usageInformation)
		{
			return _left.evaluate(usageInformation) == 
				_right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s == %s)",
				_left, _right);
		}
	}
	
	static private class GreaterEqualsComparisonExpression extends ComparisonExpression
	{
		private GreaterEqualsComparisonExpression(NumericValueExpression left,
			NumericValueExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(FilesystemUsageInformation usageInformation)
		{
			return _left.evaluate(usageInformation) >= 
				_right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s >= %s)",
				_left, _right);
		}
	}
	
	static private class GreaterComparisonExpression extends ComparisonExpression
	{
		private GreaterComparisonExpression(NumericValueExpression left,
			NumericValueExpression right)
		{
			super(left, right);
		}

		@Override
		final public boolean evaluate(FilesystemUsageInformation usageInformation)
		{
			return _left.evaluate(usageInformation) > 
				_right.evaluate(usageInformation);
		}
		
		@Override
		final public String toString()
		{
			return String.format("(%s > %s)",
				_left, _right);
		}
	}
	
	static BooleanExpression createComparisonExpression(
		NumericValueExpression left, ComparisonOperators operator,
		NumericValueExpression right)
	{
		switch (operator)
		{
			case LessThan :
				return new LessComparisonExpression(left, right);
				
			case LessThanOrEquals :
				return new LessEqualsComparisonExpression(left, right);
				
			case NotEquals :
				return new NotEqualsComparisonExpression(left, right);
				
			case Equals :
				return new EqualsComparisonExpression(left, right);
				
			case GreaterThanOrEquals :
				return new GreaterEqualsComparisonExpression(left, right);
				
			case GreaterThan :
				return new GreaterComparisonExpression(left, right);
		}
		
		return new ConstantBooleanExpression(true);
	}
}