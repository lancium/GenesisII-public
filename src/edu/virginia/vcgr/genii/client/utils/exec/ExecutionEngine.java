package edu.virginia.vcgr.genii.client.utils.exec;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class ExecutionEngine
{
	static private Log _logger = LogFactory.getLog(ExecutionEngine.class);

	static private List<String> readOutput(byte[] data) throws IOException
	{
		BufferedReader reader = null;
		String line;
		List<String> result = new LinkedList<String>();

		try {
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

			return result;
		} finally {
			StreamUtils.close(reader);
		}
	}

	static public String formatOutput(List<String> output)
	{
		StringBuilder builder = new StringBuilder();
		boolean seenFirst = false;

		for (String line : output) {
			if (seenFirst)
				builder.append('\n');
			seenFirst = true;
			builder.append(line);
		}

		return builder.toString();
	}

	static public List<String> simpleMultilineExecute(String... command) throws IOException
	{
		SimpleExecutionResults results = execute(command);
		List<String> error = results.getError();

		if (error.size() > 0)
			_logger.warn("Got output on stderr from a simple execute:\n" + formatOutput(error));

		if (results.getExitCode() != 0)
			throw new IOException(String.format("Non-zero exit code (%d) from simple exec.", results.getExitCode()));

		return results.getOutput();
	}

	static public String simpleExecute(String... command) throws IOException
	{
		List<String> output = simpleMultilineExecute(command);
		if (output.size() > 0)
			return output.get(0);

		return "";
	}

	static public SimpleExecutionResults execute(String... command) throws IOException
	{
		int exitCode;
		StreamGatherer stdout = null;
		StreamGatherer stderr = null;
		ProcessBuilder builder = new ProcessBuilder(command);
		Process proc = null;

		try {
			proc = builder.start();
			stdout = new StreamGatherer(proc.getInputStream());
			stderr = new StreamGatherer(proc.getErrorStream());

			while (true) {
				try {
					exitCode = proc.waitFor();
					break;
				} catch (InterruptedException ie) {
					Thread.interrupted();
				}
			}

			return new SimpleExecutionResults(exitCode, readOutput(stdout.getData()), readOutput(stderr.getData()));
		} finally {
			if (proc != null) {
				try {
					proc.destroy();
				} catch (Throwable cause) {
				}
			}

			StreamUtils.close(stdout);
			StreamUtils.close(stderr);
		}
	}
}