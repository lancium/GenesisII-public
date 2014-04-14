package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DirListResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.ReadResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.StatResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;


public class FSClientTest {

    public static void main(String[] args) throws Exception{
        
        DefaultResponse retVal;
        DirListResponse dlr;
        String uname = "guest1";
        
        FileServerID fsid = FileServerClient.start(uname);
        if (fsid == null) {
        	System.err.println("Unable to start proxy. Exitting!");
        	System.exit(-1);
        }
        int port = fsid.getPort();
        byte[] nonce = fsid.getNonce();
        
        String dir1 = "/Users/avinash/foo";
        retVal = FileServerClient.mkdir(dir1, nonce, port);
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        String dir2= dir1+ "/foo2";
        //creating a dir within foo
        retVal = FileServerClient.mkdir(dir2, nonce, port);
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        
        //check if dir
        retVal = FileServerClient.isDir(dir2, nonce, port);
        
        retVal = FileServerClient.canRead(dir1, nonce, port, PathType.DIRECTORY);
        retVal = FileServerClient.canRead("/Users/guest1/foo", nonce, port, PathType.DIRECTORY);
        retVal = FileServerClient.canRead("/Users/guest1/file1", nonce, port, PathType.FILE);
        retVal = FileServerClient.canWrite("/Users/guest1/foo", nonce, port, PathType.DIRECTORY);
        retVal = FileServerClient.canWrite("/Users/guest1/file1", nonce, port, PathType.FILE);

        retVal = FileServerClient.exists(dir1, nonce, port, PathType.DIRECTORY);
        retVal = FileServerClient.exists(dir1+"asdas", nonce, port, PathType.DIRECTORY);
        
        //creating file within foo
        String text = "My name is ak3ka";
        String filepath = dir1 + "/file1";
        retVal = FileServerClient.creat(filepath, nonce, port);
        if (retVal.getErrorCode() != 0) {
            System.err.println("Unable to create file!!!");
        }
        
        retVal = FileServerClient.exists(filepath, nonce, port, PathType.FILE);
        retVal = FileServerClient.exists(filepath + "asd", nonce, port, PathType.FILE);
        
        //check if dir
        retVal = FileServerClient.isDir(filepath, nonce, port);
        
        retVal = FileServerClient.write(filepath, 
                text.getBytes(), 0, nonce, port);
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        
        //appending to that file
        String append_text = " in uva";
        retVal = FileServerClient.truncAppend(filepath, append_text.getBytes(), 
                1,nonce, port);
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        
        //reading contents of the file
        ReadResponse rr = FileServerClient.read(filepath, 0, 200, nonce, port);
        if (rr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
        	String contents = new String(rr.getReadBuf());
            System.out.println(contents);
        }
        
        //stat-ing the file
        StatResponse sr = FileServerClient.stat(filepath, nonce, port);
        System.out.println(sr);

        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        
        //removing the file
        retVal = FileServerClient.rm(filepath, nonce, port);
        
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        
        //removing subdir within foo
        retVal = FileServerClient.rmdir(dir2, nonce, port);
        
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();

        //removing directory foo
        retVal = FileServerClient.rmdir(dir1, nonce, port);
        //FileServerClient.listlong(dir1, nonce, port);
        
        dlr = FileServerClient.listlong(dir1, nonce, port);
        dlr.disp();
        
        FileServerClient.stop(uname);
        
    }

}