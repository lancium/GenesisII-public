/**
 * RNSPathElementType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.context;

public class RNSPathElementType  implements java.io.Serializable {
    private java.lang.String nameFromParent;

    private org.ws.addressing.EndpointReferenceType endpoint;

    public RNSPathElementType() {
    }

    public RNSPathElementType(
           java.lang.String nameFromParent,
           org.ws.addressing.EndpointReferenceType endpoint) {
           this.nameFromParent = nameFromParent;
           this.endpoint = endpoint;
    }


    /**
     * Gets the nameFromParent value for this RNSPathElementType.
     * 
     * @return nameFromParent
     */
    public java.lang.String getNameFromParent() {
        return nameFromParent;
    }


    /**
     * Sets the nameFromParent value for this RNSPathElementType.
     * 
     * @param nameFromParent
     */
    public void setNameFromParent(java.lang.String nameFromParent) {
        this.nameFromParent = nameFromParent;
    }


    /**
     * Gets the endpoint value for this RNSPathElementType.
     * 
     * @return endpoint
     */
    public org.ws.addressing.EndpointReferenceType getEndpoint() {
        return endpoint;
    }


    /**
     * Sets the endpoint value for this RNSPathElementType.
     * 
     * @param endpoint
     */
    public void setEndpoint(org.ws.addressing.EndpointReferenceType endpoint) {
        this.endpoint = endpoint;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RNSPathElementType)) return false;
        RNSPathElementType other = (RNSPathElementType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.nameFromParent==null && other.getNameFromParent()==null) || 
             (this.nameFromParent!=null &&
              this.nameFromParent.equals(other.getNameFromParent()))) &&
            ((this.endpoint==null && other.getEndpoint()==null) || 
             (this.endpoint!=null &&
              this.endpoint.equals(other.getEndpoint())));
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
        if (getNameFromParent() != null) {
            _hashCode += getNameFromParent().hashCode();
        }
        if (getEndpoint() != null) {
            _hashCode += getEndpoint().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RNSPathElementType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "RNSPathElementType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nameFromParent");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "name-from-parent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endpoint");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "endpoint"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
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
