/**
 * ResolveFailedWithReferralFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ogf.schemas.naming._2006._08.naming;

public class ResolveFailedWithReferralFaultType  extends org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType[] referenceResolver;

    private org.apache.axis.types.URI[] endpointIdentifier;

    public ResolveFailedWithReferralFaultType() {
    }

    public ResolveFailedWithReferralFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause,
           org.ws.addressing.EndpointReferenceType[] referenceResolver,
           org.apache.axis.types.URI[] endpointIdentifier) {
        super(
            _any,
            timestamp,
            originator,
            errorCode,
            description,
            faultCause);
        this.referenceResolver = referenceResolver;
        this.endpointIdentifier = endpointIdentifier;
    }


    /**
     * Gets the referenceResolver value for this ResolveFailedWithReferralFaultType.
     * 
     * @return referenceResolver
     */
    public org.ws.addressing.EndpointReferenceType[] getReferenceResolver() {
        return referenceResolver;
    }


    /**
     * Sets the referenceResolver value for this ResolveFailedWithReferralFaultType.
     * 
     * @param referenceResolver
     */
    public void setReferenceResolver(org.ws.addressing.EndpointReferenceType[] referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public org.ws.addressing.EndpointReferenceType getReferenceResolver(int i) {
        return this.referenceResolver[i];
    }

    public void setReferenceResolver(int i, org.ws.addressing.EndpointReferenceType _value) {
        this.referenceResolver[i] = _value;
    }


    /**
     * Gets the endpointIdentifier value for this ResolveFailedWithReferralFaultType.
     * 
     * @return endpointIdentifier
     */
    public org.apache.axis.types.URI[] getEndpointIdentifier() {
        return endpointIdentifier;
    }


    /**
     * Sets the endpointIdentifier value for this ResolveFailedWithReferralFaultType.
     * 
     * @param endpointIdentifier
     */
    public void setEndpointIdentifier(org.apache.axis.types.URI[] endpointIdentifier) {
        this.endpointIdentifier = endpointIdentifier;
    }

    public org.apache.axis.types.URI getEndpointIdentifier(int i) {
        return this.endpointIdentifier[i];
    }

    public void setEndpointIdentifier(int i, org.apache.axis.types.URI _value) {
        this.endpointIdentifier[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResolveFailedWithReferralFaultType)) return false;
        ResolveFailedWithReferralFaultType other = (ResolveFailedWithReferralFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.referenceResolver==null && other.getReferenceResolver()==null) || 
             (this.referenceResolver!=null &&
              java.util.Arrays.equals(this.referenceResolver, other.getReferenceResolver()))) &&
            ((this.endpointIdentifier==null && other.getEndpointIdentifier()==null) || 
             (this.endpointIdentifier!=null &&
              java.util.Arrays.equals(this.endpointIdentifier, other.getEndpointIdentifier())));
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
        if (getReferenceResolver() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getReferenceResolver());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getReferenceResolver(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getEndpointIdentifier() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEndpointIdentifier());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEndpointIdentifier(), i);
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
        new org.apache.axis.description.TypeDesc(ResolveFailedWithReferralFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedWithReferralFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("referenceResolver");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ReferenceResolver"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endpointIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "EndpointIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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


    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
