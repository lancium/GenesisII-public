package edu.virginia.vcgr.genii.client.gui.exportdir;

public class ExportCreationInformation {
	private boolean _isLightWeight;
	private String _containerPath;
	private String _localPath;
	private String _rnsPath;

	public ExportCreationInformation(String containerPath, String localPath,
			String rnsPath, boolean isLightWeight) {
		_containerPath = containerPath;
		_localPath = localPath;
		_rnsPath = rnsPath;
		_isLightWeight = isLightWeight;
	}

	public boolean isLightWeight() {
		return _isLightWeight;
	}

	public String getContainerPath() {
		return _containerPath;
	}

	public String getLocalPath() {
		return _localPath;
	}

	public String getRNSPath() {
		return _rnsPath;
	}
}