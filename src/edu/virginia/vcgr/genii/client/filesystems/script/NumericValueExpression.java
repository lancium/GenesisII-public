package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

interface NumericValueExpression
{
	public double evaluate(FilesystemUsageInformation usageInformation);
}