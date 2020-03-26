/**
 * AclType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common.security;

public class AclType  implements java.io.Serializable {
    private edu.virginia.vcgr.genii.common.security.AclEntryListType readAcl;

    private edu.virginia.vcgr.genii.common.security.AclEntryListType writeAcl;

    private edu.virginia.vcgr.genii.common.security.AclEntryListType executeAcl;

    private java.lang.Boolean requireEncryption;  // attribute

    public AclType() {
    }

    public AclType(
           edu.virginia.vcgr.genii.common.security.AclEntryListType readAcl,
           edu.virginia.vcgr.genii.common.security.AclEntryListType writeAcl,
           edu.virginia.vcgr.genii.common.security.AclEntryListType executeAcl,
           java.lang.Boolean requireEncryption) {
           this.readAcl = readAcl;
           this.writeAcl = writeAcl;
           this.executeAcl = executeAcl;
           this.requireEncryption = requireEncryption;
    }


    /**
     * Gets the readAcl value for this AclType.
     * 
     * @return readAcl
     */
    public edu.virginia.vcgr.genii.common.security.AclEntryListType getReadAcl() {
        return readAcl;
    }


    /**
     * Sets the readAcl value for this AclType.
     * 
     * @param readAcl
     */
    public void setReadAcl(edu.virginia.vcgr.genii.common.security.AclEntryListType readAcl) {
        this.readAcl = readAcl;
    }


    /**
     * Gets the writeAcl value for this AclType.
     * 
     * @return writeAcl
     */
    public edu.virginia.vcgr.genii.common.security.AclEntryListType getWriteAcl() {
        return writeAcl;
    }


    /**
     * Sets the writeAcl value for this AclType.
     * 
     * @param writeAcl
     */
    public void setWriteAcl(edu.virginia.vcgr.genii.common.security.AclEntryListType writeAcl) {
        this.writeAcl = writeAcl;
    }


    /**
     * Gets the executeAcl value for this AclType.
     * 
     * @return executeAcl
     */
    public edu.virginia.vcgr.genii.common.security.AclEntryListType getExecuteAcl() {
        return executeAcl;
    }


    /**
     * Sets the executeAcl value for this AclType.
     * 
     * @param executeAcl
     */
    public void setExecuteAcl(edu.virginia.vcgr.genii.common.security.AclEntryListType executeAcl) {
        this.executeAcl = executeAcl;
    }


    /**
     * Gets the requireEncryption value for this AclType.
     * 
     * @return requireEncryption
     */
    public java.lang.Boolean getRequireEncryption() {
        return requireEncryption;
    }


    /**
     * Sets the requireEncryption value for this AclType.
     * 
     * @param requireEncryption
     */
    public void setRequireEncryption(java.lang.Boolean requireEncryption) {
        this.requireEncryption = requireEncryption;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AclType)) return false;
        AclType other = (AclType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.readAcl==null && other.getReadAcl()==null) || 
             (this.readAcl!=null &&
              this.readAcl.equals(other.getReadAcl()))) &&
            ((this.writeAcl==null && other.getWriteAcl()==null) || 
             (this.writeAcl!=null &&
              this.writeAcl.equals(other.getWriteAcl()))) &&
            ((this.executeAcl==null && other.getExecuteAcl()==null) || 
             (this.executeAcl!=null &&
              this.executeAcl.equals(other.getExecuteAcl()))) &&
            ((this.requireEncryption==null && other.getRequireEncryption()==null) || 
             (this.requireEncryption!=null &&
              this.requireEncryption.equals(other.getRequireEncryption())));
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
        if (getReadAcl() != null) {
            _hashCode += getReadAcl().hashCode();
        }
        if (getWriteAcl() != null) {
            _hashCode += getWriteAcl().hashCode();
        }
        if (getExecuteAcl() != null) {
            _hashCode += getExecuteAcl().hashCode();
        }
        if (getRequireEncryption() != null) {
            _hashCode += getRequireEncryption().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AclType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AclType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("requireEncryption");
        attrField.setXmlName(new javax.xml.namespace.QName("", "requireEncryption"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("readAcl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "readAcl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AclEntryListType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("writeAcl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "writeAcl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AclEntryListType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("executeAcl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "executeAcl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AclEntryListType"));
        elemField.setNillable(false);
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
