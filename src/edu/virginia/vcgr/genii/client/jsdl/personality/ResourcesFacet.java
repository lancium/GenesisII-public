package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;

public interface ResourcesFacet extends PersonalityFacet
{
	public void consumeExclusiveExecution(
		Object currentUnderstanding, 
		boolean exclusiveExecution) throws JSDLException;
	public void consumeIndividualCPUSpeed(
		Object currentUnderstanding,
		RangeExpression individualCPUSpeed) throws JSDLException;
	public void consumeIndividualCPUTime(
		Object currentUnderstanding,
		RangeExpression individualCPUTime) throws JSDLException;
	public void consumeIndividualCPUCount(
		Object currentUnderstanding,
		RangeExpression individualCPUCount) throws JSDLException;
	public void consumeIndividualNetworkBandwidth(
		Object currentUnderstanding,
		RangeExpression individualNetworkBandwidth) throws JSDLException;
	public void consumeIndividualPhysicalMemory(
		Object currentUnderstanding,
		RangeExpression individualPhysicalMemory) throws JSDLException;
	public void consumeIndividualVirtualMemory(
		Object currentUnderstanding,
		RangeExpression individualVirtualMemory) throws JSDLException;
	public void consumeIndividualDiskSpace(
		Object currentUnderstanding,
		RangeExpression individualDiskSpace) throws JSDLException;
	public void consumeTotalCPUTime(
		Object currentUnderstanding,
		RangeExpression totalCPUTime) throws JSDLException;
	public void consumeTotalCPUCount(
		Object currentUnderstanding,
		RangeExpression totalCPUCount) throws JSDLException;
	public void consumeTotalPhysicalMemory(
		Object currentUnderstanding,
		RangeExpression totalPhysicalMemory) throws JSDLException;
	public void consumeTotalVirtualMemory(
		Object currentUnderstanding,
		RangeExpression totalVirtualMemory) throws JSDLException;
	public void consumeTotalDiskSpace(
		Object currentUnderstanding,
		RangeExpression totalDiskSpace) throws JSDLException;
	public void consumeTotalResourceCount(
		Object currentUnderstanding,
		RangeExpression totalResourceCount) throws JSDLException;
}