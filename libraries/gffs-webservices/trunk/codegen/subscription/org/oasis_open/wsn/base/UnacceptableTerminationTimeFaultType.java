/**
 * UnacceptableTerminationTimeFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class UnacceptableTerminationTimeFaultType  extends org.oasis_open.wsrf.basefaults.BaseFaultType  implements java.io.Serializable {
    private java.util.Calendar minimumTime;

    private java.util.Calendar maximumTime;

    public UnacceptableTerminationTimeFaultType() {
    }

    public UnacceptableTerminationTimeFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause,
           java.util.Calendar minimumTime,
           java.util.Calendar maximumTime) {
        super(
            _any,
            timestamp,
            originator,
            errorCode,
            description,
            faultCause);
        this.minimumTime = minimumTime;
        this.maximumTime = maximumTime;
    }


    /**
     * Gets the minimumTime value for this UnacceptableTerminationTimeFaultType.
     * 
     * @return minimumTime
     */
    public java.util.Calendar getMinimumTime() {
        return minimumTime;
    }


    /**
     * Sets the minimumTime value for this UnacceptableTerminationTimeFaultType.
     * 
     * @param minimumTime
     */
    public void setMinimumTime(java.util.Calendar minimumTime) {
        this.minimumTime = minimumTime;
    }


    /**
     * Gets the maximumTime value for this UnacceptableTerminationTimeFaultType.
     * 
     * @return maximumTime
     */
    public java.util.Calendar getMaximumTime() {
        return maximumTime;
    }


    /**
     * Sets the maximumTime value for this UnacceptableTerminationTimeFaultType.
     * 
     * @param maximumTime
     */
    public void setMaximumTime(java.util.Calendar maximumTime) {
        this.maximumTime = maximumTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnacceptableTerminationTimeFaultType)) return false;
        UnacceptableTerminationTimeFaultType other = (UnacceptableTerminationTimeFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.minimumTime==null && other.getMinimumTime()==null) || 
             (this.minimumTime!=null &&
              this.minimumTime.equals(other.getMinimumTime()))) &&
            ((this.maximumTime==null && other.getMaximumTime()==null) || 
             (this.maximumTime!=null &&
              this.maximumTime.equals(other.getMaximumTime())));
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
        if (getMinimumTime() != null) {
            _hashCode += getMinimumTime().hashCode();
        }
        if (getMaximumTime() != null) {
            _hashCode += getMaximumTime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UnacceptableTerminationTimeFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnacceptableTerminationTimeFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("minimumTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "MinimumTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maximumTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "MaximumTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
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


    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
