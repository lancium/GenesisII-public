package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

/**
 * Associated with default recv.
 * Error code indicates error code of the given FS operation
 * and error msg indicates the error msg on why the given FS
 * operation wasn't successful 
 */
public class DefaultResponse {

    private int _errorCode;
    private String _errorMsg;
    
    public DefaultResponse(int ec, String msg) {
        _errorCode = ec;
        _errorMsg = msg;
    }
    
    public DefaultResponse(int ec, byte[] msgBuf) {
        _errorCode = ec;
        _errorMsg = new String(msgBuf);
    }

    public DefaultResponse(int ec) {
        _errorCode = ec;
        _errorMsg = null;
    }

    public int getErrorCode() {
        return _errorCode;
    }

    public String getErrorMsg() {
        return _errorMsg;
    }
    
}
