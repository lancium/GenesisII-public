package edu.virginia.vcgr.genii.client.cmd.tools;

import java.text.ParseException;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.login.AbstractLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.login.GuiLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.login.TextLoginHandler;
import edu.virginia.vcgr.genii.client.configuration.UserPreferences;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

// future: fix non-sensical members at base. divide into different derived bases using taxonomy.

public abstract class BaseLoginTool extends BaseGridTool
{
	static public final String PKCS12 = "PKCS12";
	static public final String JKS = "JKS";
	static public final String WINDOWS = "WIN";

	protected String _password = null;
	protected String _storeType = null;
	protected String _durationString = null;
	protected boolean _aliasPatternFlag = false;
	protected boolean _replaceClientToolIdentityFlag = false;
	protected String _username = null;
	protected String _pattern = null;
	protected String _authnUri = null;
	protected boolean _create_nonce=false;
	protected boolean _push_nonce=false;
	protected String _nonce=null;
	protected boolean _pop_nonce=false;

	protected BaseLoginTool(String description, String usage, boolean isHidden)
	{
		super(new LoadFileResource(description), new LoadFileResource(usage), isHidden, ToolCategory.INTERNAL);
	}

	public BaseLoginTool()
	{
		super(null, null, false);
	}

	@Option({ "storetype" })
	public void setStoretype(String storeType)
	{
		_storeType = storeType;
	}

	/*
	 * flag that indicates that no password will be given, to support using only the TLS identity as credential.
	 */
	boolean _bogusPassword = false;

	@Option({ "noPassword" })
	public void setNoPassword()
	{
		_bogusPassword = true;
	}

	@Option({ "create-nonce" })
	public void setcreate_nonce()
	{
		_create_nonce = true;
	}
	
	@Option({ "pop-nonce" })
	public void setpop_nonce()
	{
		_pop_nonce = true;
	}
	
	@Option({ "push-nonce" })
	public void setpushnonce(String nonce)
	{
		_nonce = nonce;
	}


	@Option({ "password" })
	public void setPassword(String password)
	{
		_password = password;
	}

	@Option({ "username" })
	public void setUsername(String username)
	{
		_username = username;
	}

	@Option({ "alias" })
	public void setAlias()
	{
		_aliasPatternFlag = true;
	}

	@Option({ "toolIdentity" })
	public void setToolIdentity()
	{
		_replaceClientToolIdentityFlag = true;
	}

	@Option({ "pattern" })
	public void setPattern(String pattern)
	{
		_pattern = pattern;
	}

	@Option({ "validDuration" })
	public void setValidDuration(String durationString) throws ParseException
	{
		_durationString = durationString;
	}

	@Option({ "validMillis" })
	public void setValidMillis(long validMillis) throws ParseException
	{
		_credentialValidMillis = validMillis;
	}

	// Copies t1's credentials to t2
	public static final void copyCreds(BaseLoginTool t1, BaseLoginTool t2)
	{
		t2.setStoretype(t1._storeType);
		t2.setPassword(t1._password);
		t2.setUsername(t1._username);
		t2._aliasPatternFlag = t1._aliasPatternFlag;
		t2._replaceClientToolIdentityFlag = t1._replaceClientToolIdentityFlag;
		t2._durationString = t1._durationString;
		t2._authnUri = t1._authnUri;
		t2._pattern = t1._pattern;
		t2._arguments = t1._arguments;
	}

	protected final void aquireUsername() throws DialogException, UserCancelException
	{
		DialogProvider provider = DialogFactory.getProvider(stdout, stderr, stdin, useGui());

		if (_username == null) {
			InputDialog usernameDialog = provider.createInputDialog("Username", "Please enter username.");
			usernameDialog.showDialog();
			_username = usernameDialog.getAnswer();
		}
	}

	protected final void aquirePassword() throws ToolException
	{

		if (_password == null) {
			AbstractLoginHandler handler = null;
			if (!useGui() || !GuiUtils.supportsGraphics() || !UserPreferences.preferences().preferGUI()) {
				handler = new TextLoginHandler(stdout, stderr, stdin);
			} else {
				handler = new GuiLoginHandler(stdout, stderr, stdin);
			}
			char[] pword = handler.getPassword("Username/Password Login", String.format("Password for %s:  ", _username));
			if (pword == null)
				throw new ToolException("Unable to aquire password");
			_password = new String(pword);
		}

	}

}
