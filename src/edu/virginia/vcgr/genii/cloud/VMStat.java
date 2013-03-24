package edu.virginia.vcgr.genii.cloud;

import java.sql.SQLException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VMStat
{

	private String _id;
	private String _besid;
	private VMState _state = null;
	private String _host;
	private int _port = 22;
	private int _load = 0;
	private boolean _prepared = false;
	private boolean _preparing = false;

	public void setPreparing()
	{
		_preparing = true;
	}

	public boolean preparing()
	{
		return _preparing;
	}

	static private Log _logger = LogFactory.getLog(VMStat.class);

	public VMStat()
	{

	}

	public VMStat(String id, String host, int port, int load, int setup, String besid)
	{

		_id = id;
		_host = host;
		_port = port;
		_load = load;
		_besid = besid;
		if (setup == 1)
			_prepared = true;

	}

	private VMStat(String id, String besid, VMState state, String host, int load, boolean prepared)
	{
		_id = id;
		_besid = besid;
		_state = state;
		_host = host;
		_load = load;
		_prepared = prepared;
		_port = -1;
	}

	public void setPrepared()
	{
		_prepared = true;
		updateResource();
	}

	public void setHost(String host)
	{
		_host = host;
	}

	public void setID(String id)
	{
		_id = id;
	}

	public void setBESID(String besid)
	{
		_besid = besid;
	}

	public String getID()
	{
		return _id;
	}

	public VMState getState()
	{
		return _state;
	}

	public void setState(VMState state)
	{
		_state = state;
	}

	public String getHost()
	{
		return _host;
	}

	public int getPort()
	{
		return _port;
	}

	public int getLoad()
	{
		return _load;
	}

	public void setLoad(int load)
	{
		_load = load;
		updateResource();
	}

	public void addWork()
	{
		_load += 1;
		updateResource();
	}

	public void removeWork()
	{
		_load -= 1;
		updateResource();
	}

	public boolean isReady()
	{
		return ((_state == VMState.RUNNING) && _prepared);
	}

	public void updateResource()
	{
		int prepared = 0;
		if (_prepared)
			prepared = 1;
		try {
			CloudMonitor.updateResource(_besid, _id, _load, prepared);
		} catch (SQLException e) {
			_logger.error(e);
		}

	}

	public MessageElement toMessageElement(QName elementName)
	{
		String tState = "";
		if (_state != null) {
			tState = _state.toString();
		}

		MessageElement ret = new MessageElement(elementName);

		MessageElement id = new MessageElement(new QName(CloudConstants.GENII_CLOUDBES_NS, "id"), _id);
		MessageElement besid = new MessageElement(new QName(CloudConstants.GENII_CLOUDBES_NS, "besid"), _besid);
		MessageElement state = new MessageElement(new QName(CloudConstants.GENII_CLOUDBES_NS, "state"), tState);
		MessageElement host = new MessageElement(new QName(CloudConstants.GENII_CLOUDBES_NS, "host"), _host);
		MessageElement load = new MessageElement(new QName(CloudConstants.GENII_CLOUDBES_NS, "load"), _load);
		MessageElement prepared = new MessageElement(new QName(CloudConstants.GENII_CLOUDBES_NS, "prepared"), _prepared);

		try {
			ret.addChild(id);
			ret.addChild(besid);
			ret.addChild(state);
			ret.addChild(host);
			ret.addChild(load);
			ret.addChild(prepared);

		} catch (SOAPException se) {
			throw new RuntimeException("Unexpected exception thrown while packageing policy.");
		}

		return ret;
	}

	static public VMStat fromMessageElement(MessageElement element)
	{

		String id = "";
		String besid = "";
		VMState state = null;
		String host = "";
		int load = 0;
		boolean prepared = false;

		Iterator<?> iter = element.getChildElements();
		while (iter.hasNext()) {
			MessageElement child = (MessageElement) iter.next();
			QName childName = child.getQName();

			if (childName.equals(new QName(CloudConstants.GENII_CLOUDBES_NS, "id")))
				id = child.getValue();
			else if (childName.equals(new QName(CloudConstants.GENII_CLOUDBES_NS, "besid")))
				besid = child.getValue();
			else if (childName.equals(new QName(CloudConstants.GENII_CLOUDBES_NS, "state"))) {
				if (child.getValue() != null)
					state = VMState.valueOf(child.getValue());
			} else if (childName.equals(new QName(CloudConstants.GENII_CLOUDBES_NS, "host")))
				host = child.getValue();
			else if (childName.equals(new QName(CloudConstants.GENII_CLOUDBES_NS, "load")))
				load = Integer.parseInt(child.getValue());
			else if (childName.equals(new QName(CloudConstants.GENII_CLOUDBES_NS, "prepared")))
				prepared = Boolean.getBoolean(child.getValue());

		}

		return new VMStat(id, besid, state, host, load, prepared);

	}

}
