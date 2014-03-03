package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.variables.BasicParameterizable;
import edu.virginia.vcgr.genii.gjt.data.variables.Parameterizable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableListener;

public class DefaultDataItem implements Parameterizable, Modifyable {
	@XmlTransient
	private BasicModifyable _modifyable = new BasicModifyable();

	@XmlTransient
	private BasicParameterizable _parameterizable = new BasicParameterizable();

	public void fireJobDescriptionModified() {
		_modifyable.fireJobDescriptionModified();
	}

	public void fireParameterizableStringModified(String oldValue,
			String newValue) {
		_parameterizable.fireParameterizableStringModified(oldValue, newValue);
	}

	@Override
	public void addParameterizableListener(ParameterizableListener listener) {
		_parameterizable.addParameterizableListener(listener);
	}

	@Override
	public void removeParameterizableListener(ParameterizableListener listener) {
		_parameterizable.removeParameterizableListener(listener);
	}

	@Override
	public void addModificationListener(ModificationListener listener) {
		_modifyable.addModificationListener(listener);
	}

	@Override
	public void removeModificationListener(ModificationListener listener) {
		_modifyable.removeModificationListener(listener);
	}
}