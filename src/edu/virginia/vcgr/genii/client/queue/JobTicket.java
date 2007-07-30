package edu.virginia.vcgr.genii.client.queue;

public class JobTicket implements Comparable<JobTicket>
{
	private String _ticketValue;
	
	public JobTicket(String ticketValue)
	{
		_ticketValue = ticketValue;
	}
	
	public int hashCode()
	{
		return _ticketValue.hashCode();
	}
	
	public boolean equals(JobTicket ticket)
	{
		return _ticketValue.equals(ticket._ticketValue);
	}
	
	public boolean equals(Object other)
	{
		return equals((JobTicket)other);
	}
	
	public String toString()
	{
		return _ticketValue;
	}

	public int compareTo(JobTicket arg0)
	{
		return _ticketValue.compareTo(arg0._ticketValue);
	}
}