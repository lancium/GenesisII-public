package org.morgan.util.gui.font;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("rawtypes")
class FontFamilyList extends JList
{
	static final long serialVersionUID = 0L;
	
	private FontModel _model;
	
	private class InternalListSelectionListener 
		implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			Object value = getSelectedValue();
			if (value != null)
				_model.setFamily(value.toString());
		}	
	}
	
	@SuppressWarnings("unchecked")
    FontFamilyList(FontModel model)
	{
		super(new FontFamilyListModel());
		
		_model = model;
		
		addListSelectionListener(new InternalListSelectionListener());
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		setCellRenderer(new FontFamilyListCellRenderer());
	}
}