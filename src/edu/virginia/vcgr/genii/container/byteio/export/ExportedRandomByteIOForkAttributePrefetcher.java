package edu.virginia.vcgr.genii.container.byteio.export;

import java.util.Calendar;

import edu.virginia.vcgr.genii.container.byteio.RandomByteIOAttributePreFetcher;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.LightWeightExportUtils;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportFile;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class ExportedRandomByteIOForkAttributePrefetcher extends RandomByteIOAttributePreFetcher<IResource>
{
	
	private ResourceKey _rKey;
	private String _forkPath;
	
	public ExportedRandomByteIOForkAttributePrefetcher(ResourceKey rKey,
			String forkPath) 
	{
		super(rKey.dereference());	
		_rKey = rKey;
		_forkPath = forkPath;
	}
	
	@Override
	protected Long getSize() throws Throwable
	{
		VExportFile target = LightWeightExportUtils.getFile(_forkPath, _rKey);
		return target.size();
	}

	@Override
	protected Calendar getAccessTime() throws Throwable {
		VExportFile target = LightWeightExportUtils.getFile(_forkPath, _rKey);
		return target.accessTime();
	}

	@Override
	protected Calendar getModificationTime() throws Throwable {
		VExportFile target = LightWeightExportUtils.getFile(_forkPath, _rKey);
		return target.modificationTime();
	}

	@Override
	protected Calendar getCreateTime() throws Throwable 
	{
		VExportFile target = LightWeightExportUtils.getFile(_forkPath, _rKey);
		return target.createTime();
	}

}
