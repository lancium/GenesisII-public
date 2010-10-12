package edu.virginia.vcgr.genii.ui.trash;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.utils.gui.ButtonPanel;
import org.morgan.utils.gui.GUIUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.UIFrame;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.persist.PersistenceKey;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class TrashDialog extends UIFrame
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(TrashDialog.class);
	
	private JList _unsortedList;
	private JList _unlinkList = new JList(new DefaultListModel());
	private JList _removeList = new JList(new DefaultListModel());
	
	private UndeleteAction _undeleteAction = new UndeleteAction();
	private EnactAction _enactAction = new EnactAction();
	private CancelAction _cancelAction = new CancelAction();
	
	private JPopupMenu _popup = new JPopupMenu("Undelete Menu");
	
	private TrashCanWidget _widget;
	
	static private void setupDragAndDrop(JList list)
	{
		list.setDragEnabled(true);
		list.setTransferHandler(new TrashTransferHandler());
		list.setDropMode(DropMode.INSERT);
	}
	
	private TrashDialog(TrashCanWidget widget, 
		ApplicationContext appContext, UIContext uiContext)
	{
		super(uiContext, "Trash Bin");
		_widget = widget;
		
		_popup.add(_undeleteAction);
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		DefaultListModel model = new DefaultListModel();
		for (Pair<String, PersistenceKey> pair : 
			uiContext.trashCan().contents())
			model.addElement(new TrashCanEntryWrapper(pair));
		
		_unsortedList = new JList(model);
		_unsortedList.setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		_unsortedList.addMouseListener(new MouseClickListenerImpl());
		_unsortedList.addListSelectionListener(_undeleteAction);
		
		setupDragAndDrop(_unsortedList);
		setupDragAndDrop(_unlinkList);
		setupDragAndDrop(_removeList);
		
		add(GUIUtils.addTitle("Unsorted", new JScrollPane(_unsortedList)),
			new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		add(GUIUtils.addTitle("To Unlink", new JScrollPane(_unlinkList)),
			new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		add(GUIUtils.addTitle("To Delete", new JScrollPane(_removeList)),
			new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		
		add(ButtonPanel.createHorizontalButtonPanel(
			_enactAction, _cancelAction),
			new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private class EnactAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public EnactAction()
		{
			super("Enact");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
			_uiContext.progressMonitorFactory().monitor(_widget,
				"Emptying Trash", "Emptying trash", 1000L,
				new TrashCanEnactorTask(),
				new TrashCanEnactorCompleter());
		}
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public CancelAction()
		{
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}
	
	private class TrashCanEnactorTask extends AbstractTask<TrashCanEnactorResults>
	{
		@Override
		public TrashCanEnactorResults execute(
			TaskProgressListener progressListener) throws Exception
		{
			Collection<Pair<String, PersistenceKey>> failedKeys =
				new LinkedList<Pair<String,PersistenceKey>>();
			
			progressListener.updateSubTitle("Emptying \"Unlink\" items.");
			for (int lcv = 0; lcv < _unlinkList.getModel().getSize(); lcv++)
			{
				Object item = _unlinkList.getModel().getElementAt(lcv);
				TrashCanEntryWrapper wrapper = (TrashCanEntryWrapper)item;
				_uiContext.trashCan().remove(wrapper.pair().second());
			}
			
			for (int lcv = 0; lcv < _removeList.getModel().getSize(); lcv++)
			{
				if (wasCancelled())
					break;
				
				Object item = _removeList.getModel().getElementAt(lcv);
				TrashCanEntryWrapper wrapper = (TrashCanEntryWrapper)item;
				
				progressListener.updateSubTitle(String.format(
					"Deleting \"%s\".", wrapper.pair().first()));
				
				try
				{
					TrashCanEntry tce = new TrashCanEntry(
						wrapper.pair().second());
					GeniiCommon common = ClientUtils.createProxy(
						GeniiCommon.class, tce.path().getEndpoint(), 
						tce.callingContext());
					common.destroy(new Destroy());
					_uiContext.trashCan().remove(wrapper.pair().second());
				}
				catch (Throwable cause)
				{
					failedKeys.add(wrapper.pair());
					_logger.warn("Unable to remove trash can item.",
						cause);
				}
			}
			
			return new TrashCanEnactorResults(failedKeys);
		}
	}
	
	private class TrashCanEnactorCompleter 
		implements TaskCompletionListener<TrashCanEnactorResults>
	{
		@Override
		public void taskCancelled(Task<TrashCanEnactorResults> task)
		{
			// Ignore
		}

		@Override
		public void taskCompleted(Task<TrashCanEnactorResults> task,
			TrashCanEnactorResults result)
		{
			if (result.unsuccessfulResults().size() > 0)
				JOptionPane.showMessageDialog(_widget, 
					"Unable to remove all trash can items.", 
					"Trash Can Empty Failed", JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public void taskExcepted(Task<TrashCanEnactorResults> task,
				Throwable cause)
		{
			ErrorHandler.handleError(_uiContext, _widget, cause);	
		}
	}
	
	private class UndeleteAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private UndeleteAction()
		{
			super("Undelete");
			
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Collection<TrashCanEntryWrapper> wrappers;
			
			JList source = _unsortedList;
			Object []selected = source.getSelectedValues();
			if (selected == null || selected.length == 0)
				return;
			
			wrappers = new Vector<TrashCanEntryWrapper>(selected.length);
			for (Object item : selected)
			{
				TrashCanEntryWrapper wrapper = (TrashCanEntryWrapper)item;
				wrappers.add(wrapper);
			}
			
			_uiContext.progressMonitorFactory().monitor(_unsortedList,
				"Undelete", "Undeleting entries.", 1000L, 
				new UndeleteTask(wrappers), new UndeleteTaskCompleter());
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			JList source = (JList)e.getSource();
			Object []selected = source.getSelectedValues();
			if (selected == null || selected.length == 0)
				setEnabled(false);
			else
				setEnabled(true);
		}
	}
	
	private class MouseClickListenerImpl extends MouseAdapter
	{
		static final long serialVersionUID = 0L;

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger())
				_popup.show(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
				_popup.show(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				_popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	private class UndeleteTask
		extends AbstractTask<Collection<TrashCanEntryWrapper>>
	{
		private Collection<TrashCanEntryWrapper> _toUndelete;
		
		private UndeleteTask(Collection<TrashCanEntryWrapper> toUndelete)
		{
			_toUndelete = toUndelete;
		}
		
		@Override
		public Collection<TrashCanEntryWrapper> execute(
				TaskProgressListener progressListener) throws Exception
		{
			Collection<TrashCanEntryWrapper> successful =
				new LinkedList<TrashCanEntryWrapper>();
			
			for (TrashCanEntryWrapper wrapper : _toUndelete)
			{
				if (wasCancelled())
					break;
				
				TrashCanEntry entry = new TrashCanEntry(wrapper.pair().second());
				progressListener.updateSubTitle(String.format(
					"Un-deleting \"%s\".", wrapper));
				IContextResolver resolver = ContextManager.getResolver();
				
				try
				{
					ContextManager.setResolver(
						new MemoryBasedContextResolver(entry.callingContext()));
					RNSPath parent = entry.path().getParent();
					RNSPath newTarget = parent.lookup(entry.path().getName(),
						RNSPathQueryFlags.MUST_NOT_EXIST);
					newTarget.link(entry.path().getEndpoint());
					successful.add(wrapper);

					_uiContext.trashCan().remove(wrapper.pair().second());
				}
				catch (RNSPathAlreadyExistsException rpaee)
				{
					JOptionPane.showMessageDialog(
						_unsortedList, 
						String.format("Path \"%s\" already exists!", wrapper), 
						"Unable to Undelete", JOptionPane.ERROR_MESSAGE);
				}
				catch (Throwable cause)
				{
					ErrorHandler.handleError(_uiContext, _unsortedList, cause);
				}
				finally
				{
					ContextManager.setResolver(resolver);
				}
			}
			
			return successful;
		}	
	}
	
	private class UndeleteTaskCompleter
		implements TaskCompletionListener<Collection<TrashCanEntryWrapper>>
	{
		@Override
		public void taskCancelled(Task<Collection<TrashCanEntryWrapper>> task)
		{
			// Do nothing
		}

		@Override
		public void taskCompleted(Task<Collection<TrashCanEntryWrapper>> task,
				Collection<TrashCanEntryWrapper> result)
		{
			for (TrashCanEntryWrapper wrapper : result)
			{
				((DefaultListModel)_unsortedList.getModel()).removeElement(
					wrapper);
				String s = wrapper.toString();
				int i = s.lastIndexOf('/');
				if (i >= 0)
				{
					s = s.substring(0, i);
					if (s.length() == 0)
						s = "/";
					_uiContext.directoryChangeNexus().fireDirectoryChanged(s);
				}
			}
		}

		@Override
		public void taskExcepted(Task<Collection<TrashCanEntryWrapper>> task,
				Throwable cause)
		{
			// Do nothing
		}
	}
	
	static public void popupTrashDialog(TrashCanWidget widget,
		ApplicationContext appContext, UIContext uiContext)
	{
		TrashDialog dialog = new TrashDialog(widget, appContext, uiContext);
		dialog.pack();
		GUIUtils.centerWindow(dialog);
		dialog.setVisible(true);
	}
}