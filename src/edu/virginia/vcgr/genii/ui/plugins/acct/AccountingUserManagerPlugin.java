package edu.virginia.vcgr.genii.ui.plugins.acct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Properties;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class AccountingUserManagerPlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		String password = PasswordDialog.getPassword(context.ownerComponent());
		if (password == null)
			return;

		Connection connection = null;
		PreparedStatement stmt = null;
		String url;

		try {
			url = context.endpointRetriever().getTargetEndpoints().iterator().next().getEndpoint().getAddress().get_value()
				.toString();

			while (true) {
				Properties info = new Properties();
				info.setProperty("password", password);

				try {
					connection = DriverManager.getConnection(url, info);
					connection.setAutoCommit(false);
					break;
				} catch (SQLException sqe) {
					ErrorHandler.handleError(context.uiContext(), context.ownerComponent(), sqe);
				}
			}

			Collection<CredentialBundle> dirtyBundles = CredentialDialog
				.manageCredentials(context.ownerComponent(), connection);
			if (dirtyBundles != null && dirtyBundles.size() > 0) {
				stmt = connection.prepareStatement("UPDATE xcgcredentials SET credentialtype = ? " + "WHERE cid = ?");

				for (CredentialBundle bundle : dirtyBundles) {
					if (bundle.credentialType() != null)
						stmt.setString(1, bundle.credentialType().name());
					else
						stmt.setNull(1, Types.VARCHAR);

					stmt.setLong(2, bundle.cid());

					stmt.addBatch();
				}

				stmt.executeBatch();
				connection.commit();
			}
		} catch (Throwable e) {
			ErrorHandler.handleError(context.uiContext(), context.ownerComponent(), e);
		} finally {
			StreamUtils.close(stmt);
			StreamUtils.close(connection);
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		return selectedDescriptions.iterator().next().typeInformation().isJDBCURL();
	}
}