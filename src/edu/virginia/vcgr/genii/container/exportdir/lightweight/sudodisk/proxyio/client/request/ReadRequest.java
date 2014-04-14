package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.Conversions;

public class ReadRequest extends DefaultRequest{

    public static void send(Socket clientSocket, String path, long offset,
            long numBytes, byte[] nonce) throws UnknownHostException, IOException{

        BufferedOutputStream bos = null;

        bos = new BufferedOutputStream(
                clientSocket.getOutputStream());


        //1K buffer containing request!
        byte[] request = new byte[1024];
        int w_offset = 0;

        w_offset = Commons.formPrefix(request, Constants.FILE_OPN_BYTE,
                Constants.FILE_READ_CMD_BYTE, path, nonce);

        //filling args!
        ByteBuffer bb_arg1= ByteBuffer.allocate(8);
        long arg1 = offset;
        bb_arg1.putLong(arg1);
        byte[] b_arg1 = bb_arg1.array();
        System.arraycopy(b_arg1, 0, request, w_offset, b_arg1.length);
        w_offset += b_arg1.length;

        ByteBuffer bb_arg2= ByteBuffer.allocate(8);
        long arg2 = numBytes;
        bb_arg2.putLong(arg2);
        byte[] b_arg2 = bb_arg2.array();
        System.arraycopy(b_arg2, 0, request, w_offset, b_arg2.length);
        w_offset += b_arg2.length;

        bos.write(request, 0, w_offset);
        bos.flush();

    }

    public static ReadResponse recv(Socket clientSocket,
            int resp_size) throws IOException{
    
        BufferedInputStream bis = null;

        bis = new BufferedInputStream(
                clientSocket.getInputStream());
        
        //status code + sizeof(read buffer)
        byte[] response_pfix = new byte[5];
        int bytes_read = bis.read(response_pfix, 0, response_pfix.length);
        if (bytes_read != response_pfix.length ) {
            if (bytes_read == 0) {
                return new ReadResponse(-1, Constants.NO_RESPONSE_MSG);
            }
            return new ReadResponse(-1, Constants.SMALL_RESPONSE_ERROR);
        }

        //Error!
        int errorCode = Conversions.getIntFromBytes( 
                    (byte)0x00, (byte)0x00, (byte)0x00,
                    response_pfix[0]);

        boolean successResponse = true;
        if (errorCode != 0) {
            successResponse = false; 
        }
        
        int read_buf_size = Conversions.getIntFromBytes(response_pfix[1], 
                response_pfix[2], response_pfix[3], response_pfix[4]);
        
        if (read_buf_size > resp_size) {
            //err! we have a smaller buff to recv
            // than what the server is sending
            return new ReadResponse(-1, "Server is sending a larger response "
                    + "than it should");
        }

        byte[] contents = new byte[512* 1024];
        byte[] read_buff = new byte[read_buf_size];
        
        int readOffset = 0;
        while(true) {
            if (readOffset == read_buf_size) {
                if (successResponse) {
                    return new ReadResponse(0, read_buff);
                } else {
                   return new ReadResponse(errorCode,
                           new String(read_buff)); 
                }
            }
            
            bytes_read = bis.read(contents, 0, contents.length);
            if (bytes_read == -1) {
                if (successResponse) {
                    //XXX: Server could 've sent a smaller response!
                    return new ReadResponse(0, read_buff);
                } else {
                    return new ReadResponse(-1, 
                            Constants.SMALL_RESPONSE_ERROR);
                }
                
            }
            
            //Server sending response larger than it should -
            // shouldn't happen actually!
            if (readOffset + bytes_read > read_buf_size) {
                return new ReadResponse(-1, 
                        Constants.LARGE_RESPONSE_ERROR);
            }
            
            System.arraycopy(contents, 0, read_buff, 
                    readOffset, bytes_read);            
            readOffset += bytes_read;

        }
        
    }
}