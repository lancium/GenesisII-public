package edu.virginia.vcgr.genii.client.history;

import edu.virginia.vcgr.genii.client.utils.icon.StandardIconProvider;

public enum HistoryEventCategory {
	Default(new DefaultHistoryCategoryInformation("Default")), CreatingJob(
			new DefaultHistoryCategoryInformation("Creating Job",
					new StandardIconProvider("create-job.png"))), CreatingActivity(
			new DefaultHistoryCategoryInformation("Creating Activity",
					new StandardIconProvider("create-activity.png"))), Scheduling(
			new DefaultHistoryCategoryInformation("Scheduling",
					new StandardIconProvider("scheduling.png"))), StageIn(
			new DefaultHistoryCategoryInformation("Stage-In",
					new StandardIconProvider("stage-in.png"))), StageOut(
			new DefaultHistoryCategoryInformation("Stage-Out",
					new StandardIconProvider("stage-out.png"))), ReQueing(
			new DefaultHistoryCategoryInformation("Re-Queing",
					new StandardIconProvider("re-queing.png"))), Checking(
			new DefaultHistoryCategoryInformation("Checking",
					new StandardIconProvider("checking.png"))), Cleanup(
			new DefaultHistoryCategoryInformation("Cleanup",
					new StandardIconProvider("cleanup.png"))), Terminating(
			new DefaultHistoryCategoryInformation("Terminating",
					new StandardIconProvider("terminating.png"))), CloudSetup(
			new DefaultHistoryCategoryInformation("Cloud-Setup",
					new StandardIconProvider("create-job.png")));

	private HistoryCategoryInformation _info;

	private HistoryEventCategory(HistoryCategoryInformation info) {
		if (info == null)
			throw new IllegalArgumentException(
					"Info cannot be null for categories.");

		_info = info;
	}

	final public HistoryCategoryInformation information() {
		return _info;
	}
}