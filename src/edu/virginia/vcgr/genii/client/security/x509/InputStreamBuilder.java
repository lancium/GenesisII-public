package edu.virginia.vcgr.genii.client.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.Builder;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

public class InputStreamBuilder extends Builder
{

	// maximum times to try the callbackhandler if the password is wrong
	static final int MAX_CALLBACK_TRIES = 3;

	private final String type;
	private final Provider provider;
	private final InputStream input;
	private ProtectionParameter protection;
	private ProtectionParameter keyProtection;
	private final AccessControlContext context;

	private KeyStore keyStore;

	private Throwable oldException;

	public InputStreamBuilder(String type, Provider provider,
			InputStream input, ProtectionParameter protection,
			AccessControlContext context)
	{
		this.type = type;
		this.provider = provider;
		this.input = input;
		this.protection = protection;
		this.context = context;
	}

	public synchronized KeyStore getKeyStore() throws KeyStoreException
	{
		if (keyStore != null)
		{
			return keyStore;
		}
		if (oldException != null)
		{
			throw new KeyStoreException(
					"Previous KeyStore instantiation failed", oldException);
		}
		PrivilegedExceptionAction<KeyStore> action =
				new PrivilegedExceptionAction<KeyStore>()
				{
					public KeyStore run() throws Exception
					{
						if (protection instanceof CallbackHandlerProtection == false)
						{
							return run0();
						}
						// when using a CallbackHandler,
						// reprompt if the password is wrong
						int tries = 0;
						while (true)
						{
							tries++;
							try
							{
								return run0();
							}
							catch (IOException e)
							{
								if ((tries < MAX_CALLBACK_TRIES)
										&& (e.getCause() instanceof UnrecoverableKeyException))
								{
									continue;
								}
								throw e;
							}
						}
					}

					public KeyStore run0() throws Exception
					{
						KeyStore ks;
						if (provider == null)
						{
							ks = KeyStore.getInstance(type);
						}
						else
						{
							ks = KeyStore.getInstance(type, provider);
						}
						char[] password = null;
						try
						{
							if (protection instanceof PasswordProtection)
							{
								password =
										((PasswordProtection) protection)
												.getPassword();
								keyProtection = protection;
							}
							else
							{
								CallbackHandler handler =
										((CallbackHandlerProtection) protection)
												.getCallbackHandler();
								PasswordCallback callback =
										new PasswordCallback(
												"Password for keystore", false);
								handler.handle(new Callback[] { callback });
								password = callback.getPassword();
								if (password == null)
								{
									throw new KeyStoreException("No password"
											+ " provided");
								}
								callback.clearPassword();
								keyProtection =
										new PasswordProtection(password);
							}
							ks.load(input, password);
							return ks;
						}
						finally
						{
							if (input != null)
							{
								input.close();
							}
						}
					}
				};
		try
		{
			keyStore =
					(KeyStore) AccessController.doPrivileged(action, context);
			return keyStore;
		}
		catch (PrivilegedActionException e)
		{
			oldException = e.getCause();
			throw new KeyStoreException("KeyStore instantiation failed",
					oldException);
		}
	}

	public synchronized ProtectionParameter getProtectionParameter(String alias)
	{
		if (alias == null)
		{
			throw new NullPointerException();
		}
		if (keyStore == null)
		{
			throw new IllegalStateException(
					"getKeyStore() must be called first");
		}
		return keyProtection;
	}
}
