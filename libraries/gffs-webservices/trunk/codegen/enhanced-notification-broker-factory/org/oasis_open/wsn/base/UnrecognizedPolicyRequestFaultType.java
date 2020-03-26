/**
 * UnrecognizedPolicyRequestFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class UnrecognizedPolicyRequestFaultType  extends org.oasis_open.wsrf.basefaults.BaseFaultType  implements java.io.Serializable {
    private javax.xml.namespace.QName[] unrecognizedPolicy;

    public UnrecognizedPolicyRequestFaultType() {
    }

    public UnrecognizedPolicyRequestFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause,
           javax.xml.namespace.QName[] unrecognizedPolicy) {
        super(
            _any,
            timestamp,
            originator,
            errorCode,
            description,
            faultCause);
        this.unrecognizedPolicy = unrecognizedPolicy;
    }


    /**
     * Gets the unrecognizedPolicy value for this UnrecognizedPolicyRequestFaultType.
     * 
     * @return unrecognizedPolicy
     */
    public javax.xml.namespace.QName[] getUnrecognizedPolicy() {
        return unrecognizedPolicy;
    }


    /**
     * Sets the unrecognizedPolicy value for this UnrecognizedPolicyRequestFaultType.
     * 
     * @param unrecognizedPolicy
     */
    public void setUnrecognizedPolicy(javax.xml.namespace.QName[] unrecognizedPolicy) {
        this.unrecognizedPolicy = unrecognizedPolicy;
    }

    public javax.xml.namespace.QName getUnrecognizedPolicy(int i) {
        return this.unrecognizedPolicy[i];
    }

    public void setUnrecognizedPolicy(int i, javax.xml.namespace.QName _value) {
        this.unrecognizedPolicy[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnrecognizedPolicyRequestFaultType)) return false;
        UnrecognizedPolicyRequestFaultType other = (UnrecognizedPolicyRequestFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.unrecognizedPolicy==null && other.getUnrecognizedPolicy()==null) || 
             (this.unrecognizedPolicy!=null &&
              java.util.Arrays.equals(this.unrecognizedPolicy, other.getUnrecognizedPolicy())));
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
        if (getUnrecognizedPolicy() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnrecognizedPolicy());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnrecognizedPolicy(), i);
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
        new org.apache.axis.description.TypeDesc(UnrecognizedPolicyRequestFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnrecognizedPolicyRequestFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unrecognizedPolicy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnrecognizedPolicy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
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
