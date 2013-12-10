package edu.virginia.vcgr.genii.ui.plugins.logs.panels.meta;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.logs.panels.LogManagerPanel;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogPath;

public class LogManagerMetaPanel extends LogManagerPanel implements LazyLoadTabHandler
{
	static final long serialVersionUID = 0L;

	static final private Dimension TABLE_SIZE = new Dimension(700, 600);

	private class PopupListener extends MouseAdapter
	{
		@Override
		final public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}

		@Override
		final public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}

		@Override
		final public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e);
		}
	}

	private class RefreshAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private RefreshAction()
		{
			super("Refresh");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_model.refresh(_table);
		}
	}

	private UIPluginContext _context;
	private JTable _table;
	private LogManagerMetaTableModel _model;

	private void popup(MouseEvent e)
	{
		JPopupMenu popup = new JPopupMenu("Log Manager Popup");
		popup.add(new RefreshAction());

		popup.show(_table, e.getX(), e.getY());
	}

	public LogManagerMetaPanel(UIPluginContext context) throws RemoteException, RNSPathDoesNotExistException
	{
		super(new GridBagLayout());

		setName("Job Management");

		_context = context;

		_model = new LogManagerMetaTableModel(_context);
		_table = new JTable(_model);
		_model.prepareTableColumns(_table.getColumnModel());
		_table.setAutoCreateRowSorter(true);
		_table.addMouseListener(new PopupListener());

		_table.getActionMap().put("Refresh", new RefreshAction());
		_table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0x0), "Refresh");

		JScrollPane pane = new JScrollPane(_table);

		add(pane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
			5, 5, 5, 5), 5, 5));
		add(new JButton(new RefreshAction()), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		setPreferredSize(TABLE_SIZE);
	}

	@Override
	final public void load()
	{
		_model.refresh(_table);
	}

	@Override
	public void updateStatus(Collection<LogPath> descriptions)
	{
		_model.refresh(_table);
	}
}