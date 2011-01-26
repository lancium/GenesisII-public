package edu.virginia.vcgr.genii.cloud;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class CloudConfiguration implements Serializable{

	

	static final long serialVersionUID = 0L;
	static final public String NS =
		"http://vcgr.cs.virginia.edu/cloudbes";
	
	@XmlAttribute(name = "username", required = false)
	private String _username;
	
	@XmlAttribute(name = "password", required = false)
	private String _password;
	
	@XmlAttribute(name = "authFile", required = false)
	private String _authFile;
	
	@XmlAttribute(name = "maxResources", required = false)
	private int _maxResources = 10;
	
	@XmlAttribute(name = "workPerVM", required = false)
	private int _workPerVM = 1;
	
	@XmlAttribute(name = "publicKey", required = false)
	private String _publicKey;
	
	@XmlAttribute(name = "secretKey", required = false)
	private String  _secretKey;
	
	@XmlAttribute(name = "endPoint", required = false)
	private String _endPoint;
	
	@XmlAttribute(name = "port", required = false)
	private int _port;
	
	@XmlAttribute(name = "imageID", required = false)
	private String _imageID;
	
	@XmlAttribute(name = "imageSize", required = false)
	private String _imageSize;
	
	@XmlAttribute(name = "eucalyptus", required = false)
	private boolean _eucalyptus;
	
	@XmlAttribute(name = "setupScript", required = false)
	private String _setupScript;
	
	@XmlAttribute(name = "setupArchive", required = false)
	private String _setupArchive;
	
	@XmlAttribute(name = "remoteSetupDir", required = false)
	private String _remoteSetupDir;
	
	@XmlAttribute(name = "remoteScratchDir", required = false)
	private String _remoteScratchDir;
	
	@XmlAttribute(name = "keyPair", required = false)
	private String _keyPair;
	
	@XmlAttribute(name = "localScratchDir", required = false)
	private String _localScratchDir;
	
	@XmlAttribute(name = "remoteClientDir", required = false)
	private String _remoteClientDir;
	
	@XmlAttribute(name = "type", required = false)
	private String _type;
	
	@XmlAttribute(name = "description", required = false)
	private String _description;
	
	
	public String getRemoteScratchDir() {
		return _remoteScratchDir;
	}

	public void setRemoteScratchDir(String remoteScratchDir) {
		_remoteScratchDir = remoteScratchDir;
	}

	public String getType() {
		return _type;
	}

	public void setType(String _type) {
		this._type = _type;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String _description) {
		this._description = _description;
	}
	
	public String getLocalScratchDir() {
		return _localScratchDir;
	}

	public void setLocalScratchDir(String _localScratchDir) {
		this._localScratchDir = _localScratchDir;
	}

	public String getRemoteClientDir() {
		return _remoteClientDir;
	}

	public void setRemoteClientDir(String _remoteClientDir) {
		this._remoteClientDir = _remoteClientDir;
	}
	
	public String getKeyPair() {
		return _keyPair;
	}

	public void setKeyPair(String keypair) {
		this._keyPair = keypair;
	}
	
	public String getImageID() {
		return _imageID;
	}

	public void setImageID(String _imageID) {
		this._imageID = _imageID;
	}

	public String getImageSize() {
		return _imageSize;
	}

	public void setImageSize(String _imageSize) {
		this._imageSize = _imageSize;
	}

	public boolean isEucalyptus() {
		return _eucalyptus;
	}

	public void setEucalyptus(boolean _eucalyptus) {
		this._eucalyptus = _eucalyptus;
	}

	public String getSetupScript() {
		return _setupScript;
	}

	public void setSetupScript(String _setupScript) {
		this._setupScript = _setupScript;
	}

	public String getSetupArchive() {
		return _setupArchive;
	}

	public void setSetupArchive(String _setupArchive) {
		this._setupArchive = _setupArchive;
	}

	public String getRemoteSetupDir() {
		return _remoteSetupDir;
	}

	public void setRemoteSetupDir(String _remoteSetupDir) {
		this._remoteSetupDir = _remoteSetupDir;
	}
	
	public String getUsername() {
		return _username;
	}

	public void setUsername(String _username) {
		this._username = _username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String _password) {
		this._password = _password;
	}

	public String getAuthFile() {
		return _authFile;
	}

	public void setAuthFile(String _authFile) {
		this._authFile = _authFile;
	}

	public int getMaxResources() {
		return _maxResources;
	}

	public void setMaxResources(int _maxResources) {
		this._maxResources = _maxResources;
	}

	public int getWorkPerVM() {
		return _workPerVM;
	}

	public void setWorkPerVM(int _workPerVM) {
		//this._workPerVM = _workPerVM;
		//Currently do not support multiple jobs per vm, 
		//Ignore config file and force to be 1
		this._workPerVM = 1;
	}

	public String getPublicKey() {
		return _publicKey;
	}

	public void setPublicKey(String _publicKey) {
		this._publicKey = _publicKey;
	}

	public String getSecretKey() {
		return _secretKey;
	}

	public void setSecretKey(String _secretKey) {
		this._secretKey = _secretKey;
	}

	public String getEndPoint() {
		return _endPoint;
	}

	public void setEndPoint(String _endPoint) {
		this._endPoint = _endPoint;
	}

	public int getPort() {
		return _port;
	}

	public void setPort(int _port) {
		this._port = _port;
	}
	
	
}
