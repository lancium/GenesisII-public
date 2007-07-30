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
package edu.virginia.vcgr.genii.client.byteio;

import javax.xml.namespace.QName;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;

public class ByteIOConstants
{
	static public final String TRANSFER_TYPE_SIMPLE =
		"http://schemas.ggf.org/byteio/2005/10/transfer-mechanisms/simple";
	static public final String TRANSFER_TYPE_DIME =
		"http://schemas.ggf.org/byteio/2005/10/transfer-mechanisms/dime";
	static public final String TRANSFER_TYPE_MTOM =
		"http://schemas.ggf.org/byteio/2005/10/transfer-mechanisms/mtom";
	
	static public final String SEEK_ORIGIN_CURRENT =
		"http://schemas.ggf.org/byteio/2005/10/streamable-access/seek-origins/current";
	static public final String SEEK_ORIGIN_BEGINNING =
		"http://schemas.ggf.org/byteio/2005/10/streamable-access/seek-origins/beginning";
	static public final String SEEK_ORIGIN_END =
		"http://schemas.ggf.org/byteio/2005/10/streamable-access/seek-origins/end";
	
	static public final String SHARED_BYTEIO_NS =
		"http://schemas.ggf.org/byteio/2005/10/byteio";
	
	static public final String RANDOM_BYTEIO_NS =
		"http://schemas.ggf.org/byteio/2005/10/random-access";
	
	static public final String STREAMABLE_BYTEIO_NS =
		"http://schemas.ggf.org/byteio/2005/10/streamable-access";
	
	//Shared Random/Streamable Attr
	static public String SIZE_ATTR_NAME = "Size";
	static public String READABLE_ATTR_NAME = "Readable";
	static public String WRITEABLE_ATTR_NAME = "Writeable";
	static public String XFER_MECHS_ATTR_NAME = "TransferMechanism";
	
	//Random Byte IO Attr
	static public QName CREATTIME_ATTR_NAME =
		new QName(RANDOM_BYTEIO_NS, "CreateTime");
	static public QName MODTIME_ATTR_NAME =
		new QName(RANDOM_BYTEIO_NS, "ModificationTime");
	static public QName ACCESSTIME_ATTR_NAME =
		new QName(RANDOM_BYTEIO_NS, "AccessTime");
	
	//Streamable Attr
	static public QName POSITION_ATTR_NAME =
		new QName(STREAMABLE_BYTEIO_NS, "Position");
	static public QName SEEKABLE_ATTR_NAME =
		new QName(STREAMABLE_BYTEIO_NS, "Seekable");
	static public QName END_OF_STREAM_ATTR_NAME =
		new QName(STREAMABLE_BYTEIO_NS, "EndOfStream");
	
	static public QName FILE_CHECKSUM_ATTR_NAME =
		new QName(GenesisIIConstants.GENESISII_NS, "Checksum");
	
	static public QName SIMPLE_XFER_DATA_QNAME = new QName(
		"http://schemas.ggf.org/byteio/2005/10/byte-io",
		"data");
	
	static public URI TRANSFER_TYPE_SIMPLE_URI;
	static public URI TRANSFER_TYPE_DIME_URI;
	static public URI TRANSFER_TYPE_MTOM_URI;
	
	static public URI SEEK_ORIGIN_CURRENT_URI;
	static public URI SEEK_ORIGIN_BEGINNING_URI;
	static public URI SEEK_ORIGIN_END_URI;
	
	static
	{
		try
		{
			TRANSFER_TYPE_DIME_URI = new URI(TRANSFER_TYPE_DIME);
			TRANSFER_TYPE_MTOM_URI = new URI(TRANSFER_TYPE_MTOM);
			TRANSFER_TYPE_SIMPLE_URI = new URI(TRANSFER_TYPE_SIMPLE);
			
			SEEK_ORIGIN_CURRENT_URI = new URI(SEEK_ORIGIN_CURRENT);
			SEEK_ORIGIN_BEGINNING_URI = new URI(SEEK_ORIGIN_BEGINNING);
			SEEK_ORIGIN_END_URI = new URI(SEEK_ORIGIN_END);
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}
	
	static public QName SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "sbyteio-subscribe");
	static public QName MUST_DESTROY_PROPERTY =
		new QName(GenesisIIConstants.GENESISII_NS, "must-destroy");
	
	static public int PREFERRED_SIMPLE_XFER_BLOCK_SIZE = 1024 * 512;
}