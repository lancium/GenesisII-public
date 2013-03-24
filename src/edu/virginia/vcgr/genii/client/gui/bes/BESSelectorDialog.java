package edu.virginia.vcgr.genii.client.gui.bes;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.morgan.util.GraphicsUtils;
import org.ws.addressing.EndpointReferenceType;

@SuppressWarnings("rawtypes")
public class BESSelectorDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	private boolean _cancelled = true;
	private BESListModel _model = new BESListModel();
	@SuppressWarnings("unchecked")
	private JList _besList = new JList(_model);

	private BESSelectorDialog(Window owner)
	{
		super(owner);
		setTitle("BES Selector");

		_besList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		JPanel besPanel = new JPanel(new GridBagLayout());
		besPanel.add(new JScrollPane(_besList), new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		besPanel.add(new JButton(new CreateBESAction()), new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		besPanel.add(new JButton(new RemoveBESAction()), new GridBagConstraints(1, 1, 1, 1, 0.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		besPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Local BES Containers"));

		content.add(new JLabel("Select a BES container to manage."), new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(besPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new OKAction()), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()), new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private class CreateBESAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		public CreateBESAction()
		{
			super("Create New BES");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			String answer;

			while (true) {
				answer = JOptionPane.showInputDialog(BESSelectorDialog.this,
					"Where in the grid would you like to link the new BES container?");
				if (answer == null)
					return;

				String error = BESManager.createBES(answer, _model);
				if (error == null) {
					_besList.clearSelection();
					return;
				}
				JOptionPane.showMessageDialog(BESSelectorDialog.this, error, "Creation Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class RemoveBESAction extends AbstractAction implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;

		public RemoveBESAction()
		{
			super("Remove BES");

			setEnabled(false);
			_besList.addListSelectionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			BESBundle bundle = (BESBundle) _besList.getSelectedValue();
			if (bundle != null)
				BESManager.removeBES(bundle.path(), bundle.epr(), _model);
			_besList.clearSelection();
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_besList.getSelectedIndices().length > 0);
		}
	}

	private class OKAction extends AbstractAction implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;

		public OKAction()
		{
			super("OK");

			setEnabled(false);
			_besList.addListSelectionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_cancelled = false;
			dispose();
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_besList.getSelectedIndices().length > 0);
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
			_cancelled = true;
			dispose();
		}
	}

	static public EndpointReferenceType selectBESContainer(Window owner)
	{
		BESSelectorDialog dialog = new BESSelectorDialog(owner);
		dialog.pack();
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		GraphicsUtils.centerWindow(dialog);
		dialog.setVisible(true);

		if (dialog._cancelled)
			return null;

		BESBundle bundle = (BESBundle) dialog._besList.getSelectedValue();
		return bundle.epr();
	}
}