package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.gui.widgets.rns.RNSTree;
import edu.virginia.vcgr.genii.client.gui.widgets.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.client.rns.RNSException;

public class RNSBrowserDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	static final private String _TITLE = "RNS Target";

	private RNSTree _rnsTree;
	private JTextField _rnsPath;
	private String _selectedPath = null;

	public RNSBrowserDialog(JDialog owner) throws RNSException
	{
		super(owner);

		Container container;

		setTitle(_TITLE);
		container = getContentPane();

		container.setLayout(new GridBagLayout());

		container.add(new JScrollPane(_rnsTree = new RNSTree()), new GridBagConstraints(0, 0, 4, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JLabel("RNS Path"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(_rnsPath = new JTextField(), new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		SaveAsAction saveAs = new SaveAsAction();

		container.add(new JPanel(), new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JButton(saveAs), new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JButton(new CancelAction()), new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		_rnsTree.addTreeSelectionListener(new SelectionListener());
		_rnsPath.addCaretListener(saveAs);

		Dimension d = new Dimension(500, 500);
		setMinimumSize(d);
		setPreferredSize(d);
	}

	public String getSelectedPath()
	{
		return _selectedPath;
	}

	private class SelectionListener implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			TreePath path = e.getPath();
			if (path == null)
				return;

			RNSTreeNode node = (RNSTreeNode) path.getLastPathComponent();
			_rnsPath.setText(node.getRNSPath().pwd());
		}
	}

	private class SaveAsAction extends AbstractAction implements CaretListener
	{
		static final long serialVersionUID = 0L;

		public SaveAsAction()
		{
			super("Save As");

			String text = _rnsPath.getText();
			setEnabled(text != null && text.trim().length() > 0);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_selectedPath = _rnsPath.getText();
			if (_selectedPath != null && _selectedPath.trim().length() == 0)
				_selectedPath = null;

			setVisible(false);
		}

		@Override
		public void caretUpdate(CaretEvent e)
		{
			String text = _rnsPath.getText();
			setEnabled(text != null && text.trim().length() > 0);
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
			setVisible(false);
		}
	}
}