/**
 * InvalidFilterFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class InvalidFilterFaultType  extends org.oasis_open.wsrf.basefaults.BaseFaultType  implements java.io.Serializable {
    private javax.xml.namespace.QName[] unknownFilter;

    public InvalidFilterFaultType() {
    }

    public InvalidFilterFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause,
           javax.xml.namespace.QName[] unknownFilter) {
        super(
            _any,
            timestamp,
            originator,
            errorCode,
            description,
            faultCause);
        this.unknownFilter = unknownFilter;
    }


    /**
     * Gets the unknownFilter value for this InvalidFilterFaultType.
     * 
     * @return unknownFilter
     */
    public javax.xml.namespace.QName[] getUnknownFilter() {
        return unknownFilter;
    }


    /**
     * Sets the unknownFilter value for this InvalidFilterFaultType.
     * 
     * @param unknownFilter
     */
    public void setUnknownFilter(javax.xml.namespace.QName[] unknownFilter) {
        this.unknownFilter = unknownFilter;
    }

    public javax.xml.namespace.QName getUnknownFilter(int i) {
        return this.unknownFilter[i];
    }

    public void setUnknownFilter(int i, javax.xml.namespace.QName _value) {
        this.unknownFilter[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InvalidFilterFaultType)) return false;
        InvalidFilterFaultType other = (InvalidFilterFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.unknownFilter==null && other.getUnknownFilter()==null) || 
             (this.unknownFilter!=null &&
              java.util.Arrays.equals(this.unknownFilter, other.getUnknownFilter())));
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
        if (getUnknownFilter() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnknownFilter());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnknownFilter(), i);
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
        new org.apache.axis.description.TypeDesc(InvalidFilterFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidFilterFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unknownFilter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnknownFilter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
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
