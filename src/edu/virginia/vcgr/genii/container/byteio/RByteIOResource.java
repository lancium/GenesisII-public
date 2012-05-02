/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class RByteIOResource extends BasicDBResource implements IRByteIOResource
{
	static public QName FILE_PATH_PROPERTY = new QName(
		GenesisIIConstants.GENESISII_NS, "file-path");
	static protected final String _INTERNAL_FILE_PATH_PROP_NAME =
		"edu.virginia.vcgr.genii.byteio.rbyteio.file-path";
	static private final String _INTERNAL_CREATE_TIME_PROP_NAME=
		"edu.virginia.vcgr.genii.byteio.rbyteio.create-time";
	static private final String _INTERNAL_ACCESS_TIME_PROP_NAME=
		"edu.virginia.vcgr.genii.byteio.rbyteio.access-time";
	static protected final String _INTERNAL_BITMAP_FILE_PATH_PROP_NAME =
		"edu.virginia.vcgr.genii.byteio.rbyteio.bitmap-path";

	static private Log _logger = LogFactory.getLog(RByteIOResource.class);
	
	protected RByteIOResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public File chooseFile(HashMap<QName, Object> creationProperties)
		throws ResourceException
	{
		File userDir = Container.getConfigurationManager().getUserDirectory();
		File file = null;
		try
		{
			if (creationProperties != null)
			{
				MessageElement any = (MessageElement)creationProperties.get(
					FILE_PATH_PROPERTY);
				if (any != null)
				{
					file = new File(userDir, any.getAsString());
				}
			}
		}
		catch (ResourceException re)
		{
			throw re;
		}
		catch (Exception e)
		{
			throw new ResourceException(e.getLocalizedMessage(), e);
		}
		try
		{
			if (file == null)
			{
				file = ByteIOFileCreator.createFile(userDir);
			}
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getLocalizedMessage(), ioe);
		}
		String path = ByteIOFileCreator.getRelativePath(userDir, file);
		setProperty(_INTERNAL_FILE_PATH_PROP_NAME, path);
		return file;
	}

	public File getCurrentFile() throws ResourceException
	{
		String path = (String)getProperty(_INTERNAL_FILE_PATH_PROP_NAME);
		if (path == null)
			return chooseFile(null);
		File userDir = Container.getConfigurationManager().getUserDirectory();
		return ByteIOFileCreator.getAbsoluteFile(userDir, path);
	}
	
	public void destroy() throws ResourceException
	{
		try
		{
			File myFile = getCurrentFile();
			myFile.delete();
		}
		catch (ResourceException re)
		{
			_logger.error(re.getMessage());
		}
		
		super.destroy();
	}
	
	public String getFilePath()
		throws ResourceException
	{
		return (String) getProperty(_INTERNAL_FILE_PATH_PROP_NAME);
	}

	public void setCreateTime(Calendar tm)
		throws ResourceException
	{
		setProperty(_INTERNAL_CREATE_TIME_PROP_NAME, tm);
	}

	public Calendar getCreateTime()
		throws ResourceException
	{
		return (Calendar) getProperty(_INTERNAL_CREATE_TIME_PROP_NAME);
	}
	
	public void setModTime(Calendar tm)
		throws ResourceException
	{
		getCurrentFile().setLastModified(tm.getTimeInMillis());
	}

	public Calendar getModTime()
		throws ResourceException
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getCurrentFile().lastModified());
		return c;
	}
	
	public void setAccessTime(Calendar tm)
		throws ResourceException
	{
		setProperty(_INTERNAL_ACCESS_TIME_PROP_NAME, tm);
	}

	public Calendar getAccessTime()
		throws ResourceException
	{
		return (Calendar) getProperty(_INTERNAL_ACCESS_TIME_PROP_NAME);
	}
	
	public void setBitmapFilePath(String path)
		throws ResourceException
	{
		setProperty(_INTERNAL_BITMAP_FILE_PATH_PROP_NAME, path);
	}

	public File getBitmapFile()
		throws ResourceException
	{
		String path = (String) getProperty(_INTERNAL_BITMAP_FILE_PATH_PROP_NAME);
		if (path == null)
			return null;
		File userDir = Container.getConfigurationManager().getUserDirectory();
		return ByteIOFileCreator.getAbsoluteFile(userDir, path);
	}
}
