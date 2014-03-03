package edu.virginia.vcgr.genii.gjt.data.stage;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.DefaultDataItem;
import edu.virginia.vcgr.genii.gjt.data.xpath.XPathBuilder;
import edu.virginia.vcgr.jsdl.DataStaging;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;

public abstract class AbstractStageData extends DefaultDataItem implements
		StageData {
	@XmlTransient
	private StageProtocol _protocol;

	@XmlTransient
	private boolean _active = false;

	@SuppressWarnings("unused")
	private AbstractStageData() {
		// For deserialization only
	}

	protected abstract void activateImpl();

	protected abstract void deactivateImpl();

	@Override
	public void fireJobDescriptionModified() {
		if (_active)
			super.fireJobDescriptionModified();
	}

	@Override
	public void fireParameterizableStringModified(String oldValue,
			String newValue) {
		if (_active)
			super.fireParameterizableStringModified(oldValue, newValue);
	}

	protected AbstractStageData(StageProtocol protocol) {
		_protocol = protocol;
	}

	@Override
	final public StageProtocol protocol() {
		return _protocol;
	}

	@Override
	final public void activate() {
		_active = true;
		activateImpl();
	}

	@Override
	final public void deactivate() {
		deactivateImpl();
		_active = false;
	}

	@Override
	public void generateAdditionalJSDL(DataStaging jsdlStaging,
			XPathBuilder builder, Map<String, List<SweepParameter>> variables) {
		// Don't do anything
	}
}