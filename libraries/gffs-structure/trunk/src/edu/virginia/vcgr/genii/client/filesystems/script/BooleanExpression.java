package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

interface BooleanExpression
{
	public boolean evaluate(FilesystemUsageInformation usageInformation);
}