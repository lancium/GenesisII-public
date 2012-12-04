package edu.virginia.vcgr.genii.client.gui.bes;

import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractListModel;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.WSName;

@SuppressWarnings("rawtypes")
public class BESListModel extends AbstractListModel
{
	static final long serialVersionUID = 0L;
	
	private Map<String, WSName> _besContainers;
	private Vector<String> _sortedPaths;
	
	public BESListModel()
	{
		_besContainers = BESState.knownBESContainers();
		_sortedPaths = new Vector<String>(_besContainers.keySet());
		Collections.sort(_sortedPaths);
	}
	
	@Override
	public Object getElementAt(int index)
	{
		String path = _sortedPaths.get(index);
		return new BESBundle(path, _besContainers.get(path).getEndpoint());
	}

	@Override
	public int getSize()
	{
		return _sortedPaths.size();
	}
	
	public void addBES(String path, EndpointReferenceType target)
	{
		int oldSize = _sortedPaths.size();
		BESState.addKnownBESContainer(path, target);
		_besContainers = BESState.knownBESContainers();
		_sortedPaths = new Vector<String>(_besContainers.keySet());
		Collections.sort(_sortedPaths);
		fireContentsChanged(this, 0, oldSize);
	}
	
	public void removeBES(String path)
	{
		int oldSize = _sortedPaths.size();
		
		BESState.removeKnownBESContainer(path);
		_besContainers = BESState.knownBESContainers();
		_sortedPaths = new Vector<String>(_besContainers.keySet());
		Collections.sort(_sortedPaths);
		fireContentsChanged(this, 0, oldSize);
	}
}