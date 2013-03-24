package edu.virginia.vcgr.genii.container.bes;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.nativeq.CommonScriptBasedQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.ExecutableApplicationConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.UnixSignals;
import edu.virginia.vcgr.genii.client.nativeq.pbs.PBSQueue;
import edu.virginia.vcgr.genii.client.nativeq.pbs.PBSQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.sge.SGEQueue;
import edu.virginia.vcgr.genii.client.nativeq.sge.SGEQueueConfiguration;
import edu.virginia.vcgr.genii.client.utils.units.ClockSpeed;
import edu.virginia.vcgr.genii.client.utils.units.ClockSpeedUnits;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.client.utils.units.SizeUnits;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

class Version1Upgrader
{
	static private OperatingSystemNames getOperatingSystemName(Properties props, String propName)
	{
		String value = props.getProperty(propName);
		return (value == null) ? null : OperatingSystemNames.valueOf(value);
	}

	static private String getString(Properties props, String name)
	{
		return props.getProperty(name);
	}

	static private ProcessorArchitecture getProcessorArchitecture(Properties props, String name)
	{
		String value = props.getProperty(name);
		return (value == null) ? null : ProcessorArchitecture.valueOf(value);
	}

	static private Integer getInteger(Properties props, String name)
	{
		String value = props.getProperty(name);
		return (value == null) ? null : Integer.parseInt(value);
	}

	static private ClockSpeed getClockSpeed(Properties props, String name)
	{
		String value = props.getProperty(name);
		if (value == null)
			return null;

		double d = Double.parseDouble(value);
		return new ClockSpeed(d, ClockSpeedUnits.Hertz);
	}

	static private Size getSize(Properties props, String name)
	{
		String value = props.getProperty(name);
		if (value == null)
			return null;

		double d = Double.parseDouble(value);
		return new Size(d, SizeUnits.Bytes);
	}

	static private Collection<UnixSignals> getSignals(Properties props, String name) throws IOException
	{
		String value = props.getProperty(name);
		if (value == null)
			return null;

		Collection<String> signalStrings = new LinkedList<String>();
		for (String signal : value.split(",")) {
			if (signal != null) {
				signal = signal.trim();
				if (signal.length() > 0) {
					signalStrings.add(signal);
				}
			}
		}

		return UnixSignals.parseTrapAndKillSet(signalStrings);
	}

	static private boolean upgrade(ResourceOverrides overrides, Properties props)
	{
		boolean ret = false;

		final String BASE = "edu.virginia.vcgr.genii.native-q.resource-override.";
		final String OSNAME = BASE + "operating-system-name";
		final String OSVER = BASE + "operating-system-version";
		final String CPUARCH = BASE + "cpu-architecture-name";
		final String CPUCOUNT = BASE + "cpu-count";
		final String CPUSPEED = BASE + "cpu-speed";
		final String PHYSICALMEM = BASE + "physical-memory";
		final String VIRTUALMEM = BASE + "virtual-memory";

		OperatingSystemNames osName = getOperatingSystemName(props, OSNAME);
		if (osName != null) {
			ret = true;
			overrides.operatingSystemName(osName);
		}

		String osVersion = getString(props, OSVER);
		if (osVersion != null) {
			ret = true;
			overrides.operatingSystemVersion(osVersion);
		}

		ProcessorArchitecture cpuArch = getProcessorArchitecture(props, CPUARCH);
		if (cpuArch != null) {
			ret = true;
			overrides.cpuArchitecture(cpuArch);
		}

		Integer cpuCount = getInteger(props, CPUCOUNT);
		if (cpuCount != null) {
			ret = true;
			overrides.cpuCount(cpuCount);
		}

		ClockSpeed cpuSpeed = getClockSpeed(props, CPUSPEED);
		if (cpuSpeed != null) {
			ret = true;
			overrides.cpuSpeed(cpuSpeed);
		}

		Size physMem = getSize(props, PHYSICALMEM);
		if (physMem != null) {
			ret = true;
			overrides.physicalMemory(physMem);
		}

		Size virtMem = getSize(props, VIRTUALMEM);
		if (virtMem != null) {
			ret = true;
			overrides.virtualMemory(virtMem);
		}

		return ret;
	}

	static private boolean upgrade(NativeQueueConfiguration nativeQueueConf, Properties props) throws IOException
	{
		boolean ret = false;

		final String BASE1 = "edu.virginia.vcgr.genii.container.bes.";
		final String BASE2 = "edu.virginia.vcgr.genii.client.nativeq.";

		final String PROVNAME = BASE1 + "nativeq-provider";
		final String SHAREDDIR = BASE1 + "shared-directory";
		final String TRAPSIGS = BASE2 + "signals-to-trap-for-kill";

		String provName = getString(props, PROVNAME);
		if (provName != null) {
			nativeQueueConf.providerName(provName);
			ret = true;
		}

		String sharedDir = getString(props, SHAREDDIR);
		if (sharedDir != null) {
			nativeQueueConf.sharedDirectory(sharedDir);
			ret = true;
		}

		Collection<UnixSignals> signals = getSignals(props, TRAPSIGS);
		if (signals != null && signals.size() > 0) {
			nativeQueueConf.trapSignals().clear();
			nativeQueueConf.trapSignals().addAll(signals);
			ret = true;
		}

		if (provName != null) {
			if (provName.equals(PBSQueue.PROVIDER_NAME)) {
				PBSQueueConfiguration conf = new PBSQueueConfiguration();
				upgrade(conf, props);
				nativeQueueConf.providerConfiguration(conf);
			} else if (provName.equals(SGEQueue.PROVIDER_NAME)) {
				SGEQueueConfiguration conf = new SGEQueueConfiguration();
				upgrade(conf, props);
				nativeQueueConf.providerConfiguration(conf);
			}
		}

		return ret;
	}

	static private void upgradeScriptBasedConf(CommonScriptBasedQueueConfiguration conf, Properties props, String provName)
	{
		final String BASE = "edu.virginia.vcgr.genii.client.nativeq.";
		final String QNAME_FMT = BASE + "%s.queue-name";
		final String BINDIR_FMT = BASE + "%s.bin-directory";
		final String APPPATH_FMT = BASE + "%s.%s-path";
		final String APPARG_FMT = BASE + "%s.%s.additional-argument.%d";
		final String BASHBIN = BASE + "bash-binary";
		final String SCRIPTNAME = BASE + "submit-script-name";

		String qName = getString(props, String.format(QNAME_FMT, provName));
		if (qName != null)
			conf.queueName(qName);

		String binDir = getString(props, String.format(BINDIR_FMT, provName));
		if (binDir != null)
			conf.binDirectory(binDir);

		conf.qsub(getAppConf(props, APPPATH_FMT, APPARG_FMT, provName, "qsub"));
		conf.qstat(getAppConf(props, APPPATH_FMT, APPARG_FMT, provName, "qstat"));
		conf.qdel(getAppConf(props, APPPATH_FMT, APPARG_FMT, provName, "qdel"));

		conf.bashBinary(props.getProperty(BASHBIN));
		conf.submitScriptName(props.getProperty(SCRIPTNAME));
	}

	static private ExecutableApplicationConfiguration getAppConf(Properties props, String pathFmt, String argFmt,
		String provName, String appName)
	{
		String path = null;
		Collection<String> args = new LinkedList<String>();

		path = props.getProperty(String.format(pathFmt, provName, appName));
		int lcv = 0;
		while (true) {
			String arg = props.getProperty(String.format(argFmt, provName, appName, lcv++));
			if (arg == null)
				break;
			args.add(arg);
		}

		return new ExecutableApplicationConfiguration(path, args);
	}

	static private void upgrade(PBSQueueConfiguration conf, Properties props)
	{
		upgradeScriptBasedConf(conf, props, "pbs");
	}

	static private void upgrade(SGEQueueConfiguration conf, Properties props)
	{
		upgradeScriptBasedConf(conf, props, "sge");
	}

	static boolean upgrade(BESConstructionParameters params, Properties props) throws IOException
	{
		boolean ret = false;

		ResourceOverrides resourceOverrides = params.getResourceOverrides();
		if (upgrade(resourceOverrides, props)) {
			params.setResourceOverrides(resourceOverrides);
			ret = true;
		}

		NativeQueueConfiguration queueConf = params.getNativeQueueConfiguration();
		if (queueConf == null)
			queueConf = new NativeQueueConfiguration();
		if (upgrade(queueConf, props)) {
			params.setNativeQueueConfiguration(queueConf);
			ret = true;
		}

		return ret;
	}
}
