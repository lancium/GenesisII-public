/**
 * RegisterPublisherResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsn.br_2;

public class RegisterPublisherResponse  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType publisherRegistrationReference;

    private org.ws.addressing.EndpointReferenceType consumerReference;

    public RegisterPublisherResponse() {
    }

    public RegisterPublisherResponse(
           org.ws.addressing.EndpointReferenceType publisherRegistrationReference,
           org.ws.addressing.EndpointReferenceType consumerReference) {
           this.publisherRegistrationReference = publisherRegistrationReference;
           this.consumerReference = consumerReference;
    }


    /**
     * Gets the publisherRegistrationReference value for this RegisterPublisherResponse.
     * 
     * @return publisherRegistrationReference
     */
    public org.ws.addressing.EndpointReferenceType getPublisherRegistrationReference() {
        return publisherRegistrationReference;
    }


    /**
     * Sets the publisherRegistrationReference value for this RegisterPublisherResponse.
     * 
     * @param publisherRegistrationReference
     */
    public void setPublisherRegistrationReference(org.ws.addressing.EndpointReferenceType publisherRegistrationReference) {
        this.publisherRegistrationReference = publisherRegistrationReference;
    }


    /**
     * Gets the consumerReference value for this RegisterPublisherResponse.
     * 
     * @return consumerReference
     */
    public org.ws.addressing.EndpointReferenceType getConsumerReference() {
        return consumerReference;
    }


    /**
     * Sets the consumerReference value for this RegisterPublisherResponse.
     * 
     * @param consumerReference
     */
    public void setConsumerReference(org.ws.addressing.EndpointReferenceType consumerReference) {
        this.consumerReference = consumerReference;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RegisterPublisherResponse)) return false;
        RegisterPublisherResponse other = (RegisterPublisherResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.publisherRegistrationReference==null && other.getPublisherRegistrationReference()==null) || 
             (this.publisherRegistrationReference!=null &&
              this.publisherRegistrationReference.equals(other.getPublisherRegistrationReference()))) &&
            ((this.consumerReference==null && other.getConsumerReference()==null) || 
             (this.consumerReference!=null &&
              this.consumerReference.equals(other.getConsumerReference())));
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
        if (getPublisherRegistrationReference() != null) {
            _hashCode += getPublisherRegistrationReference().hashCode();
        }
        if (getConsumerReference() != null) {
            _hashCode += getConsumerReference().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RegisterPublisherResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", ">RegisterPublisherResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publisherRegistrationReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "PublisherRegistrationReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "ConsumerReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
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
