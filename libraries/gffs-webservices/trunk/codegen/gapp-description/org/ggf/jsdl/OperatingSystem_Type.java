/**
 * OperatingSystem_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class OperatingSystem_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.OperatingSystemType_Type operatingSystemType;

    private java.lang.String operatingSystemVersion;

    private java.lang.String description;

    private org.apache.axis.message.MessageElement [] _any;

    public OperatingSystem_Type() {
    }

    public OperatingSystem_Type(
           org.ggf.jsdl.OperatingSystemType_Type operatingSystemType,
           java.lang.String operatingSystemVersion,
           java.lang.String description,
           org.apache.axis.message.MessageElement [] _any) {
           this.operatingSystemType = operatingSystemType;
           this.operatingSystemVersion = operatingSystemVersion;
           this.description = description;
           this._any = _any;
    }


    /**
     * Gets the operatingSystemType value for this OperatingSystem_Type.
     * 
     * @return operatingSystemType
     */
    public org.ggf.jsdl.OperatingSystemType_Type getOperatingSystemType() {
        return operatingSystemType;
    }


    /**
     * Sets the operatingSystemType value for this OperatingSystem_Type.
     * 
     * @param operatingSystemType
     */
    public void setOperatingSystemType(org.ggf.jsdl.OperatingSystemType_Type operatingSystemType) {
        this.operatingSystemType = operatingSystemType;
    }


    /**
     * Gets the operatingSystemVersion value for this OperatingSystem_Type.
     * 
     * @return operatingSystemVersion
     */
    public java.lang.String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }


    /**
     * Sets the operatingSystemVersion value for this OperatingSystem_Type.
     * 
     * @param operatingSystemVersion
     */
    public void setOperatingSystemVersion(java.lang.String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }


    /**
     * Gets the description value for this OperatingSystem_Type.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this OperatingSystem_Type.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the _any value for this OperatingSystem_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this OperatingSystem_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OperatingSystem_Type)) return false;
        OperatingSystem_Type other = (OperatingSystem_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.operatingSystemType==null && other.getOperatingSystemType()==null) || 
             (this.operatingSystemType!=null &&
              this.operatingSystemType.equals(other.getOperatingSystemType()))) &&
            ((this.operatingSystemVersion==null && other.getOperatingSystemVersion()==null) || 
             (this.operatingSystemVersion!=null &&
              this.operatingSystemVersion.equals(other.getOperatingSystemVersion()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any())));
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
        if (getOperatingSystemType() != null) {
            _hashCode += getOperatingSystemType().hashCode();
        }
        if (getOperatingSystemVersion() != null) {
            _hashCode += getOperatingSystemVersion().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (get_any() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(get_any());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(get_any(), i);
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
        new org.apache.axis.description.TypeDesc(OperatingSystem_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystem_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatingSystemType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystemType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystemType_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatingSystemVersion");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystemVersion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
