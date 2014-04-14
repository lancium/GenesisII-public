package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

public class ReadResponse {

    private int error_code;
    private byte[] read_buff;
    private String _errorMsg;
    
    public ReadResponse(int error_code, byte[] read_buff) {
        this.error_code = error_code;
        this.read_buff = read_buff;
    }
    
   public ReadResponse(int error_code, String errorMsg) {
       _errorMsg = errorMsg;
       this.error_code = error_code;
   }
   
   public String getErrorMsg() {
       return _errorMsg;
   }
   
    public int getErrorCode() {
        return error_code;
    }
    
    public byte[] getReadBuf() {
        return read_buff;
    }
}
