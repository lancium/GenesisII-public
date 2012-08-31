package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.text.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.gui.TitledPanel;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.ui.UIContext;

class HistoryEventDisplayDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(
		HistoryEventDisplayDialog.class);
	
	@SuppressWarnings("unused")
	private UIContext _context;
	
	private JLabel _headerLabel = new JLabel();
	private JTextField _titleField = new JTextField();
	private EventSource _sourceLabel;
	private JLabel _levelLabel = new JLabel();
	private JLabel _categoryLabel = new JLabel();
	private HistoryPropertiesTableModel _properties = 
		new HistoryPropertiesTableModel();
	private JTextArea _detailsArea = new JTextArea();
	
	private JPanel createHeaderPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		panel.add(_headerLabel, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		panel.add(_sourceLabel, new GridBagConstraints(
			1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		return panel;
	}
	
	private JPanel createTitlePanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		panel.add(new JLabel("Event Title"), new GridBagConstraints(
			0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		panel.add(_titleField, new GridBagConstraints(
			1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		return panel;
	}
	
	private JPanel createCategoryAndLevelPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		panel.add(new JLabel("Event Level"), new GridBagConstraints(
			0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		panel.add(_levelLabel, new GridBagConstraints(
			1, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		panel.add(new JLabel("Event Category"), new GridBagConstraints(
			2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		panel.add(_categoryLabel, new GridBagConstraints(
			3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		return panel;
	}
	
	private void setDetails(Document detailsDoc, String details)
	{
		try
		{
			detailsDoc.remove(0, detailsDoc.getLength());
			detailsDoc.insertString(0, details, null);
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to set details pane.", cause);
		}
	}
	
	HistoryEventDisplayDialog(Window owner, UIContext context, 
		HistoryEvent event)
	{
		this(owner, context);
		
		setEvent(event);
	}
	
	HistoryEventDisplayDialog(Window owner, UIContext context)
	{
		super(owner);
		
		_context = context;
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		_sourceLabel = new EventSource(context);
		
		_titleField.setEditable(false);
		_sourceLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.black),
			BorderFactory.createEmptyBorder(0, 5, 0, 5)));
		
		_levelLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			BorderFactory.createEmptyBorder(0, 2, 0, 2)));
		_categoryLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			BorderFactory.createEmptyBorder(0, 2, 0, 2)));
		_detailsArea.setEditable(false);
		_detailsArea.setWrapStyleWord(true);
		_detailsArea.setLineWrap(true);
		
		content.add(createHeaderPanel(),
			new GridBagConstraints(0, 0, 1, 1,
			1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		content.add(createTitlePanel(), new GridBagConstraints(0, 1, 1, 1,
			1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		content.add(createCategoryAndLevelPanel(), new GridBagConstraints(
			0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		
		JTable propertiesTable = new JTable(_properties);
		propertiesTable.getColumnModel().getColumn(0).setHeaderValue("Property Name");
		propertiesTable.getColumnModel().getColumn(1).setHeaderValue("Property Value");
		JScrollPane propertiesScroller = new JScrollPane(propertiesTable);
		Dimension dim = new Dimension(200, 100);
		propertiesScroller.setMinimumSize(dim);
		propertiesScroller.setMaximumSize(dim);
		propertiesScroller.setPreferredSize(dim);
		content.add(new TitledPanel("Event Properties", propertiesScroller),
			new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		
		JScrollPane detailsScroller = new JScrollPane(_detailsArea);
		dim = new Dimension(800, 200);
		detailsScroller.setMinimumSize(dim);
		detailsScroller.setMaximumSize(dim);
		detailsScroller.setPreferredSize(dim);
		content.add(new TitledPanel("Event Details", detailsScroller),
			new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	void setEvent(HistoryEvent event)
	{
		Document details = _detailsArea.getDocument();
		
		if (event == null)
		{
			setTitle("No Event Selected");
			
			_headerLabel.setText("No Event Selected");
			_titleField.setText("No Event Selected");
			_levelLabel.setText("");
			_categoryLabel.setText("");
			_categoryLabel.setIcon(null);
			_properties.setProperties(null);
			setDetails(details, "");
			_sourceLabel.setEventSource(null);
		} else
		{
			setTitle(event.eventData().toString());
			
			_headerLabel.setText(String.format(
				"Event %1$s occurred at %2$tr on %2$tF", event.eventNumber(), 
				event.eventTimestamp()));
			_titleField.setText(event.eventData().toString());
			_levelLabel.setText(event.eventLevel().toString());
			_categoryLabel.setIcon(
				event.eventCategory().information().categoryIcon());
			_categoryLabel.setText(event.eventCategory().toString());
			_properties.setProperties(event.eventProperties());
			setDetails(details, event.eventData().details());
			_sourceLabel.setEventSource(event.eventSource());
			
			validate();
		}
		
		_detailsArea.setCaretPosition(0);
	}
}
