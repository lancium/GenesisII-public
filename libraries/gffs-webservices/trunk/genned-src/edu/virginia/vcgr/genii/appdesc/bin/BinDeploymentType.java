/**
 * BinDeploymentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc.bin;

public class BinDeploymentType  extends edu.virginia.vcgr.genii.appdesc.CommonDeploymentDescriptionType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] binary;

    private edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] sharedLibrary;

    private edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType[] staticFile;

    public BinDeploymentType() {
    }

    public BinDeploymentType(
           java.lang.String relativeCwd,
           java.lang.String binaryName,
           org.apache.axis.message.MessageElement [] _any,
           edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] binary,
           edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] sharedLibrary,
           edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType[] staticFile) {
        super(
            relativeCwd,
            binaryName,
            _any);
        this.binary = binary;
        this.sharedLibrary = sharedLibrary;
        this.staticFile = staticFile;
    }


    /**
     * Gets the binary value for this BinDeploymentType.
     * 
     * @return binary
     */
    public edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] getBinary() {
        return binary;
    }


    /**
     * Sets the binary value for this BinDeploymentType.
     * 
     * @param binary
     */
    public void setBinary(edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] binary) {
        this.binary = binary;
    }

    public edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType getBinary(int i) {
        return this.binary[i];
    }

    public void setBinary(int i, edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType _value) {
        this.binary[i] = _value;
    }


    /**
     * Gets the sharedLibrary value for this BinDeploymentType.
     * 
     * @return sharedLibrary
     */
    public edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] getSharedLibrary() {
        return sharedLibrary;
    }


    /**
     * Sets the sharedLibrary value for this BinDeploymentType.
     * 
     * @param sharedLibrary
     */
    public void setSharedLibrary(edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType[] sharedLibrary) {
        this.sharedLibrary = sharedLibrary;
    }

    public edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType getSharedLibrary(int i) {
        return this.sharedLibrary[i];
    }

    public void setSharedLibrary(int i, edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType _value) {
        this.sharedLibrary[i] = _value;
    }


    /**
     * Gets the staticFile value for this BinDeploymentType.
     * 
     * @return staticFile
     */
    public edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType[] getStaticFile() {
        return staticFile;
    }


    /**
     * Sets the staticFile value for this BinDeploymentType.
     * 
     * @param staticFile
     */
    public void setStaticFile(edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType[] staticFile) {
        this.staticFile = staticFile;
    }

    public edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType getStaticFile(int i) {
        return this.staticFile[i];
    }

    public void setStaticFile(int i, edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType _value) {
        this.staticFile[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BinDeploymentType)) return false;
        BinDeploymentType other = (BinDeploymentType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.binary==null && other.getBinary()==null) || 
             (this.binary!=null &&
              java.util.Arrays.equals(this.binary, other.getBinary()))) &&
            ((this.sharedLibrary==null && other.getSharedLibrary()==null) || 
             (this.sharedLibrary!=null &&
              java.util.Arrays.equals(this.sharedLibrary, other.getSharedLibrary()))) &&
            ((this.staticFile==null && other.getStaticFile()==null) || 
             (this.staticFile!=null &&
              java.util.Arrays.equals(this.staticFile, other.getStaticFile())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getBinary() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBinary());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBinary(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getSharedLibrary() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSharedLibrary());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSharedLibrary(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getStaticFile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getStaticFile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getStaticFile(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BinDeploymentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "BinDeploymentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("binary");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "binary"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "NamedSourceType"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sharedLibrary");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "shared-library"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "NamedSourceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("staticFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "static-file"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "RelativeNamedSourceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
