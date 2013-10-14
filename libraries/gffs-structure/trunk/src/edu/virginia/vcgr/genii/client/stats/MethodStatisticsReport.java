package edu.virginia.vcgr.genii.client.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MethodStatisticsReport implements Serializable
{
	static final long serialVersionUID = 0L;

	private MethodStatisticsReportPoint _totals;
	private Map<String, MethodStatisticsReportPoint> _classTotals;
	private Map<String, Map<String, MethodStatisticsReportPoint>> _methodTotals;

	public MethodStatisticsReport()
	{
		_totals = new MethodStatisticsReportPoint();
		_classTotals = new HashMap<String, MethodStatisticsReportPoint>();
		_methodTotals = new HashMap<String, Map<String, MethodStatisticsReportPoint>>();
	}

	void add(MethodDataPoint dp)
	{
		_totals.add(dp);

		MethodStatisticsReportPoint point = _classTotals.get(dp.serviceClass().getName());
		if (point == null)
			_classTotals.put(dp.serviceClass().getName(), point = new MethodStatisticsReportPoint());
		point.add(dp);

		Map<String, MethodStatisticsReportPoint> map = _methodTotals.get(dp.serviceClass().getName());
		if (map == null)
			_methodTotals.put(dp.serviceClass().getName(), map = new HashMap<String, MethodStatisticsReportPoint>());
		point = map.get(dp.serviceMethod().getName());
		if (point == null)
			map.put(dp.serviceMethod().getName(), point = new MethodStatisticsReportPoint());
		point.add(dp);
	}

	public MethodStatisticsReportPoint totals()
	{
		return _totals;
	}

	public Map<String, MethodStatisticsReportPoint> classTotals()
	{
		return _classTotals;
	}

	public Map<String, Map<String, MethodStatisticsReportPoint>> methodTotals()
	{
		return _methodTotals;
	}
}