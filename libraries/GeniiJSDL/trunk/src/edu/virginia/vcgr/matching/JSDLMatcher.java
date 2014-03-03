package edu.virginia.vcgr.matching;

import java.util.Map;

import edu.virginia.vcgr.jsdl.JobDefinition;

/**
 * This interface is a hook for outside JSDL handlers to use to ask a component
 * of their system whether or not that component can handle a given job (for
 * whatever definition of "handle" is appropriate for that component).
 * 
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public interface JSDLMatcher {
	/**
	 * Analyze a job and determine whether or not it can be handled by this
	 * component.
	 * 
	 * @param analyzationContext
	 *            An arbitrary context by which the caller can register implicit
	 *            parameters that aid in the analysis of the job definition. At
	 *            the time of the writing of this interface, no context
	 *            information is known.
	 * @param jobDefinition
	 *            The job definition document which describes the job to
	 *            analyze.
	 * @return An analysis of the job that indicates whether the job passes or
	 *         fails.
	 */
	public JobAnalysis analyzeJob(Map<String, Object> analyzationContext,
			JobDefinition jobDefinition);
}