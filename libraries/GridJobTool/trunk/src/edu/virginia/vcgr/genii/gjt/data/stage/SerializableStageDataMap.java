package edu.virginia.vcgr.genii.gjt.data.stage;

import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;

public class SerializableStageDataMap
{
	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "stage-data")
	private Vector<SerializableStageData> _stageData;

	public SerializableStageDataMap()
	{
		_stageData = new Vector<SerializableStageData>();
	}

	public void add(SerializableStageData var)
	{
		_stageData.add(var);
	}

	public Vector<SerializableStageData> stageData()
	{
		return _stageData;
	}
}