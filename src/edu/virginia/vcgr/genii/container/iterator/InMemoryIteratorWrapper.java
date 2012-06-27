package edu.virginia.vcgr.genii.container.iterator;

import java.util.LinkedList;
import java.util.List;

public class InMemoryIteratorWrapper 
{
	private String _className;
	private List<InMemoryIteratorEntry> _indices = new LinkedList<InMemoryIteratorEntry>();
	private Object[] _commonMember;
	
	public InMemoryIteratorWrapper(String className, List<InMemoryIteratorEntry> indices, 
			Object[] commonMember) 
	{
		_className = className;
		if(indices!=null)
			_indices.addAll(indices);
		_commonMember = commonMember;
	}

	public String getClassName() {
		return _className;
	}

	public List<InMemoryIteratorEntry> getIndices() {
		return _indices;
	}

	public Object[] getCommonMembers() {
		return _commonMember;
	}
}
