/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.jsdl.sweep;

import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.dom.DOMResult;

import edu.virginia.vcgr.jsdl.JSDLUtility;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.sweep.eval.EvaluationContext;

/**
 * The SweepUtility class encapsulates common methods for dealing with parameter sweeps. In
 * particular, it has a public method for determining the cardinality of a parameter sweep (that is,
 * how many jobs a parameter sweep JSDL document represents). It also has methods for performing the
 * sweep (creating individual singleton jobs from a jsdl sweep definition).
 * 
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class SweepUtility
{
	/**
	 * Retrieves the cardinality (number of singleton jobs) represented by a single JSDL Parameter
	 * Sweep document. If the JobDefinition given does not represent a parameter sweep, the
	 * cardinality is 1.
	 * 
	 * @param jobDef
	 *            The job definition document to determine the cardinality of.
	 * 
	 * @return The cardinality of the represented job definition.
	 */
	static public int sweepSize(JobDefinition jobDef)
	{
		if (jobDef == null)
			return 0;

		List<Sweep> sweeps = jobDef.parameterSweeps();
		if (sweeps == null || sweeps.size() == 0)
			return 1;

		int count = 0;

		for (Sweep sweep : sweeps)
			count += sweep.size();

		return count;
	}

	/**
	 * Performs the parameter sweep described by the given Job Definition. This method iterates
	 * through all of the parameters represented by the given parameter sweep document and, for each
	 * single job that it produces, calls an "emit" method on a provided callback handler. If the
	 * job definition that is given does not represent a parameter sweep, then the emit method is
	 * called exactly once for the original job definition document.
	 * 
	 * @param context
	 *            The JAXBContext that will be used to marshall and unmarshall the XML jsdl
	 *            document. If this parameter is null, then the default JAXBContext for marshalling
	 *            and unmarshalling JSDL will be used.
	 * @param jobDef
	 *            The job definition document to "sweep" over.
	 * @param listener
	 *            The callback listener that will receive singleton jobs produced by the parameter
	 *            sweep.
	 * @return A sweep token that can be used to "block" for the sweep to finish.
	 * @throws SweepException
	 */
	static public SweepToken performSweep(JAXBContext context, JobDefinition jobDef, SweepListener listener)
		throws SweepException
	{
		if (context == null)
			context = JSDLUtility.JSDLContext;

		SweepTokenImpl token = new SweepTokenImpl();

		if (jobDef == null || listener == null) {
			token.completeSweep(null);
			return token;
		}

		List<Sweep> parameterSweeps = jobDef.parameterSweeps();
		Thread th = new Thread(new SweepCreator(token, jobDef, parameterSweeps, listener, context));
		th.setDaemon(false);
		th.start();

		return token;
	}

	/**
	 * This method is identical to the more specific performSweep operation defined above except
	 * that it assumes that you will be using the default JAXBContext provided with this library.
	 * 
	 * @param jobDef
	 *            The job definition document to "sweep" over.
	 * @param listener
	 *            The callback listener that will receive singleton jobs produced by the parameter
	 *            sweep.
	 * @return A sweep token that can be used to "block" for the sweep to finish.
	 * @throws SweepException
	 */
	static public SweepToken performSweep(JobDefinition jobDef, SweepListener listener) throws SweepException
	{
		return performSweep(JSDLUtility.JSDLContext, jobDef, listener);
	}

	static private class SweepTokenImpl implements SweepToken
	{
		private boolean _done;
		private SweepException _exception;

		synchronized private void completeSweep(SweepException exception)
		{
			_done = true;
			_exception = exception;
			notifyAll();
		}

		private SweepTokenImpl()
		{
			_done = false;

			_exception = null;
		}

		@Override
		synchronized public void join() throws SweepException, InterruptedException
		{
			while (!_done) {
				wait();
			}

			if (_exception != null)
				throw _exception;
		}
	}

	static private class SweepCreator implements Runnable
	{
		private SweepTokenImpl _token;
		private List<Sweep> _parameterSweeps;
		private SweepListener _listener;
		private JobDefinition _jobDefinition;
		private JAXBContext _context;

		private SweepCreator(SweepTokenImpl token, JobDefinition jobDefinition, List<Sweep> parameterSweeps,
			SweepListener listener, JAXBContext context)
		{
			_token = token;
			_parameterSweeps = parameterSweeps;
			_listener = listener;
			_jobDefinition = jobDefinition;
			_context = context;
		}

		@Override
		public void run()
		{
			try {
				if (_parameterSweeps == null || _parameterSweeps.size() == 0)
					_listener.emitSweepInstance(_jobDefinition);
				else {
					List<Sweep> copy = new Vector<Sweep>(_parameterSweeps);
					_parameterSweeps.clear();

					try {
						DOMResult domResult = new DOMResult();
						_context.createMarshaller().marshal(_jobDefinition, domResult);
						EvaluationContext evaluationContext =
							new EvaluationContext(_listener, _context.createUnmarshaller(), domResult.getNode());
						for (Sweep sweep : copy)
							sweep.evaluate(evaluationContext);
					} catch (JAXBException e) {
						throw new SweepException("Unable to marshall Job Definition document.", e);
					}
				}

				_token.completeSweep(null);
			} catch (SweepException se) {
				_token.completeSweep(se);
			}
		}
	}
}