package edu.virginia.vcgr.genii.ui.login;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;

import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.ui.rns.RNSIcons;

public abstract class CredentialManagementAction extends AbstractAction implements CredentialListener
{
	static final long serialVersionUID = 0L;

	protected CredentialManagementContext _context;

	protected void resetTooltip()
	{
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		pw.println("<HTML><BODY>");
		Collection<NuCredential> creds = _context.loginItems();
		if (creds.isEmpty())
			pw.println("<B>Not logged in</B>");
		else {
			pw.println("<B>Logged in as:</B>");
			pw.println("<UL>");
			for (NuCredential cred : creds) {
				pw.format("<LI>%s</LI>\n", cred.describe(VerbosityLevel.LOW));
			}
			pw.println("</UL>");
		}
		pw.println("</BODY></HTML>");
		pw.close();

		putValue(Action.SHORT_DESCRIPTION, writer.toString());
	}

	protected CredentialManagementAction(CredentialManagementContext context)
	{
		super("Credential Management", RNSIcons.getSecurityIcon());

		_context = context;
		resetTooltip();

		_context.addCredentialListener(this);
	}

	@Override
	final public void credentialsChanged(CredentialManagementContext context)
	{
		resetTooltip();
	}
}