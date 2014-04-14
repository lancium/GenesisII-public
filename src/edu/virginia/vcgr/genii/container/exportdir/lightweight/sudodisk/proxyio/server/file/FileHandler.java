package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.ReadResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.FSActions;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.StatAttributes;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.StatResponse;

public class FileHandler {

    public static byte delete(String path, Socket socket) {
        
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File file = new File(path);
        if (!file.exists()) {
            DefaultResponse.send(socket,
                    ErrorCode.FNF_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.FNF_CODE));
            return ErrorCode.FNF_CODE;
        }
        
        if(file.delete()) {
            DefaultResponse.send(socket, 
                    ErrorCode.SUCCESS_CODE,
                    null);
            return ErrorCode.SUCCESS_CODE;
        } else {
            DefaultResponse.send(socket, 
                    ErrorCode.DELETE_FAIL_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.DELETE_FAIL_CODE));
            return ErrorCode.DELETE_FAIL_CODE;
        }
    }
    
    public static int write(String path, byte[] buf, 
            long offset, Socket socket) {
        
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
        }
        
        RandomAccessFile raf;
        boolean failed = true;
        try {
            raf = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            //Write file doesn't exist!
            DefaultResponse.send(socket, 
                    ErrorCode.FNF_CODE,
                    e.toString());
            return ErrorCode.FNF_CODE;
        }
        
        try {
            raf.seek(offset);
            raf.write(buf);
            failed = false;
            return ErrorCode.SUCCESS_CODE;
        } catch (IOException e) {
            //couldn't perform the seek/write!
            DefaultResponse.send(socket, 
                    ErrorCode.IO_ERROR_CODE,
                    e.toString());
            return ErrorCode.IO_ERROR_CODE;
        } finally {
            try {
                raf.close();
                if (!failed) {
                    DefaultResponse.send(socket, 
                            ErrorCode.SUCCESS_CODE,
                            null);
                    return ErrorCode.SUCCESS_CODE;
                }
            } catch (IOException e) {
                //Couldn't close file handle!
                DefaultResponse.send(socket, 
                        ErrorCode.IO_ERROR_CODE,
                        e.toString());
                return ErrorCode.IO_ERROR_CODE;
            }
        }
    }
    
    public static int truncAppend(String path, byte[] buf,
            long offset, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        RandomAccessFile raf;
        boolean failed = true;
        try {
            raf = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            //Write file doesn't exist!
            DefaultResponse.send(socket, 
                    ErrorCode.FNF_CODE,
                    e.toString());
            return ErrorCode.FNF_CODE;
        }
        
        try {
            raf.seek(offset);
            raf.write(buf);
            failed = false;
            
            return ErrorCode.SUCCESS_CODE;
        } catch (IOException e) {
            //couldn't perform the seek/write!
            DefaultResponse.send(socket, 
                    ErrorCode.IO_ERROR_CODE,
                    e.toString());
            return ErrorCode.IO_ERROR_CODE;
        } finally {
            try {
                raf.close();
                if (!failed) {
                    DefaultResponse.send(socket, 
                            ErrorCode.SUCCESS_CODE,
                            null);
                    return ErrorCode.SUCCESS_CODE;
                }
                
            } catch (IOException e) {
                //Couldn't close file handle!
                DefaultResponse.send(socket, 
                        ErrorCode.IO_ERROR_CODE,
                        e.toString());
                return ErrorCode.IO_ERROR_CODE;
            }
        }
    }    
    
    
    public static int stat(String path, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        Path file = Paths.get(path);
        
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(file, BasicFileAttributes.class);
        } catch (IOException e) {
            // couldn't retrieve file attributes
            
            DefaultResponse.send(socket,
                    ErrorCode.IO_ERROR_CODE,
                    e.toString());
            return ErrorCode.IO_ERROR_CODE;
        }
        
        if (attr == null) {
            //couldn't retrieve file attributes
            DefaultResponse.send(socket,
                    ErrorCode.ATTR_FETCH_FAIL_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.ATTR_FETCH_FAIL_CODE));
            return ErrorCode.ATTR_FETCH_FAIL_CODE;
        }
        
        PathType type;
        if (attr.isRegularFile()) {
            type = PathType.FILE;
        } else if(attr.isDirectory()) {
            type = PathType.DIRECTORY;
        } else if (attr.isSymbolicLink()) {
            type = PathType.LINK;
        } else {
            type = PathType.OTHER;
        }
        
        StatAttributes attributes = new StatAttributes(
                file.getFileName().toString(),
                attr.lastAccessTime(), attr.lastModifiedTime(),
                attr.size(), type);
        
        
        StatResponse.send(socket, attributes);
        
        return ErrorCode.SUCCESS_CODE;
    
    }
    
    public static int read(String path, long offset, 
            long numBytes, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File file = new File(path);
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            DefaultResponse.send(socket, 
                    ErrorCode.FNF_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.FNF_CODE));
            return ErrorCode.FNF_CODE;
        }
        
        //file exists, unless it has been deleted just after raf check!
        long size = file.length();
        
        if ((size - offset) < 0) {
            //XXX: What to do for read attempt beyond EOF?
            //attempt to read beyond EOF!
            //Simply return
            try {
                raf.close();
            } catch (IOException e) {
                //Unable to close raf
                DefaultResponse.send(socket, 
                        ErrorCode.IO_ERROR_CODE,
                        e.toString());
                return ErrorCode.IO_ERROR_CODE;
            }
            
            //Send 0 byte valid  read-response!
            byte[] buf = new byte[0];
            ReadResponse.send(socket, buf);
            return ErrorCode.SUCCESS_CODE;
        }
        
        if(size < (offset + numBytes)) {
            numBytes = size - offset; 
        }        
        
        //lossy conversion from long to int!
        byte b[] = new byte[(int)numBytes];
        try {
            raf.seek(offset);
            raf.readFully(b);            
        } catch (IOException ioe) {
            //Couldn't seek/read!
            DefaultResponse.send(socket, 
                    ErrorCode.IO_ERROR_CODE,
                    ioe.toString());
            return ErrorCode.IO_ERROR_CODE;
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                // Unable to close file
                DefaultResponse.send(socket, 
                        ErrorCode.IO_ERROR_CODE,
                        e.toString());
                return ErrorCode.IO_ERROR_CODE;
            }
        }
        
        //send the b bytes out!
        ReadResponse.send(socket, b);
        return ErrorCode.SUCCESS_CODE;
    }

    public static int createNewFile(String path,
            Socket socket) {

        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File f = new File (path);        
        if (f.exists()) {
            DefaultResponse.send(socket, 
                    ErrorCode.FILE_ALREADY_EXISTS_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.FILE_ALREADY_EXISTS_CODE));
            return ErrorCode.FILE_ALREADY_EXISTS_CODE;
        }
        
        boolean retVal;
        try {
            retVal = f.createNewFile();
        } catch (IOException e) {
            DefaultResponse.send(socket, 
                    ErrorCode.IO_ERROR_CODE,
                    e.toString());
            return ErrorCode.IO_ERROR_CODE;
        }
        
        if(retVal) {
            //success
            DefaultResponse.send(socket, 
                    ErrorCode.SUCCESS_CODE,
                    null);
            return ErrorCode.SUCCESS_CODE;
        } else {
            DefaultResponse.send(socket, 
                    ErrorCode.CREATE_FAIL_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.CREATE_FAIL_CODE));
            
            return ErrorCode.CREATE_FAIL_CODE;
        }
        
    }
    
    //returns 0 if readable!
    public static int canRead(String path, Socket socket) {
        return FSActions.canRead(path, socket);
    }
    
    //returns 0 if writeable!
    public static int canWrite(String path, Socket socket) {
        return FSActions.canWrite(path, socket);
    }
    
    public static int isExists(String path, Socket socket) {
        return FSActions.isExists(path, socket);
    }
}
