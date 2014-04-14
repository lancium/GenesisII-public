package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;

public class DefaultResponse {

    /**
     * Sends the response code alone!
     * @param socket
     * @param code
     */
    public static void send(Socket socket, byte code,
            String errorMsg) {
        if (socket == null) {
            return;
        }
        
        if (errorMsg == null && code != ErrorCode.SUCCESS_CODE) {
            errorMsg = ErrorCode.getErrorMsgFromErrorCode(code);
        }
        
        byte[] msgBytes;
        if (errorMsg != null) {
            msgBytes = errorMsg.getBytes();
        } else {
            msgBytes = new byte[0];
        }
        
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    socket.getOutputStream());
            
            //1 for status code + 4 for length of msg if any
            byte[] response = new byte[5];
            
            response[0] = code;
            
            ByteBuffer bb_buflen = ByteBuffer.allocate(4);
            //the initial order of a byte buffer is always BIG_ENDIAN.        
            bb_buflen.putInt(msgBytes.length);
            byte[] b_buflen = bb_buflen.array();
            System.arraycopy(b_buflen, 0, response, 1, b_buflen.length);
            
            bos.write(response);
            bos.write(msgBytes);
            bos.close();
        } catch (IOException e) {
            System.err.println("Error writing out to client");
            e.printStackTrace();
        }
        
    }
    
}
