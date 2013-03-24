package edu.virginia.vcgr.genii.graph;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class DependencyGraph
{
	static private Class<?>[] dependentClasses(Class<?> sourceClass, String sourceMethodName,
		Class<?>[] sourceMethodParameterTypes)
	{
		GridDependency dependency = null;

		try {
			Method sourceMethod = sourceClass.getMethod(sourceMethodName, sourceMethodParameterTypes);
			dependency = sourceMethod.getAnnotation(GridDependency.class);
		} catch (Throwable cause) {
			// Couldn't find the method, but that's no problem, it may not have
			// it.
		}

		if (dependency == null)
			dependency = sourceClass.getAnnotation(GridDependency.class);

		if (dependency == null)
			return new Class<?>[0];

		return dependency.value();
	}

	static public DependencyGraph buildGraph(Collection<Class<?>> serviceClasses, String sourceMethodName,
		Class<?>[] sourceMethodParameterTypes) throws GraphException
	{
		DependencyGraph ret = new DependencyGraph();

		for (Class<?> serviceClass : serviceClasses)
			ret.addClass(serviceClass, sourceMethodName, sourceMethodParameterTypes);

		ret.detectCycles();

		return ret;
	}

	private Map<Class<?>, Set<Class<?>>> _graph = new HashMap<Class<?>, Set<Class<?>>>();

	private DependencyGraph()
	{
	}

	private void addClass(Class<?> cl, String sourceMethodName, Class<?>[] sourceMethodParameterTypes) throws GraphException
	{
		Set<Class<?>> depSet = _graph.get(cl);
		if (depSet != null)
			return;

		_graph.put(cl, depSet = new HashSet<Class<?>>());
		Class<?>[] dependencies = dependentClasses(cl, sourceMethodName, sourceMethodParameterTypes);
		for (Class<?> dep : dependencies) {
			if (dep.equals(cl))
				throw new GraphException(String.format("Error adding %s to graph -- it depends on itself!", cl));
			depSet.add(dep);
		}

		for (Class<?> dep : depSet)
			addClass(dep, sourceMethodName, sourceMethodParameterTypes);
	}

	static private String toString(Class<?> cl)
	{
		String ret = cl.getName();
		int index = ret.lastIndexOf('.');
		if (index >= 0)
			ret = ret.substring(index + 1);

		return ret;
	}

	static private class GraphBasedComparator implements Comparator<Class<?>>
	{
		private Map<Class<?>, Set<Class<?>>> _totalMap;

		private GraphBasedComparator(Map<Class<?>, Set<Class<?>>> totalMap)
		{
			_totalMap = totalMap;
		}

		@Override
		public int compare(Class<?> o1, Class<?> o2)
		{
			Set<Class<?>> o1Set = _totalMap.get(o1);
			if (o1Set == null)
				o1Set = new HashSet<Class<?>>();

			Set<Class<?>> o2Set = _totalMap.get(o2);
			if (o2Set == null)
				o2Set = new HashSet<Class<?>>();

			if (o1Set.contains(o2))
				return 1;
			else if (o2Set.contains(o1))
				return -1;

			return 0;
		}
	}

	static private class GraphBasedObjectComparator<T> implements Comparator<T>
	{
		private Comparator<Class<?>> _classComparator;

		private GraphBasedObjectComparator(Comparator<Class<?>> classComparator)
		{
			_classComparator = classComparator;
		}

		@Override
		public int compare(T o1, T o2)
		{
			return _classComparator.compare(o1.getClass(), o2.getClass());
		}
	}

	private void addAll(Set<Class<?>> totalSet, Class<?> key)
	{
		Set<Class<?>> dep = _graph.get(key);
		if (dep != null) {
			for (Class<?> next : dep) {
				totalSet.add(next);
				addAll(totalSet, next);
			}
		}
	}

	private Map<Class<?>, Set<Class<?>>> createTotalMap()
	{
		Map<Class<?>, Set<Class<?>>> totalMap = new HashMap<Class<?>, Set<Class<?>>>();

		for (Class<?> key : _graph.keySet()) {
			Set<Class<?>> totalSet = new HashSet<Class<?>>();
			totalMap.put(key, totalSet);

			addAll(totalSet, key);
		}

		return totalMap;
	}

	private void throwCycle(LinkedList<Class<?>> visitSet) throws GraphException
	{
		Class<?> last = visitSet.getLast();
		StringBuilder builder = new StringBuilder("Cycle detected in dependency graph:  ");
		boolean started = false;
		for (Class<?> node : visitSet) {
			if (started) {
				builder.append(String.format("->%s", toString(node)));
			} else {
				if (node.equals(last)) {
					started = true;
					builder.append(toString(node));
				}
			}
		}

		throw new GraphException(builder.toString());
	}

	private void detectCycles(LinkedList<Class<?>> visitSet) throws GraphException
	{
		Class<?> current = visitSet.getLast();
		Set<Class<?>> edgeSet = _graph.get(current);
		if (edgeSet != null) {
			for (Class<?> edge : edgeSet) {
				if (visitSet.contains(edge)) {
					visitSet.addLast(edge);
					throwCycle(visitSet);
				} else {
					visitSet.addLast(edge);
					detectCycles(visitSet);
				}
			}
		}

		visitSet.removeLast();
		return;
	}

	private void detectCycles() throws GraphException
	{
		for (Class<?> testClass : _graph.keySet()) {
			LinkedList<Class<?>> visitSet = new LinkedList<Class<?>>();
			visitSet.add(testClass);
			detectCycles(visitSet);
		}
	}

	final public Comparator<Class<?>> createComparator(Collection<Class<?>> classes)
	{
		return new GraphBasedComparator(createTotalMap());
	}

	final public <T> Comparator<T> createObjectComparator(Class<T> objClass)
	{
		return new GraphBasedObjectComparator<T>(new GraphBasedComparator(createTotalMap()));
	}
}