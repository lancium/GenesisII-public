package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class DirListing implements Serializable{

    private static final long serialVersionUID = 1L;
    private ArrayList<StatAttributes> statList = 
            new ArrayList<StatAttributes>();
    
    public DirListing(ArrayList<StatAttributes> statList) {
        this.statList = statList;
    }
    
    public ArrayList<StatAttributes> getDirListing() {
        return statList;
    }
    
    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] serializedBuf = null;
        try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(this);
            serializedBuf = bos.toByteArray();
            return serializedBuf;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public static DirListing deserialize(byte[] response) {
        if (response == null) {
            return null;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(response);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            DirListing obj = (DirListing)in.readObject();
            return obj;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
        }

    }
    
}
