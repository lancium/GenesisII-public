/**
 * RPCCallerType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common;

public class RPCCallerType  implements java.io.Serializable {
    private java.lang.String rpcid;

    private edu.virginia.vcgr.genii.common.RPCMetadataType metadata;

    public RPCCallerType() {
    }

    public RPCCallerType(
           java.lang.String rpcid,
           edu.virginia.vcgr.genii.common.RPCMetadataType metadata) {
           this.rpcid = rpcid;
           this.metadata = metadata;
    }


    /**
     * Gets the rpcid value for this RPCCallerType.
     * 
     * @return rpcid
     */
    public java.lang.String getRpcid() {
        return rpcid;
    }


    /**
     * Sets the rpcid value for this RPCCallerType.
     * 
     * @param rpcid
     */
    public void setRpcid(java.lang.String rpcid) {
        this.rpcid = rpcid;
    }


    /**
     * Gets the metadata value for this RPCCallerType.
     * 
     * @return metadata
     */
    public edu.virginia.vcgr.genii.common.RPCMetadataType getMetadata() {
        return metadata;
    }


    /**
     * Sets the metadata value for this RPCCallerType.
     * 
     * @param metadata
     */
    public void setMetadata(edu.virginia.vcgr.genii.common.RPCMetadataType metadata) {
        this.metadata = metadata;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RPCCallerType)) return false;
        RPCCallerType other = (RPCCallerType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.rpcid==null && other.getRpcid()==null) || 
             (this.rpcid!=null &&
              this.rpcid.equals(other.getRpcid()))) &&
            ((this.metadata==null && other.getMetadata()==null) || 
             (this.metadata!=null &&
              this.metadata.equals(other.getMetadata())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getRpcid() != null) {
            _hashCode += getRpcid().hashCode();
        }
        if (getMetadata() != null) {
            _hashCode += getMetadata().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RPCCallerType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCCallerType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rpcid");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "rpcid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metadata");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "metadata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCMetadataType"));
        elemField.setNillable(true);
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
