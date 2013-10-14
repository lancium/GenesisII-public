package edu.virginia.vcgr.genii.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.security.wincrypto.WinX509KeyManager;

public class SimpleKeystoreLoader implements CallbackHandler
{
	static public final String PKCS12 = "PKCS12";
	static public final String JKS = "JKS";
	static public final String WINDOWS = "WIN";

	static private Log _logger = LogFactory.getLog(SimpleKeystoreLoader.class);

	private char[] _passwordChars = null;

	public SimpleKeystoreLoader()
	{
	}

	private void addEntriesFromFile(Collection<CertEntry> entries, InputStream storeInput, String storeType)
		throws GeneralSecurityException, IOException
	{
		KeyStore specifiedKs = null;

		if (storeType == null) {
			// try PKCS12
			storeType = PKCS12;
		}

		KeyStore.Builder builder =
			new InputStreamBuilder(storeType, null, storeInput, new KeyStore.CallbackHandlerProtection(this),
				AccessController.getContext());
		specifiedKs = builder.getKeyStore();

		if (specifiedKs != null) {
			Enumeration<String> aliases = specifiedKs.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				Certificate[] aliasCertChain = specifiedKs.getCertificateChain(alias);
				if (aliasCertChain == null)
					continue;

				entries.add(new CertEntry(aliasCertChain, alias, specifiedKs));
			}
		}
	}

	private void addEntriesFromWindows(Collection<CertEntry> entries)
	{
		if (OperatingSystemType.getCurrent().isWindows()) {
			WinX509KeyManager km = new WinX509KeyManager();
			String[] aliases = km.getClientAliases(null, null);
			for (String alias : aliases) {
				PrivateKey privateKey = km.getPrivateKey(alias);
				String friendlyName = km.getFriendlyName(alias);
				if (privateKey != null) {
					X509Certificate[] aliasCertChain = km.getCertificateChain(alias);
					if (aliasCertChain != null) {
						entries.add(new CertEntry(aliasCertChain, alias, privateKey, friendlyName));
					}
				}
			}
		}
	}

	protected Collection<CertEntry> retrieveCertEntries(InputStream storeInput, String storeType)
		throws GeneralSecurityException, IOException
	{
		ArrayList<CertEntry> list = new ArrayList<CertEntry>();

		if ((storeType != null) && storeType.equals(WINDOWS)) {
			addEntriesFromWindows(list);
		} else {
			addEntriesFromFile(list, storeInput, storeType);
		}

		return list;
	}

	public CertEntry selectCert(InputStream storeInput, String storeType, String password, boolean isAliasPattern,
		String entryPattern) throws GeneralSecurityException, IOException
	{
		_passwordChars = password.toCharArray();
		Collection<CertEntry> entries = retrieveCertEntries(storeInput, storeType);
		CertEntry entry = null;

		if (entryPattern != null) {
			int flags = 0;
			if (isAliasPattern)
				flags = Pattern.CASE_INSENSITIVE;
			Pattern p = Pattern.compile("^.*" + Pattern.quote(entryPattern) + ".*$", flags);

			int numMatched = 0;
			CertEntry selectedEntry = null;
			for (CertEntry iter : entries) {
				String toMatch = null;
				if (!isAliasPattern)
					toMatch = ((X509Certificate) (iter._certChain[0])).getSubjectDN().getName();
				else
					toMatch = iter._alias;

				Matcher matcher = p.matcher(toMatch);
				if (matcher.matches()) {
					selectedEntry = iter;
					numMatched++;
				}
			}

			if (numMatched == 0) {
				throw new IOException("No certificates matched the pattern \"" + entryPattern + "\".");
			} else if (numMatched > 1) {
				throw new IOException("Multiple certificates matched the pattern \"" + entryPattern + "\".");
			} else {
				entry = selectedEntry;
			}
		} else {
			if (entries.size() > 1)
				_logger.debug("selecting first certificate of multiple found");
			entry = entries.iterator().next();
		}

		if (entry == null)
			return null;

		if (entry._privateKey == null) {
			try {
				entry._privateKey = (PrivateKey) entry._keyStore.getKey(entry._alias, _passwordChars);
			} catch (UnrecoverableKeyException uke) {
				_logger.error("failed to find key using password", uke);
				return null;
			}
		}

		return entry;
	}

	private void handle(PasswordCallback callback)
	{
		if (_passwordChars != null)
			callback.setPassword(_passwordChars);
	}

	@Override
	public void handle(Callback[] callbacks)
	{
		for (Callback cb : callbacks) {
			if (cb instanceof PasswordCallback) {
				handle((PasswordCallback) cb);
			} else {
				_logger.error("Can't handle security callback of type \"" + cb.getClass().getName() + "\".");
			}
		}

	}
}
