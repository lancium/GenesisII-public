package edu.virginia.vcgr.genii.client.nativeq;

import java.io.IOException;
import java.util.EnumSet;

public enum UnixSignals
{
	SIGHUP(1),
	SIGINT(2),
	SIGQUIT(3),
	SIGILL(4),
	SIGTRAP(5),
	SIGABRT(6),
	SIGEMT(7),
	SIGFPE(8),
	SIGKILL(9),
	SIGBUS(10),
	SIGSEGV(11),
	SIGSYS(12),
	SIGPIPE(13),
	SIGALRM(14),
	SIGTERM(15),
	SIGURG(16),
	SIGSTOP(17),
	SIGTSTP(18),
	SIGCONT(19),
	SIGCHLD(20),
	SIGTTIN(21),
	SIGTTOU(22),
	SIGIO(23),
	SIGXCPU(24),
	SIGXFSZ(25),
	SIGVTALRM(26),
	SIGPROF(27),
	SIGWINCH(28),
	SIGINFO(29),
	SIGUSR1(30),
	SIGUSR2(31);
	
	private int _signalNumber;
	
	private UnixSignals(int signalNumber)
	{
		_signalNumber = signalNumber;
	}
	
	public int signalNumber()
	{
		return _signalNumber;
	}
	
	public boolean isTrappable()
	{
		return (this != SIGSTOP) && (this != SIGKILL);
	}
	
	static public EnumSet<UnixSignals> parseTrapAndKillSet(String description)
		throws IOException
	{
		EnumSet<UnixSignals> ret = EnumSet.noneOf(UnixSignals.class);
		
		if (description == null)
			return ret;
		
		for (String signal : description.split(","))
		{
			if (signal == null)
				continue;
			signal = signal.trim();
			if (signal.length() == 0)
				continue;
			signal = signal.toUpperCase();
			if (!signal.startsWith("SIG"))
				signal = "SIG" + signal;
			
			UnixSignals uSignal = UnixSignals.valueOf(signal);
			if (uSignal == null)
				throw new IOException(String.format(
					"Cannot find signal \"%s\".", signal));
			
			if (!uSignal.isTrappable())
				throw new IOException(String.format(
					"Signal \"%s\" cannot be trapped.", signal));
			
			ret.add(uSignal);
		}
		
		return ret;
	}
}
