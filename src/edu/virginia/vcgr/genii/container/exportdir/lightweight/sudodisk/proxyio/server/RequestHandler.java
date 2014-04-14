package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.Conversions;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;

public class RequestHandler implements Runnable{
    private Socket socket;
    private byte[] nonce = new byte[Constants.NONCE_SIZE];
    
    RequestHandler(Socket sock, byte[] nonce) {
        this.socket = sock;
        System.arraycopy(nonce, 0, this.nonce, 0, nonce.length);
    }    

    @Override
    public void run() {
        BufferedInputStream bin = null;
        try {
        	socket.setSoTimeout(Constants.READ_TIMEOUT_VAL);
            bin = new BufferedInputStream(socket.getInputStream());
                        
            //8K is default buffered read size in java.
            //we are being conservative and taking 0.5M          
            byte[] contents = new byte[512* 1024]; 
            
            int bytes_read = bin.read(contents, 0, contents.length);
            if (bytes_read == -1) {
                DefaultResponse.send(socket, 
                        ErrorCode.NW_READ_ERROR_CODE,
                        ErrorCode.getErrorMsgFromErrorCode(
                                ErrorCode.NW_READ_ERROR_CODE));
                return;
            }
            
            Request request = unmarshallStream(contents, bytes_read, bin);
            if (request == null) {
                DefaultResponse.send(socket, 
                        ErrorCode.INVALID_REQ_ERROR_CODE,
                        ErrorCode.getErrorMsgFromErrorCode(
                                ErrorCode.INVALID_REQ_ERROR_CODE));
                //PSFileWriter.writeToFile("Bad request!");
                return;
            } 
            
            request.handle(socket);
            
            return;            
        } catch (IOException e) {
        	DefaultResponse.send(socket, 
                    ErrorCode.NW_READ_ERROR_CODE,
                    ErrorCode.getErrorMsgFromErrorCode(
                            ErrorCode.NW_READ_ERROR_CODE));
            //e.printStackTrace();
        } finally {
            try {
                bin.close();
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
            
        }
                        
    }

    private Request unmarshallStream(byte[] contents, int resp_size,
            BufferedInputStream bin) {
        
        /**
         * [16byte nonce]|[1byte f/d]|[1byte cmd]|[4byte pathname len]|
         * [n bytes pathname]|[r/w req arg(s) each 8 bytes]
         *  [4 byte write buflen for writes]|
         * [write buf]
         */
        
        int offset = 0;
        RequestPrefix prefix = parsePrefix(contents, offset, resp_size);        
        if(prefix == null) {
            return null;
        }
        
        offset += RequestPrefix.getSize();
                
        //parse path!
        int path_length = prefix.getPathLength();
        String path = parsePath(contents, offset, resp_size, path_length);
        if (path == null) {
            return null;
        }        
        offset += path_length;
        
        /*Next parse the args if any!*/
        long[] args = new long[Constants.MAX_NUM_ARGS];
        boolean success = parseArgs(contents, 
                offset, resp_size, prefix.isReadReq(),
                prefix.isWriteReq() || prefix.isAppendReq(),
                args);
        
        //we don't have args to satisfy request!
        if (!success) {
            return null;
        }
        
        if (prefix.isReadReq()) {
            offset += Constants.READ_ARGS_SIZE; //2 longs
        } else if (prefix.isWriteReq() || prefix.isAppendReq()) {
            offset += Constants.WRITE_ARGS_SIZE; //1 long
        }
        
        int buflen = 0;
        if (!(prefix.isWriteReq() || prefix.isAppendReq())) {
            Request request = new Request(prefix.getPathType(),
                    prefix.getCmd(), path, buflen, args);
            return request;
        }
        
        //4 bytes for buflen
        if (resp_size < (offset + 4)) {
            return null;
        }

        try {
            buflen = Conversions.getIntFromBytes(contents[offset++],
                    contents[offset++], contents[offset++],
                    contents[offset++]);
        } catch(Exception e) {
            return null;
        }

        if(buflen > Constants.MAX_WRITE_BUF_SIZE) {
            return null;
        }
        
        Request request = new Request(prefix.getPathType(),
                prefix.getCmd(), path, buflen, args);
        
        //Read remaining of contents-buffer!
        request.writeToBuf(contents, offset, resp_size-offset);

        if(request.getNumBytesRead() != buflen) {
            //more bytes to read!
            if (!continueReading(request, contents, bin)) {
                return null;
            }           
        }            
        
        //System.out.println("num bytes read = " + request.getNumBytesRead());
        //System.out.println(new String(request.getWriteBuf()));
        
        return request;
    }

    

   private boolean parseArgs(byte[] contents, int offset, int resp_size,
            boolean isReadReq, boolean isWriteReq, long[] args) {
        
       try {
           if (isWriteReq) {
               //8 bytes for long
               if (resp_size < offset + Constants.WRITE_ARGS_SIZE) {
                   return false;
               }
               long w_offset = Conversions.getLongFromBytes(
                       contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++]);
               args[0] = w_offset;
           } else if (isReadReq) {
               //16 bytes = 2 longs!
               if (resp_size < offset + Constants.READ_ARGS_SIZE) {
                   return false;
               }
               long r_offset = Conversions.getLongFromBytes(
                       contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++]);
               
               long r_numBytes = Conversions.getLongFromBytes(
                       contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++], contents[offset++],
                       contents[offset++]);
               
               args[0] = r_offset;
               args[1] = r_numBytes;
           }            
       } catch (Exception e) {
           return false;
       }
       
       return true;
    }

    private String parsePath(byte[] contents, int offset, int resp_size,
            int path_length) {
        if (resp_size < (offset + path_length)) {
            return null;
        }
        
        byte[] b_path = new byte[path_length];
        System.arraycopy(contents, offset, b_path, 0, path_length);
        String path = new String(b_path);
        
        return path;
    }

    private RequestPrefix parsePrefix(byte[] contents, int offset,
            int resp_size) {

       int prefix_size = RequestPrefix.getSize();
       
       if (contents == null || resp_size < prefix_size) {
           return null;
       }
       
       //parse nonce!
       try {
           
           byte[] recvd_nonce = new byte[Constants.NONCE_SIZE];
            
           System.arraycopy(contents, offset, recvd_nonce,
                   0, recvd_nonce.length);
           
           if (!isEqual(recvd_nonce, nonce)) {
               //We can't ascertain src discarding!
               return null;
           }
           
           offset += Constants.NONCE_SIZE;
           
       } catch(Exception e) {
           return null;
       }
               
       //parse f/d
       byte type = contents[offset++];
       PathType pathType;
       if (type == 0x00) {
           pathType = PathType.FILE;
       } else if (type == 0x11) {
           pathType = PathType.DIRECTORY;
       } else {
           //how I miss gotos in java :-( !
           return null;
       }

       //parse cmd!
       byte cmd_type = contents[offset++];
       String cmd = null;

       if (pathType == PathType.FILE) {
           switch(cmd_type) {
               case Constants.FILE_READ_CMD_BYTE:
                   cmd = Constants.FILE_READ_CMD;
                   break;
               case Constants.FILE_WRITE_CMD_BYTE:
                   cmd = Constants.FILE_WRITE_CMD;
                   break;
               case Constants.FILE_TRUNCAPPEND_CMD_BYTE:
                   cmd = Constants.FILE_TRUNCAPPEND_CMD;
                   break;
               case Constants.FILE_STAT_CMD_BYTE:
                   cmd = Constants.FILE_STAT_CMD;
                   break;
               case Constants.FILE_DELETE_CMD_BYTE:
                   cmd = Constants.FILE_DELETE_CMD;
                   break;
               case Constants.FILE_CREATE_CMD_BYTE:
                   cmd = Constants.FILE_CREAT_CMD;
                   break;
               case Constants.FILE_CAN_READ_CMD_BYTE:
                   cmd = Constants.FILE_CAN_READ_CMD;
                   break;
               case Constants.FILE_CAN_WRITE_CMD_BYTE:
                   cmd = Constants.FILE_CAN_WRITE_CMD;
                   break;
               case Constants.FILE_DOESEXIST_CMD_BYTE:
                   cmd = Constants.FILE_DOESEXIST_CMD;
           }
       } else if (pathType == PathType.DIRECTORY) {
           switch(cmd_type) {
               case Constants.DIR_CREATE_CMD_BYTE:
                   cmd = Constants.DIR_CREATE_CMD;
                   break;
               case Constants.DIR_DELETE_CMD_BYTE:
                   cmd = Constants.DIR_DELETE_CMD;
                   break;
               case Constants.DIR_LISTLONG_CMD_BYTE:
                   cmd = Constants.DIR_LISTLONG_CMD;
                   break;
               case Constants.DIR_CAN_READ_CMD_BYTE:
                   cmd = Constants.DIR_CAN_READ_CMD;
                   break;
               case Constants.DIR_CAN_WRITE_CMD_BYTE:
                   cmd = Constants.DIR_CAN_WRITE_CMD;
                   break;
               case Constants.DIR_ISDIR_CMD_BYTE:
                   cmd = Constants.DIR_ISDIR_CMD;
                   break;
               case Constants.DIR_DOESEXIST_CMD_BYTE:
                   cmd = Constants.DIR_DOESEXIST_CMD;
           }
       }
       if (cmd == null) {
           return null;
       }
       
       //parse length of path
       int path_length = 0;
       try {
           path_length = Conversions.getIntFromBytes(
                   contents[offset++],
                   contents[offset++], contents[offset++],
                   contents[offset++]);
           
       } catch(Exception e) {
           return null;
       }
       if (resp_size < (offset + path_length)) {
           return null;
       } 
       
       return new RequestPrefix(nonce, pathType, cmd, path_length);
    }

    private boolean isEqual(byte[] recvd_nonce, byte[] nonce) {
        
        if (recvd_nonce == null && nonce == null) {
            return true;
        }
        
        if (recvd_nonce == null || nonce == null) {
            return false;
        }
        
        if (recvd_nonce.length != nonce.length) {
            return false;
        }
        
        int len = recvd_nonce.length;
        
        for (int lcv=0; lcv<len; lcv++) {
            if (recvd_nonce[lcv] != nonce[lcv]) {
                return false;
            }
        }
        
        return true;
    }

    private boolean continueReading(Request request, byte[] contents,
            BufferedInputStream bin) {
        
        int numBytesRead;
        int totalReadSize;
        
        while (true) {
            try {
                //num of bytes read so far
                numBytesRead = request.getNumBytesRead();
                
                //total num of bytes we need to read
                totalReadSize = request.getWriteBufSize();
                
                if(numBytesRead >= totalReadSize) {
                    //it should actually be only ==
                    break;
                }
                
                int bytes_read = bin.read(contents, 0, contents.length);
                
                if (bytes_read == -1) {
                    //client has lied about size - but since it's
                    // less than what we can hold - so we 'll satisfy request!
                    return true;
                }
                
                if ((bytes_read + numBytesRead) > totalReadSize) {
                    //client sending more bytes than we can hold
                    // we aren't going to handle request
                    return false;
                }
                
                request.writeToBuf(contents, 0, bytes_read);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        
        return true;
        
    }

}
