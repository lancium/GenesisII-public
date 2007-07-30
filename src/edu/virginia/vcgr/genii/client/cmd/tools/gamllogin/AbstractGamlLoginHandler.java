package edu.virginia.vcgr.genii.client.cmd.tools.gamllogin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

import edu.virginia.vcgr.genii.client.security.wincrypto.WinX509KeyManager;
import edu.virginia.vcgr.genii.client.cmd.tools.GamlLoginTool;

public abstract class AbstractGamlLoginHandler implements CallbackHandler
{
	static private final int _PASSWORD_TRIES = 5;
	
	protected PrintStream _out;
	protected PrintStream _err;
	protected BufferedReader _in;
	
	private char []_password = null;
	
	protected AbstractGamlLoginHandler(PrintStream out, PrintStream err,
		BufferedReader in)
	{
		_out = out;
		_err = err;
		_in = in;
	}
	
	private void addEntriesFromFile(
		Collection<GamlLoginTool.CertEntry> entries,
		String storePath, String storeType, String password)
			throws GeneralSecurityException, IOException
	{
		KeyStore specifiedKs = null;
		
		if (storePath != null)
		{
			if (storeType == null)
			{
				// try PKCS12
				storeType = "PKCS12";
			}
			
			if (password != null)
				_password = password.toCharArray();
			
			KeyStore.Builder builder = KeyStore.Builder.newInstance(
				storeType, null, new File(storePath),
				new KeyStore.CallbackHandlerProtection(this));
			specifiedKs = builder.getKeyStore();
		}
		
		if (specifiedKs != null)
		{
			Enumeration<String> aliases = specifiedKs.aliases();
			while (aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				Certificate []aliasCertChain = 
					specifiedKs.getCertificateChain(alias);
				if (aliasCertChain == null)
					continue;
				
				entries.add(new GamlLoginTool.CertEntry(
					aliasCertChain, alias, specifiedKs));
			}
		}
	}
	
	private void addEntriesFromWindows(
		Collection<GamlLoginTool.CertEntry> entries)
	{
		if (System.getProperty("os.name").contains("Windows"))
		{
			WinX509KeyManager km = new WinX509KeyManager();
			String []aliases = km.getClientAliases(null, null);
			for (String alias : aliases)
			{
				PrivateKey privateKey = km.getPrivateKey(alias);
				String friendlyName = km.getFriendlyName(alias);
				if (privateKey != null)
				{
					X509Certificate []aliasCertChain = 
						km.getCertificateChain(alias);
					if (aliasCertChain != null)
					{
						entries.add(new GamlLoginTool.CertEntry(aliasCertChain, 
							alias, privateKey, friendlyName));
					}
				}
			}
		}
	}
	
	protected Collection<GamlLoginTool.CertEntry> retrieveCertEntries(
		String storePath, String storeType, String password)
		throws GeneralSecurityException, IOException
	{
		ArrayList<GamlLoginTool.CertEntry> list = 
			new ArrayList<GamlLoginTool.CertEntry>();
		
		addEntriesFromFile(list, storePath, storeType, password);
		addEntriesFromWindows(list);
		
		return list;
	}
	
	public GamlLoginTool.CertEntry selectCert(
		String storePath, String storeType, String password,
		String entryPattern)
		throws GeneralSecurityException, IOException
	{
		Collection<GamlLoginTool.CertEntry> entries =
			retrieveCertEntries(storePath, storeType, password);
		GamlLoginTool.CertEntry entry = null;
		
		if (entryPattern != null)
		{
			Pattern p = Pattern.compile(
				"^.*" + Pattern.quote(entryPattern) + ".*$");
			
			int numMatched = 0;
			GamlLoginTool.CertEntry selectedEntry = null;
			for (GamlLoginTool.CertEntry iter : entries)
			{
				String name = 
					((X509Certificate)(iter._certChain[0])).getSubjectDN().getName();
				Matcher matcher = p.matcher(name);
				if (matcher.matches())
				{
					selectedEntry = iter;
					numMatched++;
				}
			}
			
			if (numMatched == 0)
			{
				throw new IOException("No certificates matched the pattern \"" 
					+ entryPattern + "\".");
			} else if (numMatched > 1)
			{
				throw new IOException("Multiple certificates matched the pattern \""
					+ entryPattern + "\".");
			} else
			{
				entry = selectedEntry;
			}
		} else
		{		
			entry = selectCert(entries);
		}
		
		if (entry == null)
			return null;
		
		if (entry._privateKey == null)
		{
			char []passwordChars = null;
			
			for (int tryNumber = 0; tryNumber < _PASSWORD_TRIES; tryNumber++)
			{
				try
				{
					entry._privateKey =
						(PrivateKey)entry._keyStore.getKey(entry._alias, 
							passwordChars);
					break;
				}
				catch (UnrecoverableKeyException uke)
				{
					if (_password != null)
					{
						passwordChars = _password;
						_password = null;
					} else
					{
						passwordChars = getPassword("Key Password",
							"Enter key password for \"" +
							entry._certChain[0].getSubjectDN().getName() +
							"\".");
					}
				}
			}
		}
		
		_password = null;
		return entry;
	}
	
	private void handle(PasswordCallback callback)
	{
		if (_password != null)
			callback.setPassword(_password);
		else
			callback.setPassword(_password = getPassword(
					"Password Entry", callback.getPrompt() + ": "));
	}
	
	public void handle(Callback []callbacks)
	{
		for (Callback cb : callbacks)
		{
			if (cb instanceof PasswordCallback)
			{
				handle((PasswordCallback)cb);
			} else
			{
				_err.println("Can't handle security callback of type \"" +
					cb.getClass().getName() + "\".");
			}
		}
	}
	
	protected abstract char[] getPassword(String title, String prompt);
	protected abstract GamlLoginTool.CertEntry selectCert(
		Collection<GamlLoginTool.CertEntry> entries);
}