/**
 * CountRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.resolver;

public class CountRequestType  implements java.io.Serializable {
    private org.apache.axis.types.URI endpointIdentifier;

    public CountRequestType() {
    }

    public CountRequestType(
           org.apache.axis.types.URI endpointIdentifier) {
           this.endpointIdentifier = endpointIdentifier;
    }


    /**
     * Gets the endpointIdentifier value for this CountRequestType.
     * 
     * @return endpointIdentifier
     */
    public org.apache.axis.types.URI getEndpointIdentifier() {
        return endpointIdentifier;
    }


    /**
     * Sets the endpointIdentifier value for this CountRequestType.
     * 
     * @param endpointIdentifier
     */
    public void setEndpointIdentifier(org.apache.axis.types.URI endpointIdentifier) {
        this.endpointIdentifier = endpointIdentifier;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CountRequestType)) return false;
        CountRequestType other = (CountRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.endpointIdentifier==null && other.getEndpointIdentifier()==null) || 
             (this.endpointIdentifier!=null &&
              this.endpointIdentifier.equals(other.getEndpointIdentifier())));
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
        if (getEndpointIdentifier() != null) {
            _hashCode += getEndpointIdentifier().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CountRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/genii-resolver", "CountRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endpointIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "EndpointIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
