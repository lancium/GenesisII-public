package edu.virginia.vcgr.genii.cloud;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;


public class CloudStat {


	private int _available;
	private int _total;
	private int _busy;
	private int _pending;
	private int _preparing;
	private String _type;
	private String _description;



	public CloudStat(int _available, int _total, int _busy,
			int _pending, int preparing,String _type, String _description) {
		
		
		this._available = _available;
		this._total = _total;
		this._busy = _busy;
		this._pending = _pending;
		this._preparing = preparing;
		this._type = _type;
		this._description = _description;
	}



	@Override
	public String toString() {
		return " Available=" + _available + "\n Total=" + _total + "\n" +  
		" Busy=" + _busy + "\n Pending=" + _pending +
		"\n Preparing= " + _preparing;
	}


	public MessageElement toMessageElement(QName elementName)
	{
		MessageElement ret = new MessageElement(elementName);

		MessageElement available = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"available"), _available);
		MessageElement total = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"total"), _total);
		MessageElement busy = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"busy"), _busy);
		MessageElement pending = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"pending"), _pending);
		MessageElement preparing = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"preparing"), _preparing);
		MessageElement type = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"type"), _type);
		MessageElement description = new MessageElement(
				new QName(CloudConstants.GENII_CLOUDBES_NS,
						"description"), _description);


		try
		{
			ret.addChild(available);
			ret.addChild(total);
			ret.addChild(busy);
			ret.addChild(pending);
			ret.addChild(preparing);
			ret.addChild(type);
			ret.addChild(description);

		}
		catch (SOAPException se)
		{
			throw new RuntimeException(
			"Unexpected exception thrown while packageing policy.");
		}

		return ret;
	}

	static public CloudStat fromMessageElement(MessageElement element)
	{
		int available = 0;
		int total = 0;
		int busy = 0;
		int pending = 0;
		int preparing = 0;
		String type = "";
		String description = "";

		Iterator<?> iter = element.getChildElements();
		while (iter.hasNext())
		{
			MessageElement child = (MessageElement)iter.next();
			QName childName = child.getQName();

			if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "available")))
				available = Integer.parseInt(child.getValue());
			else if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "total")))
				total = Integer.parseInt(child.getValue());
			else if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "busy")))
				busy = Integer.parseInt(child.getValue());
			else if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "pending")))
				pending = Integer.parseInt(child.getValue());
			else if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "preparing")))
				preparing = Integer.parseInt(child.getValue());
			else if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "type")))
				type = child.getValue();
			else if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, "description")))
				description = child.getValue();

		}

		return new CloudStat(available, total, busy,
				pending, preparing, type, description);
	}

}
