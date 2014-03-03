package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreationContext;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreator;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class MakeDirectoryPlugin extends AbstractCombinedUIMenusPlugin {

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException {
		Closeable contextToken = null;

		while (true) {
			contextToken = null;

			try {
				contextToken = ContextManager.temporarilyAssumeContext(context
						.uiContext().callingContext());
				Collection<RNSPath> paths = context.endpointRetriever()
						.getTargetEndpoints();
				RNSPath path = paths.iterator().next();
				RNSPath linkPath = path;
				String answer = null;
				while (answer == null) {
					// Keep trying until they get a good path or they give up
					answer = JOptionPane
							.showInputDialog(
									context.ownerComponent(),
									"New Directory Path <pwd="
											+ path.toString() + ">?");
					if (answer == null)
						return;
					// Now check path
					try {
						path = path.lookup(answer,
								RNSPathQueryFlags.MUST_NOT_EXIST);
						linkPath = path;
					} catch (RNSPathAlreadyExistsException r) {
						JOptionPane.showMessageDialog(null, answer,
								"Indicated path already exists",
								JOptionPane.ERROR_MESSAGE);
						answer = null;
					}
					if (!path.getParent().exists()) {
						JOptionPane.showMessageDialog(null, answer,
								"Parent directory "
										+ path.getParent().toString()
										+ " does not exist!",
								JOptionPane.ERROR_MESSAGE);
						answer = null;
					}
				}

				// Now see if they want on the same storage service as the
				// parent directory, or
				// somewhere else
				int sameStorage = JOptionPane
						.showConfirmDialog(
								null,
								"Store directory on same storage server as parent directory "
										+ path.getParent().toString(),
								"Decide where to store the directory and things created in it",
								JOptionPane.YES_NO_OPTION);
				// System.err.println("samestorage = " + sameStorage);
				if (sameStorage == 0) {
					// They said yes - go figure 0 is yes
					path.mkdir();
				} else {
					String containerPath = null;
					// Much more complicated. First must get the container they
					// want to use.

					boolean done = false;
					while (!done) {
						// Keep trying till they get a good container path or
						// they give up
						containerPath = JOptionPane
								.showInputDialog(context.ownerComponent(),
										"Input storage container path: ",
										containerPath);
						if (containerPath == null)
							return;
						done = true; // assume this works until proven otherwise
						try {
							path = path.lookup(containerPath,
									RNSPathQueryFlags.MUST_EXIST);
							path = path.lookup(containerPath
									+ "/Services/EnhancedRNSPortType",
									RNSPathQueryFlags.MUST_EXIST);
						} catch (RNSPathDoesNotExistException r) {
							JOptionPane.showMessageDialog(null, containerPath,
									"Storage container path does not exist",
									JOptionPane.ERROR_MESSAGE);
							// lets keep what they already typed and let them
							// edit it instead
							// containerPath = null;
							done = false;
						}

						// if (containerPath != null)
						// break;
					}
					if (new TypeInformation(path.lookup(containerPath)
							.getEndpoint()).isContainer()) {
						// System.err.println("About to mkdir: container " +
						// path.toString() +
						// " : link to " + linkPath.toString());
						EndpointReferenceType newEPR = ResourceCreator
								.createNewResource(path.getEndpoint(), null,
										new ResourceCreationContext());
						linkPath.link(newEPR);

					} else {
						JOptionPane.showMessageDialog(null, containerPath,
								"Storage container is not a container",
								JOptionPane.ERROR_MESSAGE);
						containerPath = null;
					}

					path = path.lookup(containerPath,
							RNSPathQueryFlags.MUST_EXIST);
					// Then create the directory on that container, linked to
					// the path we want
				}

				context.endpointRetriever().refresh();
				return;
			} catch (Throwable cause) {
				ErrorHandler.handleError(context.uiContext(),
						context.ownerComponent(), cause);
			} finally {
				StreamUtils.close(contextToken);
			}
		}
	}

	@Override
	public boolean isEnabled(
			Collection<EndpointDescription> selectedDescriptions) {
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		// ASG: 9-13-2013. Modified to be more selective. Not just is it an RNS,
		// but is it an RNS
		// and NOT (isContainer, isBES ...
		// Perhaps should be even more selective,
		TypeInformation tp = selectedDescriptions.iterator().next()
				.typeInformation();
		return (tp.isRNS() && !(tp.isContainer() || tp.isBESContainer()
				|| tp.isQueue() || tp.isIDP()));
		// return
		// selectedDescriptions.iterator().next().typeInformation().isRNS();
	}
}