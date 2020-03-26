/**
 * InsertResourcePropertiesRequestFailedFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsrf.rp_2;

public class InsertResourcePropertiesRequestFailedFaultType  extends org.oasis_open.wsrf.basefaults.BaseFaultType  implements java.io.Serializable {
    private org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureType resourcePropertyChangeFailure;

    public InsertResourcePropertiesRequestFailedFaultType() {
    }

    public InsertResourcePropertiesRequestFailedFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause,
           org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureType resourcePropertyChangeFailure) {
        super(
            _any,
            timestamp,
            originator,
            errorCode,
            description,
            faultCause);
        this.resourcePropertyChangeFailure = resourcePropertyChangeFailure;
    }


    /**
     * Gets the resourcePropertyChangeFailure value for this InsertResourcePropertiesRequestFailedFaultType.
     * 
     * @return resourcePropertyChangeFailure
     */
    public org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureType getResourcePropertyChangeFailure() {
        return resourcePropertyChangeFailure;
    }


    /**
     * Sets the resourcePropertyChangeFailure value for this InsertResourcePropertiesRequestFailedFaultType.
     * 
     * @param resourcePropertyChangeFailure
     */
    public void setResourcePropertyChangeFailure(org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureType resourcePropertyChangeFailure) {
        this.resourcePropertyChangeFailure = resourcePropertyChangeFailure;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InsertResourcePropertiesRequestFailedFaultType)) return false;
        InsertResourcePropertiesRequestFailedFaultType other = (InsertResourcePropertiesRequestFailedFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.resourcePropertyChangeFailure==null && other.getResourcePropertyChangeFailure()==null) || 
             (this.resourcePropertyChangeFailure!=null &&
              this.resourcePropertyChangeFailure.equals(other.getResourcePropertyChangeFailure())));
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
        if (getResourcePropertyChangeFailure() != null) {
            _hashCode += getResourcePropertyChangeFailure().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InsertResourcePropertiesRequestFailedFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertResourcePropertiesRequestFailedFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resourcePropertyChangeFailure");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourcePropertyChangeFailure"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourcePropertyChangeFailureType"));
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
