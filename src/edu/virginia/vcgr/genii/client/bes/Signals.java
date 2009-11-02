package edu.virginia.vcgr.genii.client.bes;

public enum Signals
{
	SIGHUP(1),
	SIGINT(2),
	SIGQUIT(3),
	SIGILL(4),
	SIGTRAP(5),
	SIGABRT(6),
	SIGEMT(7),
	SIGFPE(8),
	SIGPKILL(9),
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
	
	private Signals(int signalNumber)
	{
		_signalNumber = signalNumber;
	}
	
	public int signalNumber()
	{
		return _signalNumber;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s(%d)", name(), _signalNumber);
	}
}