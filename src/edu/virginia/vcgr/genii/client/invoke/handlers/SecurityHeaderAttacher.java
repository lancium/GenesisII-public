package edu.virginia.vcgr.genii.client.invoke.handlers;

import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;

import edu.virginia.cs.vcgr.genii.job_management.SubmitJobRequestType;
import edu.virginia.cs.vcgr.genii.job_management.SubmitJobResponseType;
import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.queue.QueuePortType;

/*
 * This is the intercepter for retrieving lookup response from cache and submitting subscribe requests
 * for endPoints on which a lookup operation is invoked. 
 * */
public class SecurityHeaderAttacher {

	/*
	 * These set of interceptor methods set thread local variables with the
	 * logged in user's x.509 certificate. This variable (certificate) is
	 * attached to Soap headers AxisClientHeaderHandler when a job is submitted
	 * to a queue or a bes.
	 */

	//Intercept Job Submission to Queue
	@PipelineProcessor(portType = QueuePortType.class)
	public SubmitJobResponseType submitJob(InvocationContext ctxt,
			SubmitJobRequestType submitJobRequest) throws Throwable {
		MyProxyCertificate.setIfXSEDEUser();
		SubmitJobResponseType s = (SubmitJobResponseType) ctxt.proceed();
		MyProxyCertificate.reset();
		return s;
	}

	//Intercept Job Submission to BES
	@PipelineProcessor(portType = GeniiBESPortType.class)
	public CreateActivityResponseType createActivity(InvocationContext ctxt,
			CreateActivityType parameters) throws Throwable {
		MyProxyCertificate.setIfXSEDEUser();
		CreateActivityResponseType c = (CreateActivityResponseType) ctxt
				.proceed();
		MyProxyCertificate.reset();
		return c;
	}

}
