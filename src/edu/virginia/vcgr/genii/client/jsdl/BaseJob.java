package edu.virginia.vcgr.genii.client.jsdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.cmd.tools.CopyTool;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.PosixLikeApplicationUnderstanding;

@XmlRootElement(name = "BaseJob")
public class BaseJob {


	private ArrayList<DataStage> _stagein = new ArrayList<DataStage>();
	private ArrayList<DataStage> _stageout = new ArrayList<DataStage>();

	private String _exec;


	public void addStageIn(DataStage tStage){
		_stagein.add(tStage);
	}

	@XmlElement(name = "exec", required = true)
	public String get_exec() {
		return _exec;
	}

	public void set_exec(String _exec) {
		this._exec = _exec;
	}

	@XmlElement(name = "StageIn", required = true)
	public ArrayList<DataStage> get_stagein() {
		return _stagein;
	}

	public void set_stagein(ArrayList<DataStage> _stagein) {
		this._stagein = _stagein;
	}

	@XmlElement(name = "StageOut", required = true)
	public ArrayList<DataStage> get_stageout() {
		return _stageout;
	}

	public void set_stageout(ArrayList<DataStage> _stageout) {
		this._stageout = _stageout;
	}

	public void addStageOut(DataStage tStage){
		_stageout.add(tStage);
	}
	public BaseJob(){

	}

	public void stageIn(String workingDir) throws FileNotFoundException, IOException{
		for (DataStage tStage : _stagein){
			CopyTool.copy(tStage.get_uri(), "local:" + workingDir + "/" + tStage.get_fileName());
		}
	}

	public void stageOut(String workingDir) throws FileNotFoundException, IOException{
		for (DataStage tStage : _stageout){
			CopyTool.copy("local:" + workingDir + "/" + tStage.get_fileName(), tStage.get_uri());
		}
	}


	public void generateJobScript(OutputStream tStream, File workingDir, File resourceUsage, PosixLikeApplicationUnderstanding app){

		try
		{

			PrintStream ps = new PrintStream(tStream);

			//Generate Header
			ps.format("#!%s\n\n", "/bin/bash");

			//Generate App Body
			ps.format("cd \"%s\"\n", workingDir.getAbsolutePath());


			ResourceOverrides overrides = new ResourceOverrides();

			ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(
					workingDir, overrides.operatingSystemName(),
					overrides.cpuArchitecture());

			boolean first = true;

			for (String element : wrapper.formCommandLine(
					null, //app.getEnvironment()
					workingDir,
					getRedirect(app.getStdinRedirect(), workingDir), 
					getRedirect(app.getStdoutRedirect(), workingDir),
					getRedirect(app.getStderrRedirect(), workingDir),
					resourceUsage,
					"./" + _exec, app.getArguments().toArray(
							new String[app.getArguments().size()])))
			{
				if (!first)
					ps.format(" ");
				first = false;
				ps.format("\"%s\"", element);
			}
			ps.println();
			ps.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
	}

	private File getRedirect(FilesystemRelativePath tPath, File workingDir){
		if (tPath == null)
			return null;
		return new File(workingDir.toString() + "/" + tPath.getString());
	}
}



