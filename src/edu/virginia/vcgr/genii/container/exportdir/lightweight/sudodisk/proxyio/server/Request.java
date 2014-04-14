package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server;

import java.net.Socket;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.dir.DirHandler;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.file.FileHandler;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;

public class Request {
    
    private PathType _type;
    private String _cmd;
    private String _path;
    private int _writeBufLen;
    private byte[] _writeBuf;
    
    //This is the offset into writeBuf where the next byte of contents from
    // client gets written to. Eventually, at the end it should be equal to
    // _writeBufLen.
    private int _numBytesRead;
    
    private long[] _args = new long[Constants.MAX_NUM_ARGS];
    
    public Request(PathType type, String cmd, String path,
            int buflen, long[] args) {
        _type = type;
        _cmd = cmd;
        _path = path;
        _writeBufLen = buflen;
        _writeBuf = new byte[buflen];
        _numBytesRead = 0;

        System.arraycopy(args, 0, _args, 0, args.length);
        
        /*PSFileWriter.writeToFile("Creating request with type : " + 
                type + " cmd = " + cmd + " path = " + path + 
                " write buf len = " + _writeBufLen) ;
        for (int ii=0; ii< args.length; ii++) {
            PSFileWriter.writeToFile("arg["+ii+"] : " + args[ii]);
        }*/

    }
    
    public int getWriteBufSize() {
        return _writeBufLen;
    }
    
    public boolean isWrite() {
        return (_cmd.equals("w") || (_cmd.equals("a"))); 
    }
        
    
    public byte[] getWriteBuf() {
        return _writeBuf;
    }


    public void writeToBuf(byte[] src, int offset, int numBytes) {
        System.arraycopy(src, offset, _writeBuf, _numBytesRead, numBytes);
        _numBytesRead += numBytes;
    }

    public int getNumBytesRead() {
        return _numBytesRead;
    }
    
    public PathType getPathType() {
        return _type;
    }
    
    public String getPath() {
        return _path;
    }

    //Calls the corresponding fs method which in turns sends the response
    public void handle(Socket socket) {
        if (socket == null) {
            //PSFileWriter.writeToFile("Proxy Server Warning: No socket to write back to!");
            return;
        }
        
        if (_type == PathType.FILE) {
            handleFileOp(socket);
        } else if (_type == PathType.DIRECTORY){
            handleDirOp(socket);
        }
    }


    /**
     * Invokes the appropriate file operation
     * @param socket The socket attached with the request on which the
     * response needs to be sent on.
     */
    private void handleFileOp(Socket socket) {
        if (_cmd == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.INVALID_CMD_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.INVALID_CMD_CODE));
        }
        
        //PSFileWriter.writeToFile("Proxy Server: Handling file operation");
        
        if(_cmd.equalsIgnoreCase(Constants.FILE_READ_CMD)) {
            FileHandler.read(_path, _args[0], _args[1], socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_WRITE_CMD)) {
            FileHandler.write(_path, _writeBuf, _args[0], socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_TRUNCAPPEND_CMD)) {
            FileHandler.truncAppend(_path, _writeBuf, _args[0], socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_STAT_CMD)) {
            FileHandler.stat(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_DELETE_CMD)) {
            FileHandler.delete(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_CREAT_CMD)) {
            FileHandler.createNewFile(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_CAN_READ_CMD)) {
            FileHandler.canRead(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_CAN_WRITE_CMD)) {
            FileHandler.canWrite(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.FILE_DOESEXIST_CMD)) {
            FileHandler.isExists(_path, socket);
        } else {
            DefaultResponse.send(socket, 
                    ErrorCode.INVALID_CMD_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.INVALID_CMD_CODE));
        }
    }

    /**
     * Invokes the appropriate dir operation
     * @param socket The socket attached with the request on which the
     * response needs to be sent on.
     */
    private void handleDirOp(Socket socket) {
        if (_cmd == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.INVALID_CMD_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.INVALID_CMD_CODE));
        }
        
        //PSFileWriter.writeToFile("Proxy Server: Handling dir operation");

        if (_cmd.equalsIgnoreCase(Constants.DIR_CREATE_CMD)) {
            DirHandler.mkdir(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.DIR_DELETE_CMD)) {
            DirHandler.remove(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.DIR_LISTLONG_CMD)) {
            DirHandler.listlong(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.DIR_CAN_READ_CMD)) {
            DirHandler.canRead(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.DIR_CAN_WRITE_CMD)) {
            DirHandler.canWrite(_path, socket);
        } else if (_cmd.equalsIgnoreCase(Constants.DIR_ISDIR_CMD)) {
            DirHandler.isDir(_path, socket);
        } else if(_cmd.equalsIgnoreCase(Constants.DIR_DOESEXIST_CMD)) { 
            DirHandler.isExists(_path, socket);
        } else {
            DefaultResponse.send(socket, 
                    ErrorCode.INVALID_CMD_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.INVALID_CMD_CODE));
        }
        
    }
}
