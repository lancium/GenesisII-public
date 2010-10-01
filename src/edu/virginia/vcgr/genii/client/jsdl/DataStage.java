package edu.virginia.vcgr.genii.client.jsdl;

import javax.xml.bind.annotation.XmlElement;

public class DataStage {

	private String _uri;
	private String _fileName;
	private String _protocol;
	private Boolean _cached;
	



	
	public DataStage(){
		
	}
	
	public DataStage(String uri, String fileName, String protocol, Boolean cached){
		_uri = uri;
		_protocol = protocol;
		_cached = cached;
		_fileName = fileName;
	}

	@XmlElement(name = "fileName", required = true)
	public String get_fileName() {
		return _fileName;
	}

	public void set_fileName(String _fileName) {
		this._fileName = _fileName;
	}
	
	@XmlElement(name = "uri", required = true)
	public String get_uri() {
		return _uri;
	}

	public void set_uri(String _uri) {
		this._uri = _uri;
	}

	@XmlElement(name = "protocol", required = true)
	public String get_protocol() {
		return _protocol;
	}

	public void set_protocol(String _protocol) {
		this._protocol = _protocol;
	}

	@XmlElement(name = "cached", required = true)
	public Boolean get_cached() {
		return _cached;
	}

	public void set_cached(Boolean _cached) {
		this._cached = _cached;
	}
	
}
