package edu.virginia.vcgr.genii.gjt.data.fs.scratch;

import java.awt.Window;

import javax.swing.JOptionPane;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemFactory;

public class ScratchFilesystemFactory implements FilesystemFactory {
	@Override
	public Filesystem instantiate(Window owner) {
		String answer = JOptionPane.showInputDialog(owner,
				"Unique identifier for this scratch filesystem.",
				"Scratch Filesystem Configuration",
				JOptionPane.QUESTION_MESSAGE);
		if (answer == null)
			return null;

		return new ScratchFilesystem(answer);
	}
}