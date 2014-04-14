package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;

/**
 * This class contains information on fields required to communicate
 * with an already started fileserver process for a given user.
 * 
 * It contains the nonce: that goes in every request
 * the port: where the fileserver is running
 * the process object itself which can be used to check if the
 * fileserver is actually running (or has it been killed)
 *
 */
public class FileServerID {

    private byte[] _nonce = new byte[Constants.NONCE_SIZE];
    private int _port;
    private Process _fsProcess;
    
    public FileServerID(byte[] nonce, int port, Process p) {
        System.arraycopy(nonce, 0, _nonce, 0, nonce.length);
        _port = port; 
        _fsProcess = p;
    }
    
    public byte[] getNonce () {
        return _nonce;
    }
    
    public int getPort() {
        return _port;
    }
    
    public Process getFSProcess() {
        return _fsProcess;
    }
}
