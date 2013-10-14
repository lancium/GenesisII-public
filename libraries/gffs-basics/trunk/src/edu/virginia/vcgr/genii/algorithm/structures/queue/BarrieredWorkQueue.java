package edu.virginia.vcgr.genii.algorithm.structures.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.graph.DependencyGraph;
import edu.virginia.vcgr.genii.algorithm.graph.GraphException;

public class BarrieredWorkQueue
{
	static private Log _logger = LogFactory.getLog(BarrieredWorkQueue.class);

	private LinkedList<IServiceWithCleanupHook> _workQueue = new LinkedList<IServiceWithCleanupHook>();
	private boolean _released = false;

	final private void run(IServiceWithCleanupHook runnable)
	{
		try {
			runnable.postStartup();
		} catch (Throwable cause) {
			_logger.error("Error running barriered task.", cause);
		}
	}

	final public void enqueue(IServiceWithCleanupHook runnable)
	{
		boolean released;

		synchronized (_workQueue) {
			released = _released;
			if (!released)
				_workQueue.addLast(runnable);
		}

		if (released)
			run(runnable);
	}

	final public void release() throws GraphException
	{
		synchronized (_workQueue) {
			_released = true;
		}

		List<IServiceWithCleanupHook> sortedList = new ArrayList<IServiceWithCleanupHook>(_workQueue);
		Set<Class<?>> classList = new HashSet<Class<?>>();
		for (IServiceWithCleanupHook icm : _workQueue)
			classList.add(icm.getClass());
		DependencyGraph dg = DependencyGraph.buildGraph(classList, "postStartup", new Class<?>[0]);
		Collections.sort(sortedList, dg.createObjectComparator(IServiceWithCleanupHook.class));
		for (IServiceWithCleanupHook runnable : sortedList)
			run(runnable);
	}
}