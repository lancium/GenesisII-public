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
package edu.virginia.vcgr.genii.container.jsdl;

import java.io.File;

public abstract class BaseRedux
{
	private IJobPlanProvider _provider;
	
	protected BaseRedux(IJobPlanProvider provider)
	{
		_provider = provider;
	}
	
	protected IJobPlanProvider getProvider()
	{
		return _provider;
	}
	
	protected File mergePaths(File pathOne, String pathTwo)
	{
		File tmp = new File(pathTwo);
		if (pathOne == null)
			return tmp;
		if (tmp.isAbsolute())
			return tmp;
		return new File(pathOne, pathTwo);
	}
	
	public void verifyComplete() throws JSDLException
	{
	}
	
	public abstract void consume() throws JSDLException;
}
