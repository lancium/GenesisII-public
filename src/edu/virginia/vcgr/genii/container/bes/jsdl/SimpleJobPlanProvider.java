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
package edu.virginia.vcgr.genii.container.bes.jsdl;

import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;
import org.ggf.jsdl.posix.POSIXApplication_Type;

import edu.virginia.vcgr.genii.container.jsdl.ApplicationRedux;
import edu.virginia.vcgr.genii.container.jsdl.DataStagingRedux;
import edu.virginia.vcgr.genii.container.jsdl.DefaultJobPlanProvider;

public class SimpleJobPlanProvider extends DefaultJobPlanProvider
	implements IPOSIXProvider, IHPCProvider
{
	public ApplicationRedux createApplication(Application_Type applicationType)
	{
		return new SimpleApplicationRedux(this, applicationType);
	}
	
	public PosixApplicationRedux createPosixApplication(
		POSIXApplication_Type posixApplication)
	{
		return new PosixApplicationRedux(this, posixApplication);
	}
	
	public DataStagingRedux createDataStaging(
		DataStaging_Type []dataStaging)
	{
		return new SimpleDataStagingRedux(this, dataStaging);
	}

	public HPCProfileApplicationRedux createHPCApplication(
		HPCProfileApplication_Type hpcApplication)
	{
		return new HPCProfileApplicationRedux(this, hpcApplication);
	}
}
