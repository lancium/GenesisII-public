package edu.virginia.vcgr.genii.gjt.data.stage;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.xpath.XPathBuilder;
import edu.virginia.vcgr.jsdl.DataStaging;
import edu.virginia.vcgr.jsdl.hpcfse.Credential;
import edu.virginia.vcgr.jsdl.hpcfse.UsernameToken;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;

public abstract class AbstractUsernamePasswordStageData extends AbstractStageData
{
	@XmlAttribute(name = "username")
	private String _username = null;

	@XmlAttribute(name = "password")
	private String _password = null;

	private AbstractUsernamePasswordStageData()
	{
		super(null);
		// For XML Deserialization only.
	}

	protected AbstractUsernamePasswordStageData(StageProtocol protocol)
	{
		super(protocol);
	}

	final public String username()
	{
		return _username;
	}

	final public void username(String username)
	{
		_username = username;

		fireJobDescriptionModified();
	}

	final public String password()
	{
		return _password;
	}

	final public void password(String password)
	{
		_password = password;

		fireJobDescriptionModified();
	}

	@Override
	public void analyze(String filename, Analysis analysis)
	{
		if (_username == null || _username.length() == 0)
			analysis.addWarning("No username entered for stage file \"%s\".", filename);

		if (_password == null || _password.length() == 0)
			analysis.addWarning("No password entered for stage file \"%s\".", filename);
	}

	@Override
	public void generateAdditionalJSDL(DataStaging jsdlStaging, XPathBuilder builder,
		Map<String, List<SweepParameter>> variables)
	{
		String username = (_username == null) ? "" : _username;
		String password = (_password == null) ? "" : _password;

		if (username.length() > 0 || _password.length() > 0)
			jsdlStaging.credentials(new Credential(new UsernameToken(username, password)));
	}
}