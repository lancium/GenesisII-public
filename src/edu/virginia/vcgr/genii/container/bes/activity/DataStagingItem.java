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
package edu.virginia.vcgr.genii.container.bes.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.ggf.jsdl.CreationFlagEnumeration;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.SourceTarget_Type;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;

public class DataStagingItem implements Serializable
{
	static final long serialVersionUID = 0;
	
	private String _filename;
	private InternalCreationFlagEnumeration _creationFlag; 
	private boolean _deleteOnTerminate;
	private SourceDataStage _source = null;
	private TargetDataStage _target = null;
	
	/* For Deserialization only */
	public DataStagingItem()
	{
		_filename = null;
		_creationFlag = InternalCreationFlagEnumeration.DONT_OVERWRITE;
		_deleteOnTerminate = true;
	}
	
	public DataStagingItem(DataStaging_Type stage) throws JSDLException
	{
		_filename = stage.getFileName();
		
		CreationFlagEnumeration flag = stage.getCreationFlag();
		if (flag == null)
			throw new InvalidJSDLException("CreationFlag must be specified.");
		else if (flag.equals(CreationFlagEnumeration.append))
			_creationFlag = InternalCreationFlagEnumeration.APPEND;
		else if (flag.equals(CreationFlagEnumeration.overwrite))
			_creationFlag = InternalCreationFlagEnumeration.OVERWRITE;
		else if (flag.equals(CreationFlagEnumeration.dontOverwrite))
			_creationFlag = InternalCreationFlagEnumeration.DONT_OVERWRITE;
		else
			throw new InvalidJSDLException("CreationFlag \"" + flag 
				+ "\" is invalid.");
		
		_deleteOnTerminate = true;
		SourceTarget_Type source = stage.getSource();
		SourceTarget_Type target = stage.getTarget();
		if (source != null)
			_source = new SourceDataStage(source);
		if (target != null)
			_target = new TargetDataStage(target);
	}
	
	public void stageIn(File baseDir) throws IOException
	{
		if (_source != null)
		{
			InputStream in = null;
			OutputStream out = null;
			
			try
			{
				in = _source.openStream();
				out = openFileAsOutput(baseDir, _filename, _creationFlag);
				copy(in, out);
			}
			finally
			{
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}
	}
	
	public void stageOut(File baseDir) throws IOException
	{
		if (_target != null)
		{
			InputStream in = null;
			OutputStream out = null;
			
			try
			{
				in = openFileAsInput(baseDir, _filename);
				out = _target.openStream();
				copy(in, out);
			}
			finally
			{
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}
	}
	
	public void cleanup(File basedir)
	{
		if (_deleteOnTerminate)
		{
			File f = new File(basedir, _filename);
			f.delete();
		}
	}
	
	static protected InputStream openFileAsInput(File baseDir, String filename)
		throws IOException
	{
		File newFile = new File(baseDir, filename);
		return new FileInputStream(newFile);
	}
	
	static protected OutputStream openFileAsOutput(File baseDir, String filename,
		InternalCreationFlagEnumeration creationFlag) throws IOException
	{
		File newFile = new File(baseDir, filename);
		
		if (creationFlag.equals(InternalCreationFlagEnumeration.APPEND))
			return new FileOutputStream(newFile, true);
		else if (creationFlag.equals(InternalCreationFlagEnumeration.OVERWRITE))
			return new FileOutputStream(newFile);
		else
		{
			if (newFile.exists())
				throw new IOException("File \"" + filename + "\" already exists.");
			return new FileOutputStream(newFile);
		}
	}
	
	static protected void copy(InputStream in, OutputStream out) throws IOException
	{
		byte []data = new byte[1024 * 16];
		int read;
		
		while ( (read = in.read(data)) >= 0 )
		{
			out.write(data, 0, read);
		}
	}
}
