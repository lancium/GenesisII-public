package edu.virginia.vcgr.genii.client.comm.socket;

public enum TrafficClass {
	LOWCOST(0x02), RELIABILITY(0x04), THROUGHPUT(0x08), LOWDELAY(0x10);

	private int _value;

	private TrafficClass(int value)
	{
		_value = value;
	}

	public int trafficClassBitVector()
	{
		return _value;
	}
}