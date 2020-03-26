/**
 * CreateResolverResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class CreateResolverResponseType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType resolution_EPR;

    private org.ws.addressing.EndpointReferenceType resolver_EPR;

    public CreateResolverResponseType() {
    }

    public CreateResolverResponseType(
           org.ws.addressing.EndpointReferenceType resolution_EPR,
           org.ws.addressing.EndpointReferenceType resolver_EPR) {
           this.resolution_EPR = resolution_EPR;
           this.resolver_EPR = resolver_EPR;
    }


    /**
     * Gets the resolution_EPR value for this CreateResolverResponseType.
     * 
     * @return resolution_EPR
     */
    public org.ws.addressing.EndpointReferenceType getResolution_EPR() {
        return resolution_EPR;
    }


    /**
     * Sets the resolution_EPR value for this CreateResolverResponseType.
     * 
     * @param resolution_EPR
     */
    public void setResolution_EPR(org.ws.addressing.EndpointReferenceType resolution_EPR) {
        this.resolution_EPR = resolution_EPR;
    }


    /**
     * Gets the resolver_EPR value for this CreateResolverResponseType.
     * 
     * @return resolver_EPR
     */
    public org.ws.addressing.EndpointReferenceType getResolver_EPR() {
        return resolver_EPR;
    }


    /**
     * Sets the resolver_EPR value for this CreateResolverResponseType.
     * 
     * @param resolver_EPR
     */
    public void setResolver_EPR(org.ws.addressing.EndpointReferenceType resolver_EPR) {
        this.resolver_EPR = resolver_EPR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateResolverResponseType)) return false;
        CreateResolverResponseType other = (CreateResolverResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.resolution_EPR==null && other.getResolution_EPR()==null) || 
             (this.resolution_EPR!=null &&
              this.resolution_EPR.equals(other.getResolution_EPR()))) &&
            ((this.resolver_EPR==null && other.getResolver_EPR()==null) || 
             (this.resolver_EPR!=null &&
              this.resolver_EPR.equals(other.getResolver_EPR())));
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
        if (getResolution_EPR() != null) {
            _hashCode += getResolution_EPR().hashCode();
        }
        if (getResolver_EPR() != null) {
            _hashCode += getResolver_EPR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateResolverResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "CreateResolverResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resolution_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "resolution_EPR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resolver_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "resolver_EPR"));
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
