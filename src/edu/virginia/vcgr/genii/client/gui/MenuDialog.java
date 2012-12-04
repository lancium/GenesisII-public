package edu.virginia.vcgr.genii.client.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("rawtypes")
public class MenuDialog<EntryType> extends JDialog 
	implements ActionListener, ListSelectionListener
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(MenuDialog.class);
	
	static private final String _OK_ACTION = "OK";
	static private final String _CANCEL_ACTION = "CANCEL";
	
	private JList _list;
	private JButton _okButton = null;
	
	static String title = "";
	static String prompt = "";
	
	static Collection<?> entries;
	
	static SynchronousQueue<Object> waitingQueue = new SynchronousQueue<Object>();
	
	private Object[] createListContents(
		Collection<?> entries)
	{
		Object []ret = new Object[entries.size()];
		entries.toArray(ret);
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
    private MenuDialog(String title, String prompt, 
		Collection<?> entries)
	{
		super();
		
		setTitle(title);
		Container container = getContentPane();
		
		container.setLayout(new GridBagLayout());
		
		container.add(new JLabel(prompt),
			new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		container.add( new JScrollPane(
			_list = new JList(createListContents(entries))),
			new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.addListSelectionListener(this);
		_list.addMouseListener(new ActionJList(_list));

		JButton button = new JButton("OK");
		button.setActionCommand(_OK_ACTION);
		button.addActionListener(this);
		container.add(button, 
			new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 10, 10));
		_okButton = button;
		_okButton.setEnabled(false);
		
		button = new JButton("Cancel");
		button.setActionCommand(_CANCEL_ACTION);
		button.addActionListener(this);
		container.add(button, 
			new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 10, 10));
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent arg0)
	{
		try{
			if (arg0.getActionCommand().equals(_CANCEL_ACTION))
				waitingQueue.put(new String("cancel"));
			else
				waitingQueue.put((EntryType)_list.getSelectedValue());
		}catch(InterruptedException e){
			_logger.info("exception occurred in actionPerformed", e);
		}
		
		setVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	static public <EntryType> EntryType getMenuSelection(
		String t, String p,
			Collection<? extends EntryType> e)
	{	
		MenuDialog.entries = e;
		MenuDialog.title = t;
		MenuDialog.prompt = p;
		
		//	Swing thread-safe initializer
        javax.swing.SwingUtilities.invokeLater(new Runnable() {        	
        	
           public void run() {
            	MenuDialog<EntryType> mDialog = new MenuDialog<EntryType>(
        			title, prompt, entries);
        		mDialog.pack();
        		GuiUtils.centerComponent(mDialog);
        		mDialog.setAlwaysOnTop(true);
        		mDialog.setModal(true);
        		mDialog.setVisible(true);
            }
            
        });
		try {
			Object toReturn = waitingQueue.take();
			if(toReturn instanceof String && 
					((String)toReturn).equals("cancel")){
				return null;
			}
			else{
				return (EntryType)toReturn;
			}
		} catch (InterruptedException e1) {			
			_logger.info("exception occurred in run", e1);
			return null;
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		int []selected = _list.getSelectedIndices();
		if (selected == null || selected.length == 0)
			_okButton.setEnabled(false);
		else
			_okButton.setEnabled(true);
	}
	
	private class ActionJList extends MouseAdapter
	{
		protected JList _list;
				    
		public ActionJList(JList l)
		{
			_list = l;
		}
		    
		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2)
			{
				int index = _list.locationToIndex(e.getPoint());
				_list.ensureIndexIsVisible(index);
				setVisible(false);
			}
		}
	}
}
