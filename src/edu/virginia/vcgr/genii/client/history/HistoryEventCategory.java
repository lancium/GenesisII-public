package edu.virginia.vcgr.genii.client.history;

import edu.virginia.vcgr.genii.client.utils.icon.DefaultIconProvider;

public enum HistoryEventCategory
{
	Default(new DefaultHistoryCategoryInformation("Default")),
	CreatingJob(new DefaultHistoryCategoryInformation("Creating Job",
		DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/create-job.png"))),
	CreatingActivity(new DefaultHistoryCategoryInformation("Creating Activity",
			DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/create-activity.png"))),
	Scheduling(new DefaultHistoryCategoryInformation("Scheduling",
		DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/scheduling.png"))),
	StageIn(new DefaultHistoryCategoryInformation("Stage-In",
		DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/stage-in.png"))),
	StageOut(new DefaultHistoryCategoryInformation("Stage-Out",
		DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/stage-out.png"))),
	ReQueing(new DefaultHistoryCategoryInformation("Re-Queing",
			DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/re-queing.png"))),
	Checking(new DefaultHistoryCategoryInformation("Checking",
			DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/checking.png"))),
	Cleanup(new DefaultHistoryCategoryInformation("Cleanup",
			DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/cleanup.png"))),
	Terminating(new DefaultHistoryCategoryInformation("Terminating",
		DefaultIconProvider.createIconProvider(HistoryEventCategory.class,
			"resources/terminating.png")));
	
	private HistoryCategoryInformation _info;
	
	private HistoryEventCategory(HistoryCategoryInformation info)
	{
		if (info == null)
			throw new IllegalArgumentException(
				"Info cannot be null for categories.");
		
		_info = info;
	}
	
	final public HistoryCategoryInformation information()
	{
		return _info;
	}
}