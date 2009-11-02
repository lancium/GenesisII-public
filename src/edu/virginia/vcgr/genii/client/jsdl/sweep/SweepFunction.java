package edu.virginia.vcgr.genii.client.jsdl.sweep;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlSeeAlso;

import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.LoopDoubleSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.LoopIntegerSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.ValuesSweepFunction;

@XmlSeeAlso({
	ValuesSweepFunction.class,
	LoopIntegerSweepFunction.class,
	LoopDoubleSweepFunction.class
})
public abstract class SweepFunction
	implements Countable, Serializable, Iterable<Object>
{
	static final long serialVersionUID = 0L;
}