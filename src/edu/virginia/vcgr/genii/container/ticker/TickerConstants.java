package edu.virginia.vcgr.genii.container.ticker;

public interface TickerConstants
{
	static public final String TICKER_FACTORY_NS =
		"http://schemas.ogf.org/ogsa/2007/12/wsrf-bp-interop/ticker-factory";
	static public final String TICKER_NS =
		"http://schemas.ogf.org/ogsa/2007/12/wsrf-bp-interop/ticker";
	
	static public final String TICKER_FACTORY_PORT_NAME =
		"TickerFactory";
	static public final String TICKER_PORT_NAME =
		"Ticker";
	
	static public final long TICKER_RATE = 1000 * 30;
	
	static public final String TICKER_CREATION_PROPERTY =
		"edu.virginia.vcgr.genii.container.ticker.TickerServiceImpl.Ticker-Creation-Property";
}