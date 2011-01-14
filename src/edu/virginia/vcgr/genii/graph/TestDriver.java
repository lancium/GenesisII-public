package edu.virginia.vcgr.genii.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestDriver
{
	@GridDependency({Beta.class, Gamma.class})
	static private class Alpha
	{
	}
	
	@GridDependency(Delta.class)
	static private class Beta
	{
	}
	
	@GridDependency(Delta.class)
	static private class Gamma
	{
	}
	
	@GridDependency(Epsilon.class)
	static private class Delta
	{
	}
	
	static private class Epsilon
	{
	}
	
	static public void main(String []args) throws Throwable
	{
		Collection<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(Alpha.class);
		classes.add(Beta.class);
		classes.add(Gamma.class);
		classes.add(Delta.class);
		classes.add(Epsilon.class);
		
		DependencyGraph graph = DependencyGraph.buildGraph(classes, 
			"postStartup", new Class<?>[0]);
		List<Class<?>> sorted = new ArrayList<Class<?>>(classes);
		Collections.sort(sorted, graph.createComparator(classes));
		System.out.println("Order:");
		for (Class<?> cl : sorted)
			System.out.format("\t%s\n", cl);
	}
}