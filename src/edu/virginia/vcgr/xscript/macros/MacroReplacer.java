package edu.virginia.vcgr.xscript.macros;

import java.util.Stack;

import javax.script.ScriptContext;

public class MacroReplacer
{
	static private MacroExpression findMacroSubExpression(
		MacroTokenizer tokenizer)
	{
		Stack<MacroExpression> stack = new Stack<MacroExpression>();
		String token;
		
		while ( (token = tokenizer.nextToken()) != null)
		{
			MacroExpression expr;
			
			if (token.equals("${"))
				expr = new LookupExpression(
					findMacroSubExpression(tokenizer));
			else if (token.equals("}"))
			{
				if (stack.isEmpty())
					return new ConstantExpression("");
				return stack.pop();
			} else
			{
				expr = new ConstantExpression(token);
			}
			
			if (stack.isEmpty())
				stack.push(expr);
			else
				stack.push(new ConcatenateExpression(stack.pop(), expr));
		}
		
		throw new RuntimeException(
			"Unable to parse line...couldn't locate end of macro.");
	}
	
	static public String replaceMacros(ScriptContext variables, String input)
	{
		Stack<MacroExpression> stack = new Stack<MacroExpression>();
		MacroTokenizer tokenizer = new MacroTokenizer(input);
		String token;
		
		while ( (token = tokenizer.nextToken()) != null)
		{
			MacroExpression expr;
			
			if (token.equals("${"))
			{
				expr = new LookupExpression(
					findMacroSubExpression(tokenizer));
			} else
				expr = new ConstantExpression(token);
			
			if (stack.isEmpty())
				stack.push(expr);
			else
				stack.push(new ConcatenateExpression(stack.pop(), expr));
		}
		
		if (stack.isEmpty())
			return "";
		
		return stack.pop().toString(variables);
	}
}