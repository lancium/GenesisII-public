package edu.virginia.vcgr.genii.client.gui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

public class PasswordDialog extends JDialog implements ActionListener
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(PasswordDialog.class);
	
	static private final String _OK_ACTION = "OK";
	static private final String _CANCEL_ACTION = "CANCEL";
	
	static String prompt = "";
	static String title = "";
	
	private JPasswordField _password;
	
	private static SynchronousQueue<String> waitingQueue = new SynchronousQueue<String>();
	
	private PasswordDialog(String title, String prompt)
	{
		super();
		
		setTitle(title);
		Container container = getContentPane();
		
		container.setLayout(new GridBagLayout());
		
		container.add(new JLabel(prompt),
			new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add( (_password = new JPasswordField()),
			new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		_password.setActionCommand(_OK_ACTION);
		_password.addActionListener(this);		

		JButton button = new JButton("OK");
		button.setActionCommand(_OK_ACTION);
		button.addActionListener(this);
		container.add(button, 
			new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 10, 10));
		
		button = new JButton("Cancel");
		button.setActionCommand(_CANCEL_ACTION);
		button.addActionListener(this);
		container.add(button, 
			new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 10, 10));			
	}

	public void actionPerformed(ActionEvent arg0)
	{
		try {
			if (arg0.getActionCommand().equals(_CANCEL_ACTION)){
				waitingQueue.put(new String("cancel"));
				setVisible(false);
			}
			else if(arg0.getActionCommand().equals(_OK_ACTION)){
				waitingQueue.put(new String(_password.getPassword()));
				setVisible(false);
			} 
		}catch (InterruptedException e) {
			_logger.info("exception occurred in actionPerformed", e);
		}		
	}
	
	public static char[] getPassword(String t, String p)
	{
		PasswordDialog.title = t;
		PasswordDialog.prompt = p;
		
		//	Swing thread-safe initializer
        javax.swing.SwingUtilities.invokeLater(new Runnable() {        	
        	
            public void run() {
            	PasswordDialog pDialog = new PasswordDialog(title, prompt);
        		pDialog.pack();
        		GuiUtils.centerComponent(pDialog);
        		pDialog.setAlwaysOnTop(true);
        		pDialog.setModal(true);
        		pDialog.setVisible(true);
        		pDialog.requestFocusInWindow();
            }            
        });
		try {			
			Object toReturn = waitingQueue.take();
			if(toReturn instanceof String && 
					((String)toReturn).equals("cancel")){
				return null;
			}
			else{
				return ((String)toReturn).toCharArray();
			}			
		} catch (InterruptedException e) {			
			_logger.info("exception occurred in getPassword::run", e);
			return null;
		}
	}
}
