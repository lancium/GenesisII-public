package edu.virginia.vcgr.genii.client.incommon;

import java.io.PrintWriter;
import java.security.PrivateKey;

public class CILogonParameters {
	public String IDPUrl;
	public String username;
	public String password;

	public PrintWriter stdout;
	public PrintWriter stderr;

	public String csr;
	public PrivateKey key;

	public boolean verbose;
	public boolean silent;

	public int lifetime;

	public CILogonParameters(String url, String user, String pass, String csr,
			PrivateKey key, PrintWriter stdout, PrintWriter stderr) {
		this.IDPUrl = url;
		this.username = user;
		this.password = pass;

		this.stdout = stdout;
		this.stderr = stderr;

		this.key = key;
		this.csr = csr;

		this.verbose = false;
		this.silent = false;

		this.lifetime = 24; // hours
	}

	public CILogonParameters() {
		this.IDPUrl = null;
		this.username = null;
		this.password = null;

		stdout = new PrintWriter(System.out);
		stderr = new PrintWriter(System.err);

		this.key = null;
		this.csr = null;

		this.verbose = false;
		this.silent = false;

		this.lifetime = 0;
	}
}
