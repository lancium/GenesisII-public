package edu.virginia.vcgr.genii.client.jsdl.parser;

import javax.xml.namespace.QName;

import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.ContainerDataStage;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelative;
import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultDataStagingFacet;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class ExecutionDataStagingFacet extends DefaultDataStagingFacet
{
	private CreationFlagEnumeration _creationFlag = null;
	private Boolean _deleteOnTerminate = null;
	private Boolean _handleAsArchive = null;
	private Boolean _alwaysStageOut = null;
	private String _filesystemName = null;
	private String _fileName = null;
	private UsernamePasswordIdentity _credential = null;
	private String _sourceURI = null;
	private String _targetURI = null;

	void setSourceURI(String sourceURI)
	{
		_sourceURI = sourceURI;
	}

	void setTargetURI(String targetURI)
	{
		_targetURI = targetURI;
	}

	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
	{
		return this;
	}

	@Override
	public void consumeCreationFlag(Object currentUnderstanding, CreationFlagEnumeration creationFlag) throws JSDLException
	{
		if (creationFlag == CreationFlagEnumeration.append)
			throw new UnsupportedJSDLElement("Creation flag \"append\" is not supported.", new QName(JSDLConstants.JSDL_NS, "CreationFlag"));

		_creationFlag = creationFlag;
	}

	@Override
	public void consumeDeleteOnTerminateFlag(Object currentUnderstanding, boolean deleteOnTerminate)
	{
		_deleteOnTerminate = new Boolean(deleteOnTerminate);
	}

	@Override
	public void consumeHandleAsArchiveFlag(Object currentUnderstanding, boolean handleAsArchive)
	{
		_handleAsArchive = new Boolean(handleAsArchive);
	}
	
	@Override
	public void consumeAlwaysStageOutFlag(Object currentUnderstanding, boolean alwaysStageOut)
	{
		_alwaysStageOut = new Boolean(alwaysStageOut);
	}

	@Override
	public void consumeFileSystemName(Object currentUnderstanding, String filesystemName)
	{
		_filesystemName = filesystemName;
	}

	@Override
	public void consumeFileName(Object currentUnderstanding, String fileName)
	{
		_fileName = fileName;
	}

	@Override
	public void consumeUsernamePassword(Object currentUnderstanding, UsernamePasswordIdentity credential)
	{
		_credential = credential;
	}

	@Override
	public void completeFacet(Object parentUnderstanding, Object currentUnderstanding)
	{
		JobRequest jr = (JobRequest) parentUnderstanding;
		jr.addDataStage(new ContainerDataStage(new FilesystemRelative<String>(_filesystemName, _fileName), _deleteOnTerminate,
			_handleAsArchive, _creationFlag, _sourceURI, _targetURI, _credential));
	}
}