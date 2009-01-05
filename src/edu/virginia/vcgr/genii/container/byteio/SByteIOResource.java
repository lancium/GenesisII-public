package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class SByteIOResource extends RByteIOResource implements
		ISByteIOResource
{
	static public QName MUST_DESTROY_PROPERTY =
		new QName(GenesisIIConstants.GENESISII_NS, "must-destroy");
	static private final String _INTERNAL_MUST_DESTROY_PROPERTY =
		"edu.virginia.vcgr.genii.byteio.sbyteio.must-destroy";
	
	public SByteIOResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public File chooseFile(HashMap<QName, Object> creationProperties)
		throws ResourceException
	{
		MessageElement fileAny;
		MessageElement deleteAny;
		//String fileAny;
		//Boolean deleteAny;
		File file = null;
		Boolean mustDelete = null;
		
		if (!isServiceResource())
		{
			if (creationProperties == null)
				throw new ResourceException(
					"StreamableByteIO Instances MUST have a file path.");
			
			fileAny = (MessageElement)creationProperties.get(FILE_PATH_PROPERTY);
			//fileAny = (String)creationProperties.get(FILE_PATH_PROPERTY);
			if (fileAny == null)
				throw new ResourceException(
					"StreamableByteIO Instances MUST have a file path " +
					"element creation property.");
			deleteAny = (MessageElement)creationProperties.get(MUST_DESTROY_PROPERTY);
			//deleteAny = (Boolean)creationProperties.get(MUST_DESTROY_PROPERTY);
			if (deleteAny == null)
				throw new ResourceException(
					"StreamableByteIO Instances MUST have a must destroy " +
					"element creation property.");
			try
			{
				file = new File(fileAny.getValue());
				//file = new File(fileAny);
				mustDelete = Boolean.parseBoolean(deleteAny.getValue());
			}
			catch (Exception e)
			{
				throw new ResourceException(e.getLocalizedMessage(), e);
			}

		} else
		{
			mustDelete = true;
			file = super.chooseFile(null);
		}
		
		if (!file.exists())
			throw new ResourceException("Cannot read file \"" + 
				file.getAbsolutePath() + "\".");
		
		setProperty(_INTERNAL_FILE_PATH_PROP_NAME, file.getAbsolutePath());
		setProperty(_INTERNAL_MUST_DESTROY_PROPERTY, mustDelete);
		
		return file;
	}
	
	public void destroy() throws ResourceException
	{
		Boolean mustDestroy = (Boolean)getProperty(_INTERNAL_MUST_DESTROY_PROPERTY);
		if (mustDestroy != null && mustDestroy.booleanValue())
		{
			File myFile = getCurrentFile();
			myFile.delete();
		}
	}
}