package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.test;


import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.FileServerClient;

public class FileStatTest {

    public static void main(String[] args) throws Exception {

        int port = 57594;
        String s_nonce = "123456781234567\n";
        byte[] nonce = s_nonce.getBytes();
        
        /*byte[] contents = new String("avinash").getBytes();
        DefaultResponse dr = FileServerClient.write(filepath, 
                contents, 0, nonce, port);
        System.out.println(dr.getErrorCode());
        byte[] tappendcontents = new String(" is the first alphabet").getBytes();
        dr = FileServerClient.truncAppend(filepath, tappendcontents, 1,
                nonce, port);
        System.out.println(dr.getErrorCode());
        */
        
        DefaultResponse dr =
                FileServerClient.canWrite("/Users/guest1/foo/", nonce, port, PathType.DIRECTORY);
        System.out.println(dr.getErrorCode());
    }

}
