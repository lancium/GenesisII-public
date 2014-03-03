package edu.virginia.vcgr.genii.gjt.data.stage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;
import edu.virginia.vcgr.genii.gjt.data.stage.ftp.FtpStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.grid.GridStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.http.HttpStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.mailto.MailtoStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.scp.ScpStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.undef.UndefinedStageData;

public class SerializableStageData {
	@XmlElements({
			@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "undefined-stage", type = UndefinedStageData.class),
			@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "http", type = HttpStageData.class),
			@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "grid", type = GridStageData.class),
			@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "mailto", type = MailtoStageData.class),
			@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "ftp", type = FtpStageData.class),
			@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "scp", type = ScpStageData.class) })
	private StageData _stageData;

	public SerializableStageData(StageData stageData) {
		_stageData = stageData;
	}

	public SerializableStageData() {
		this(null);
	}

	final public StageData stageData() {
		return _stageData;
	}
}
