package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.client.security.authz.acl.X509PatternAclEntry;
import edu.virginia.vcgr.genii.client.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferData;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferable;

public class PatternComponent extends DraggableImageComponent
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(PatternComponent.class);
	
	private UIContext _context;
	private ACLEntryWrapper _wrapper = null;
	
	PatternComponent(UIContext context)
	{
		super(ACLImages.emptyPattern());
	
		_context = context;
		
		setToolTipText(
			"Drag an X.509 certificate onto this " +
			"icon to create a pattern-based ACL.");
		
		setTransferHandler(new PatternComponentTransferHandler());
		
		addMouseListener(new MouseClickListener());
	}
	
	private void clear()
	{
		int answer = JOptionPane.showConfirmDialog(this, "Clear pattern?", 
			"Clear Confirm", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION)
		{
			_wrapper = null;
			setToolTipText(
				"Drag an X.509 certificate onto this " +
				"icon to create a pattern-based ACL.");
			setImage(ACLImages.emptyPattern());
		}
	}
	
	private void setPattern(X509Identity identity, X500Principal principle)
	{
		AclEntry entry;
		
		entry = new X509PatternAclEntry(identity, principle);
		
		_wrapper = new ACLEntryWrapper(_context, entry);
		setToolTipText(String.format("Pattern:  %s", principle));
		setImage(ACLImages.filledPattern());
	}
	
	private void doImport(Pair<RNSTreeNode, RNSPath> remotePath)
	{
		Collection<RNSPath> targets = new Vector<RNSPath>(1);
		targets.add(remotePath.second());
		
		_context.progressMonitorFactory().createMonitor(
			this, "Reading Grid Identity", "Reading grid identity",
			1000L, new RemoteIdentityLookupTask(targets),
			new RemoteLookupCompletionListener()).start();
	}
	
	private void doImport(File file)
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(file);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
			X509Certificate[] chain = { cert };
			doImport(new X509Identity(chain));
		}
		catch (IOException ioe)
		{
			JOptionPane.showMessageDialog(this,
				"Unable to read certificate file.", "Certificate Read Error", 
				JOptionPane.ERROR_MESSAGE);
		} 
		catch (CertificateException e)
		{
			JOptionPane.showMessageDialog(this,
				"Unable to read certificate from file.", "Certificate Read Error", 
				JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	private void doImport(X509Identity identity)
	{
		X500Principal principal = PatternDialog.getPattern(
			_context, identity, this);
		if (principal != null)
			setPattern(identity, principal);
	}
	
	private class RemoteLookupCompletionListener
		implements TaskCompletionListener<X509Identity[]>
	{

		@Override
		public void taskCancelled(Task<X509Identity[]> task)
		{
			// Do nothing
		}

		@Override
		public void taskCompleted(Task<X509Identity[]> task, X509Identity[] result)
		{
			for (X509Identity identity : result)
				doImport(identity);
		}

		@Override
		public void taskExcepted(Task<X509Identity[]> task, Throwable cause)
		{
			ErrorHandler.handleError(_context, PatternComponent.this, cause);
		}	
	}
	
	private class PatternComponentTransferHandler extends TransferHandler
	{
		static final long serialVersionUID = 0L;

		@Override
		protected Transferable createTransferable(JComponent c)
		{
			if (_wrapper == null)
				return null;
			
			return new ACLTransferable(new ACLEntryWrapperTransferData(null,
				_wrapper));
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return LINK;
		}
		
		@Override
		public boolean canImport(TransferSupport support)
		{
			try
			{
				if (!support.isDrop())
					return false;
				
				if (!support.getComponent().isEnabled())
					return false;
				
				if ((support.getSourceDropActions() & COPY) > 0x0)
					support.setDropAction(COPY);
				else if ((support.getSourceDropActions() & LINK) > 0x0)
					support.setDropAction(LINK);
				else
					return false;
				
				if (support.isDataFlavorSupported(ACLTransferable.DATA_FLAVOR))
				{
					ACLEntryWrapperTransferData data =
						(ACLEntryWrapperTransferData)support.getTransferable(
							).getTransferData(ACLTransferable.DATA_FLAVOR);

					Collection<ACLEntryWrapper> wrappers = data.wrappers();
					if (wrappers.size() != 1)
						return false;
					
					AclEntry entry = wrappers.iterator().next().entry();
					if (!(entry instanceof X509Identity))
						return false;
					
					return true;
				} else if (support.isDataFlavorSupported(
					RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				{
					RNSListTransferData data =
						(RNSListTransferData)support.getTransferable(
							).getTransferData(
								RNSListTransferable.RNS_PATH_LIST_FLAVOR);

					if (data.paths().size() != 1)
						return false;
					
					return true;
				} else if (
					support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					return true;
				}
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to check for DnD capability.", cause);
			}
			
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TransferSupport support)
		{
			try
			{
				if ((support.getDropAction() & (LINK | COPY)) == 0x0)
					return false;
				
				if (support.isDataFlavorSupported(ACLTransferable.DATA_FLAVOR))
				{
					ACLEntryWrapperTransferData data =
						(ACLEntryWrapperTransferData)support.getTransferable(
							).getTransferData(ACLTransferable.DATA_FLAVOR);
					
					Collection<ACLEntryWrapper> wrappers = data.wrappers();
					if (wrappers.size() != 1)
						return false;
					
					AclEntry entry = wrappers.iterator().next().entry();
					if (!(entry instanceof X509Identity))
						return false;
					
					// Do the import
					doImport((X509Identity)entry);
					return true;
				} else if (support.isDataFlavorSupported(
					RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				{
					RNSListTransferData data =
						(RNSListTransferData)support.getTransferable(
							).getTransferData(
								RNSListTransferable.RNS_PATH_LIST_FLAVOR);

					if (data.paths().size() != 1)
						return false;
					
					// Do the import
					doImport(data.paths().iterator().next());
					return true;
				} else if (
					support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					List<File> files = (List<File>)support.getTransferable(
						).getTransferData(DataFlavor.javaFileListFlavor);
					if (files == null || files.size() != 1)
						return false;
					
					File file = files.get(0);
					if (!file.isFile())
						return false;
					
					// Do the import
					doImport(file);
					return true;
				}
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to import data.", cause);
			}
			
			return false;
		}
	}
	
	private class MouseClickListener extends MouseAdapter
	{
		static final long serialVersionUID = 0L;

		private void popup(MouseEvent e)
		{
			JPopupMenu menu = new JPopupMenu("Popup Menu");
			menu.add(new AbstractAction("Clear Pattern")
			{
				static final long serialVersionUID = 0L;
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					clear();
				}
			});
			menu.show(PatternComponent.this, e.getX(), e.getY());
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
			else if (e.getClickCount() == 2)
				clear();
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}
	}
}