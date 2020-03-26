/**
 * BrokerWithForwardingPortCreateRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.notification.factory;

public class BrokerWithForwardingPortCreateRequestType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType notificationForwardingPort;

    private long notificationBrokerLifetime;

    public BrokerWithForwardingPortCreateRequestType() {
    }

    public BrokerWithForwardingPortCreateRequestType(
           org.ws.addressing.EndpointReferenceType notificationForwardingPort,
           long notificationBrokerLifetime) {
           this.notificationForwardingPort = notificationForwardingPort;
           this.notificationBrokerLifetime = notificationBrokerLifetime;
    }


    /**
     * Gets the notificationForwardingPort value for this BrokerWithForwardingPortCreateRequestType.
     * 
     * @return notificationForwardingPort
     */
    public org.ws.addressing.EndpointReferenceType getNotificationForwardingPort() {
        return notificationForwardingPort;
    }


    /**
     * Sets the notificationForwardingPort value for this BrokerWithForwardingPortCreateRequestType.
     * 
     * @param notificationForwardingPort
     */
    public void setNotificationForwardingPort(org.ws.addressing.EndpointReferenceType notificationForwardingPort) {
        this.notificationForwardingPort = notificationForwardingPort;
    }


    /**
     * Gets the notificationBrokerLifetime value for this BrokerWithForwardingPortCreateRequestType.
     * 
     * @return notificationBrokerLifetime
     */
    public long getNotificationBrokerLifetime() {
        return notificationBrokerLifetime;
    }


    /**
     * Sets the notificationBrokerLifetime value for this BrokerWithForwardingPortCreateRequestType.
     * 
     * @param notificationBrokerLifetime
     */
    public void setNotificationBrokerLifetime(long notificationBrokerLifetime) {
        this.notificationBrokerLifetime = notificationBrokerLifetime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BrokerWithForwardingPortCreateRequestType)) return false;
        BrokerWithForwardingPortCreateRequestType other = (BrokerWithForwardingPortCreateRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.notificationForwardingPort==null && other.getNotificationForwardingPort()==null) || 
             (this.notificationForwardingPort!=null &&
              this.notificationForwardingPort.equals(other.getNotificationForwardingPort()))) &&
            this.notificationBrokerLifetime == other.getNotificationBrokerLifetime();
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
        if (getNotificationForwardingPort() != null) {
            _hashCode += getNotificationForwardingPort().hashCode();
        }
        _hashCode += new Long(getNotificationBrokerLifetime()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BrokerWithForwardingPortCreateRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "BrokerWithForwardingPortCreateRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notificationForwardingPort");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "notificationForwardingPort"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notificationBrokerLifetime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "notificationBrokerLifetime"));
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
