package edu.virginia.vcgr.genii.container.stats;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodStatisticsReport
{
	private MethodStatisticsReportPoint _totals;
	private Map<Class<?>, MethodStatisticsReportPoint> _classTotals;
	private Map<Class<?>, Map<Method, MethodStatisticsReportPoint>> _methodTotals;
	
	public MethodStatisticsReport()
	{
		_totals = new MethodStatisticsReportPoint();
		_classTotals = new HashMap<Class<?>, MethodStatisticsReportPoint>();
		_methodTotals = new HashMap<Class<?>, Map<Method,MethodStatisticsReportPoint>>();
	}
	
	void add(MethodDataPoint dp)
	{
		_totals.add(dp);
		
		MethodStatisticsReportPoint point = _classTotals.get(dp.serviceClass());
		if (point == null)
			_classTotals.put(dp.serviceClass(), point = new MethodStatisticsReportPoint());
		point.add(dp);
		
		Map<Method, MethodStatisticsReportPoint> map = _methodTotals.get(dp.serviceClass());
		if (map == null)
			_methodTotals.put(dp.serviceClass(), map = new HashMap<Method, MethodStatisticsReportPoint>());
		point = map.get(dp.serviceMethod());
		if (point == null)
			map.put(dp.serviceMethod(), point = new MethodStatisticsReportPoint());
		point.add(dp);
	}
	
	public MethodStatisticsReportPoint totals()
	{
		return _totals;
	}
	
	public Map<Class<?>, MethodStatisticsReportPoint> classTotals()
	{
		return _classTotals;
	}
	
	public Map<Class<?>, Map<Method, MethodStatisticsReportPoint>> methodTotals()
	{
		return _methodTotals;
	}
}