package edu.virginia.vcgr.genii.client.cmd.tools;


import java.text.ParseException;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;


public abstract class BaseLoginTool extends BaseGridTool{

	
	static public final String PKCS12 = "PKCS12";
	static public final String JKS = "JKS";
	static public final String WINDOWS = "WIN";
	
	protected String _password = null;
	protected String _storeType = null;
	protected String _durationString = null;
	protected long _validMillis = GenesisIIConstants.CredentialExpirationMillis;
	protected boolean _aliasPatternFlag = false;
	protected boolean _replaceClientToolIdentityFlag = false;
	protected String _username = null;
	protected String _pattern = null;
	protected String _authnUri = null;

	protected BaseLoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}
	
	public BaseLoginTool() {
		super("", "", false);
	}

	@Option({"storetype"})
	public void setStoretype(String storeType) {
		_storeType = storeType;
	}

	@Option({"password"})
	public void setPassword(String password) {
		_password = password;
	}

	@Option({"username"})
	public void setUsername(String username) {
		_username = username;
	}

	@Option({"alias"})
	public void setAlias() {
		_aliasPatternFlag = true;
	}
	
	@Option({"toolIdentity"})
	public void setToolIdentity() {
		_replaceClientToolIdentityFlag = true;
	}

	@Option({"pattern"})
	public void setPattern(String pattern) {
		_pattern = pattern;
	}

	@Option({"validDuration"})
	public void setValidDuration(String durationString) 
		throws ParseException
	{
		_durationString = durationString;
	}
	
	
	//Copys t1's creds to t2
	public static final void copyCreds(BaseLoginTool t1, BaseLoginTool t2){
		t2.setStoretype(t1._storeType);
		t2.setPassword(t1._password);
		t2.setUsername(t1._username);
		t2._aliasPatternFlag = t1._aliasPatternFlag;
		t2._replaceClientToolIdentityFlag = t1._replaceClientToolIdentityFlag;
		t2._durationString = t1._durationString;
		t2._authnUri = t1._authnUri;
		t2._validMillis = t1._validMillis;
		t2._pattern = t1._pattern;
	}
	
	
}
