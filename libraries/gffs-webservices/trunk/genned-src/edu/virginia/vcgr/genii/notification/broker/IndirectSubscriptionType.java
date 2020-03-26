/**
 * IndirectSubscriptionType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.notification.broker;

public class IndirectSubscriptionType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType publisher;

    private long duration;

    public IndirectSubscriptionType() {
    }

    public IndirectSubscriptionType(
           org.ws.addressing.EndpointReferenceType publisher,
           long duration) {
           this.publisher = publisher;
           this.duration = duration;
    }


    /**
     * Gets the publisher value for this IndirectSubscriptionType.
     * 
     * @return publisher
     */
    public org.ws.addressing.EndpointReferenceType getPublisher() {
        return publisher;
    }


    /**
     * Sets the publisher value for this IndirectSubscriptionType.
     * 
     * @param publisher
     */
    public void setPublisher(org.ws.addressing.EndpointReferenceType publisher) {
        this.publisher = publisher;
    }


    /**
     * Gets the duration value for this IndirectSubscriptionType.
     * 
     * @return duration
     */
    public long getDuration() {
        return duration;
    }


    /**
     * Sets the duration value for this IndirectSubscriptionType.
     * 
     * @param duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IndirectSubscriptionType)) return false;
        IndirectSubscriptionType other = (IndirectSubscriptionType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.publisher==null && other.getPublisher()==null) || 
             (this.publisher!=null &&
              this.publisher.equals(other.getPublisher()))) &&
            this.duration == other.getDuration();
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
        if (getPublisher() != null) {
            _hashCode += getPublisher().hashCode();
        }
        _hashCode += new Long(getDuration()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(IndirectSubscriptionType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker", "IndirectSubscriptionType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publisher");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker", "publisher"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("duration");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker", "duration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
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
