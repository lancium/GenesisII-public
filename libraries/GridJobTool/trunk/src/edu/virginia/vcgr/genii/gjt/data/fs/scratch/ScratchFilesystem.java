package edu.virginia.vcgr.genii.gjt.data.fs.scratch;

import java.awt.Window;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.fs.AbstractFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.jsdl.FileSystem;
import edu.virginia.vcgr.jsdl.FileSystemType;
import edu.virginia.vcgr.jsdl.GenesisIIConstants;

public class ScratchFilesystem extends AbstractFilesystem implements
		Externalizable {
	static public final String COMMON_NAME = "Job Scratch";

	static public final String JSDL_NAME = "SCRATCH";
	static public final String DESCRIPTION = "A scratch filesystem where large files can persist between runs.";

	static public final boolean CAN_EDIT = true;

	@XmlAttribute(name = "unique-identifier", required = true)
	private String _uniqueIdentifier;

	public ScratchFilesystem() {
		super(FilesystemType.Scratch);
	}

	@Override
	protected boolean editImpl(Window owner) {
		String answer = (String) JOptionPane.showInputDialog(owner,
				"Unique identifier for this scratch filesystem.",
				"Scratch Filesystem Configuration",
				JOptionPane.QUESTION_MESSAGE, null, null, _uniqueIdentifier);
		if (answer != null)
			_uniqueIdentifier = answer;

		return _uniqueIdentifier != null;
	}

	ScratchFilesystem(String uniqueIdentifier) {
		super(FilesystemType.Scratch);

		_uniqueIdentifier = uniqueIdentifier;
	}

	@Override
	public String toString() {
		return String.format("%s:  %s", filesystemType().toString(),
				_uniqueIdentifier);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ScratchFilesystem) {
			return ((ScratchFilesystem) other)._uniqueIdentifier
					.equals(_uniqueIdentifier);
		}

		return false;
	}

	@Override
	public Object clone() {
		return new ScratchFilesystem(_uniqueIdentifier);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		_uniqueIdentifier = in.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(_uniqueIdentifier);
	}

	@Override
	public FileSystem toJSDLFilesystem() {
		FileSystem ret = new FileSystem("SCRATCH", FileSystemType.spool);
		ret.anyAttributes().put(GenesisIIConstants.SCRATCH_UNIQUE_ID_ATTRIBUTE,
				_uniqueIdentifier);

		return ret;

	}
}