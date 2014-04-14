package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.dir;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.DirListResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.DirListing;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.FSActions;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.StatAttributes;

public class DirHandler {

    /**
     * Function to create a directory at path
     * @param path
     * @return
     */
    public static int mkdir(String path, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File newDir = new File (path);        
        if (newDir.exists()) {
            DefaultResponse.send(socket, 
                    ErrorCode.DIR_ALREADY_EXISTS_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.DIR_ALREADY_EXISTS_CODE));
            return ErrorCode.DIR_ALREADY_EXISTS_CODE;
        }
        
        if (newDir.mkdir()) {
            //success!
            DefaultResponse.send(socket, 
                    ErrorCode.SUCCESS_CODE,
                    null);
            return ErrorCode.SUCCESS_CODE;
        }
        
        DefaultResponse.send(socket, 
                ErrorCode.MKDIR_FAIL_CODE,
                ErrorCode.getErrorMsgFromErrorCode(
                        ErrorCode.MKDIR_FAIL_CODE));
        
        return ErrorCode.MKDIR_FAIL_CODE;
    }
    
    public static int list(String path, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File dir = new File(path);
        if (!dir.exists()) {
            DefaultResponse.send(socket,
                    ErrorCode.FNF_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.FNF_CODE));
            return ErrorCode.FNF_CODE;
        }
        
        File[] files = dir.listFiles();
        
        for(File file: files) {
            System.out.println(file.getName());
        }
        return 0;
    }
        
    public static int listlong(String path, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File dir = new File(path);
        if(!dir.exists()) {
            DefaultResponse.send(socket, 
                    ErrorCode.FNF_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.FNF_CODE));
            return ErrorCode.FNF_CODE;
        }
        
        if (!dir.isDirectory()) {
            DefaultResponse.send(socket,
                    ErrorCode.LL_NOT_DIR_ERROR_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.LL_NOT_DIR_ERROR_CODE));
            return ErrorCode.LL_NOT_DIR_ERROR_CODE;
        }
        
        File[] files = dir.listFiles();
        
        //this can happen when there isn't permission to 
        // list this dir!
        if (files == null) {
            DefaultResponse.send(socket,
                    ErrorCode.LL_FAIL_ERROR_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.LL_FAIL_ERROR_CODE));
            return ErrorCode.LL_FAIL_ERROR_CODE;
        }
        
        ArrayList<StatAttributes> stat_list = 
                new ArrayList<StatAttributes>();
        
        for(File file: files) {
           try {
               StatAttributes sa = getFileAttributes(
                       file.getAbsolutePath());
               if (sa != null) {
                   //If we get no info for a file!
                   //We discard and move on!
                   stat_list.add(sa);
               }
           } catch(Exception e) {
               //If we haven't managed to fetch for a file
               //we continue fetching for the remaining files
               // we don't throw error back to client!
           }
        }
        
        DirListing dsa = new DirListing(stat_list);
        DirListResponse.send(socket, dsa);        
        
        return ErrorCode.SUCCESS_CODE;
    }
    
    
    private static StatAttributes getFileAttributes(String path) 
        throws IOException{
        
        if (path == null) {
            return null;
        }
        
        Path file = Paths.get(path);
        
        BasicFileAttributes attr = null;
        attr = Files.readAttributes(file, BasicFileAttributes.class);
        
        if (attr == null) {
            //couldn't retrieve file attributes
            return null;
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
        
        return attributes;
    }
    
    public static int remove(String path, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File dir = new File(path);
        if (!dir.exists()) {
            DefaultResponse.send(socket, 
                    ErrorCode.FNF_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.FNF_CODE));
            return ErrorCode.FNF_CODE;
        }
        
        if(dir.delete()) {
            DefaultResponse.send(socket, 
                    ErrorCode.SUCCESS_CODE
                    ,null);
            return ErrorCode.SUCCESS_CODE;
        } else {
            DefaultResponse.send(socket, 
                    ErrorCode.DELETE_FAIL_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.DELETE_FAIL_CODE));
            return ErrorCode.DELETE_FAIL_CODE;
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
    
    public static int isDir(String path, Socket socket) {
        if (path == null) {
            DefaultResponse.send(socket, 
                    ErrorCode.BAD_PATH_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.BAD_PATH_CODE));
            return ErrorCode.BAD_PATH_CODE;
        }
        
        File f = new File(path);
        if (f.isDirectory()) {
            DefaultResponse.send(socket, 
                    ErrorCode.SUCCESS_CODE,
                    null);
            return ErrorCode.SUCCESS_CODE;
        } else {
            DefaultResponse.send(socket, 
                    ErrorCode.NOT_DIR_CODE,
                    null);
            return ErrorCode.NOT_DIR_CODE;
        }
    }
    
    public static int isExists(String path, Socket socket) {
        return FSActions.isExists(path, socket);
    }
}