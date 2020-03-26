/**
 * SetTerminationTime.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsrf.rl_2;

public class SetTerminationTime  implements java.io.Serializable {
    private java.util.Calendar requestedTerminationTime;

    private org.apache.axis.types.Duration requestedLifetimeDuration;

    public SetTerminationTime() {
    }

    public SetTerminationTime(
           java.util.Calendar requestedTerminationTime,
           org.apache.axis.types.Duration requestedLifetimeDuration) {
           this.requestedTerminationTime = requestedTerminationTime;
           this.requestedLifetimeDuration = requestedLifetimeDuration;
    }


    /**
     * Gets the requestedTerminationTime value for this SetTerminationTime.
     * 
     * @return requestedTerminationTime
     */
    public java.util.Calendar getRequestedTerminationTime() {
        return requestedTerminationTime;
    }


    /**
     * Sets the requestedTerminationTime value for this SetTerminationTime.
     * 
     * @param requestedTerminationTime
     */
    public void setRequestedTerminationTime(java.util.Calendar requestedTerminationTime) {
        this.requestedTerminationTime = requestedTerminationTime;
    }


    /**
     * Gets the requestedLifetimeDuration value for this SetTerminationTime.
     * 
     * @return requestedLifetimeDuration
     */
    public org.apache.axis.types.Duration getRequestedLifetimeDuration() {
        return requestedLifetimeDuration;
    }


    /**
     * Sets the requestedLifetimeDuration value for this SetTerminationTime.
     * 
     * @param requestedLifetimeDuration
     */
    public void setRequestedLifetimeDuration(org.apache.axis.types.Duration requestedLifetimeDuration) {
        this.requestedLifetimeDuration = requestedLifetimeDuration;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SetTerminationTime)) return false;
        SetTerminationTime other = (SetTerminationTime) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.requestedTerminationTime==null && other.getRequestedTerminationTime()==null) || 
             (this.requestedTerminationTime!=null &&
              this.requestedTerminationTime.equals(other.getRequestedTerminationTime()))) &&
            ((this.requestedLifetimeDuration==null && other.getRequestedLifetimeDuration()==null) || 
             (this.requestedLifetimeDuration!=null &&
              this.requestedLifetimeDuration.equals(other.getRequestedLifetimeDuration())));
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
        if (getRequestedTerminationTime() != null) {
            _hashCode += getRequestedTerminationTime().hashCode();
        }
        if (getRequestedLifetimeDuration() != null) {
            _hashCode += getRequestedLifetimeDuration().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SetTerminationTime.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">SetTerminationTime"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestedTerminationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "RequestedTerminationTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestedLifetimeDuration");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "RequestedLifetimeDuration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "duration"));
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
