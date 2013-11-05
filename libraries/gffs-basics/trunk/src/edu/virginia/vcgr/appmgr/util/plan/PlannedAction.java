package edu.virginia.vcgr.appmgr.util.plan;

import java.io.Closeable;

public interface PlannedAction<PlanContext extends Closeable>
{
	public void perform(PlanContext planContext) throws Throwable;
}