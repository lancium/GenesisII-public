package edu.virginia.vcgr.genii.container.kerbauthn;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Password callback handler for resolving password/usernames for a JAAS login.
 */
public class LoginCallbackHandler implements CallbackHandler
{

	private String password;
	private String username;

	public LoginCallbackHandler(String name, String password)
	{
		super();
		this.username = name;
		this.password = password;
	}

	/**
	 * Handles the callbacks, and sets the user/password detail.
	 * 
	 * @param callbacks
	 *            the callbacks to handle
	 * @throws IOException
	 *             if an input or output error occurs.
	 */
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
	{

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback && username != null) {
				NameCallback nc = (NameCallback) callbacks[i];
				nc.setName(username);
			} else if (callbacks[i] instanceof PasswordCallback) {
				PasswordCallback pc = (PasswordCallback) callbacks[i];
				pc.setPassword(password.toCharArray());
			}
		}
	}
}
