package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.mapping;

import java.util.HashMap;
import java.util.Map;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.FileServerID;

/**
 * This class helps in mapping a username to a fileserver
 */
public class Mapper {

    static private Map<String, FileServerID> _unameToPortMapping =
            new HashMap<String,FileServerID>();
    
    /**
     * Given a username, it returns the FileServerID stored for that
     * user if any. If yes, the returned FileServerID contains the nonce 
     * that needs to go in every request, the port where the proxy server is
     * running and the Process object which can be used to check if the proxy
     * server (child process) is still running
     * @param uname The user for whom a FileServerID mapping is needed, so that
     * file system operations can be performed on behalf of that user
     * @return
     */
    static public FileServerID getClientMapping(String uname) {
        if (uname == null) {
            return null;
        }
        
        FileServerID port = _unameToPortMapping.get(uname);
        return port;
    }
    
    /**
     * Used for storing the FileServerID mapping for a given user, so that
     * subsequent file system calls for that user can be proxied to the right
     * process. The FileServerID has the nonce which goes in every request, the
     * port number where the fileserver is running and the FileServer process object
     * itself 
     * @param uname The user for whom a mapping is to be stored
     * @param id The FileServerID for that user
     */
    static public void setClientMapping(String uname, 
            FileServerID id) {
        if (uname == null || id == null) {
            return;
        }
        
        _unameToPortMapping.put(uname, id);
    }
        
}
