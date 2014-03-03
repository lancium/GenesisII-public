package edu.virginia.vcgr.genii.gjt.data.stage;

import java.util.EnumMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SerializableStageDataAdapter extends XmlAdapter<SerializableStageDataMap, Map<StageProtocol, StageData>>
{
	@Override
	public SerializableStageDataMap marshal(Map<StageProtocol, StageData> v) throws Exception
	{
		SerializableStageDataMap ret = new SerializableStageDataMap();

		for (StageData sd : v.values()) {
			if (sd != null)
				ret.add(new SerializableStageData(sd));
		}

		return ret;
	}

	@Override
	public Map<StageProtocol, StageData> unmarshal(SerializableStageDataMap v) throws Exception
	{
		Map<StageProtocol, StageData> ret = new EnumMap<StageProtocol, StageData>(StageProtocol.class);

		for (SerializableStageData ssd : v.stageData()) {
			StageData sd = ssd.stageData();

			ret.put(sd.protocol(), sd);
		}

		return ret;
	}
}