package edu.virginia.vcgr.smb.server;

public class SMBTransactionInfo
{
	SMBBuffer params;
	SMBBuffer data;
	SMBBuffer setup;
	int paramCount;
	int dataCount;

	// For return
	int maxSetupCount;
	int maxParamCount;
	int maxDataCount;

	public SMBTransactionInfo(SMBBuffer transParam, SMBBuffer transData, int maxParamCount, int maxDataCount, SMBBuffer setup,
		int maxSetupCount)
	{
		this.params = transParam;
		this.data = transData;
		this.maxParamCount = maxParamCount;
		this.maxDataCount = maxDataCount;
		this.setup = setup;
		this.maxSetupCount = maxSetupCount;
	}

	public SMBTransactionInfo(int totalParamCount, int totalDataCount, int maxParamCount, int maxDataCount, SMBBuffer setup,
		int maxSetupCount)
	{
		this.params = SMBBuffer.allocateBuffer(totalParamCount);
		this.data = SMBBuffer.allocateBuffer(totalDataCount);
		this.maxParamCount = maxParamCount;
		this.maxDataCount = maxDataCount;
		this.setup = setup;
		this.maxSetupCount = maxSetupCount;
	}

	public boolean recv(SMBBuffer transParam, int paramDisp, int totalParamCount, SMBBuffer transData, int dataDisp, int totalDataCount)
	{
		if (totalParamCount < params.limit())
			params.limit(totalParamCount);
		if (totalDataCount < data.limit())
			data.limit(totalDataCount);

		paramCount += transParam.remaining();
		dataCount += transData.remaining();

		params.put(paramDisp, transParam);
		data.put(dataDisp, transData);

		return paramCount == params.limit() && dataCount == data.limit();
	}

	public SMBBuffer getSetup()
	{
		return setup;
	}

	public int getMaxSetupCount()
	{
		return maxSetupCount;
	}

	public int getMaxParamCount()
	{
		return maxParamCount;
	}

	public int getMaxDataCount()
	{
		return maxDataCount;
	}

	public SMBBuffer getParams()
	{
		return params;
	}

	public SMBBuffer getData()
	{
		return data;
	}
}