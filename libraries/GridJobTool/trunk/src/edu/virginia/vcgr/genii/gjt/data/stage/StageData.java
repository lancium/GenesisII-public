package edu.virginia.vcgr.genii.gjt.data.stage;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlSeeAlso;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.ftp.FtpStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.grid.GridStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.http.HttpStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.mailto.MailtoStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.scp.ScpStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.undef.UndefinedStageData;
import edu.virginia.vcgr.genii.gjt.data.xpath.XPathBuilder;
import edu.virginia.vcgr.jsdl.DataStaging;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;

@XmlSeeAlso({ UndefinedStageData.class, GridStageData.class,
		HttpStageData.class, MailtoStageData.class, FtpStageData.class,
		ScpStageData.class })
public interface StageData {
	public StageProtocol protocol();

	public void analyze(String filename, Analysis analysis);

	public void generateAdditionalJSDL(DataStaging jsdlStaging,
			XPathBuilder builder, Map<String, List<SweepParameter>> variables);

	public String getJSDLURI();

	public void activate();

	public void deactivate();
}