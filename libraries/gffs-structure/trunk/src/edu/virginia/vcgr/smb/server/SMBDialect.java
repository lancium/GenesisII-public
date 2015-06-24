package edu.virginia.vcgr.smb.server;

public enum SMBDialect {
	CORE(0),
	LM10(1),
	LM12(2),
	LM20(3),
	LM21(4),
	NTLM(5);

	private int level;

	private SMBDialect(int level)
	{
		this.level = level;
	}

	public boolean atLeast(SMBDialect other)
	{
		return this.level >= other.level;
	}
}
