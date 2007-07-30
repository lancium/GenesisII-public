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
package edu.virginia.vcgr.genii.client.byteio.xfer;

import java.io.Closeable;
import java.rmi.RemoteException;

public interface IRByteIOTransferer extends Closeable
{
	public byte[] read(long startOffset, int bytesPerBlock,
		int numBlocks, long stride) throws RemoteException;
	
	public void write(long startOffset, int bytesPerBlock,
		long stride, byte []data) throws RemoteException;
	
	void append(byte []data) throws RemoteException;
	
	void truncAppend(long offset, byte []data) throws RemoteException;
}
