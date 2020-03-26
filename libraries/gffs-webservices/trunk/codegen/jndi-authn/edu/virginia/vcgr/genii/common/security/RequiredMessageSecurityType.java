/**
 * RequiredMessageSecurityType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common.security;

public class RequiredMessageSecurityType  implements java.io.Serializable {
    private java.lang.String authzModule;

    private edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityTypeMin min;  // attribute

    public RequiredMessageSecurityType() {
    }

    public RequiredMessageSecurityType(
           java.lang.String authzModule,
           edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityTypeMin min) {
           this.authzModule = authzModule;
           this.min = min;
    }


    /**
     * Gets the authzModule value for this RequiredMessageSecurityType.
     * 
     * @return authzModule
     */
    public java.lang.String getAuthzModule() {
        return authzModule;
    }


    /**
     * Sets the authzModule value for this RequiredMessageSecurityType.
     * 
     * @param authzModule
     */
    public void setAuthzModule(java.lang.String authzModule) {
        this.authzModule = authzModule;
    }


    /**
     * Gets the min value for this RequiredMessageSecurityType.
     * 
     * @return min
     */
    public edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityTypeMin getMin() {
        return min;
    }


    /**
     * Sets the min value for this RequiredMessageSecurityType.
     * 
     * @param min
     */
    public void setMin(edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityTypeMin min) {
        this.min = min;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RequiredMessageSecurityType)) return false;
        RequiredMessageSecurityType other = (RequiredMessageSecurityType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.authzModule==null && other.getAuthzModule()==null) || 
             (this.authzModule!=null &&
              this.authzModule.equals(other.getAuthzModule()))) &&
            ((this.min==null && other.getMin()==null) || 
             (this.min!=null &&
              this.min.equals(other.getMin())));
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
        if (getAuthzModule() != null) {
            _hashCode += getAuthzModule().hashCode();
        }
        if (getMin() != null) {
            _hashCode += getMin().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RequiredMessageSecurityType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "RequiredMessageSecurityType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("min");
        attrField.setXmlName(new javax.xml.namespace.QName("", "min"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", ">RequiredMessageSecurityType>min"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("authzModule");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "authz-module"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
