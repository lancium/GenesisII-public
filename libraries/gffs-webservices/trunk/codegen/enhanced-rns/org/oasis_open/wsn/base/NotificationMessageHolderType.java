/**
 * NotificationMessageHolderType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class NotificationMessageHolderType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType subscriptionReference;

    private org.ws.addressing.EndpointReferenceType producerReference;

    private org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage message;

    private org.apache.axis.message.MessageElement [] _any;

    public NotificationMessageHolderType() {
    }

    public NotificationMessageHolderType(
           org.ws.addressing.EndpointReferenceType subscriptionReference,
           org.ws.addressing.EndpointReferenceType producerReference,
           org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage message,
           org.apache.axis.message.MessageElement [] _any) {
           this.subscriptionReference = subscriptionReference;
           this.producerReference = producerReference;
           this.message = message;
           this._any = _any;
    }


    /**
     * Gets the subscriptionReference value for this NotificationMessageHolderType.
     * 
     * @return subscriptionReference
     */
    public org.ws.addressing.EndpointReferenceType getSubscriptionReference() {
        return subscriptionReference;
    }


    /**
     * Sets the subscriptionReference value for this NotificationMessageHolderType.
     * 
     * @param subscriptionReference
     */
    public void setSubscriptionReference(org.ws.addressing.EndpointReferenceType subscriptionReference) {
        this.subscriptionReference = subscriptionReference;
    }


    /**
     * Gets the producerReference value for this NotificationMessageHolderType.
     * 
     * @return producerReference
     */
    public org.ws.addressing.EndpointReferenceType getProducerReference() {
        return producerReference;
    }


    /**
     * Sets the producerReference value for this NotificationMessageHolderType.
     * 
     * @param producerReference
     */
    public void setProducerReference(org.ws.addressing.EndpointReferenceType producerReference) {
        this.producerReference = producerReference;
    }


    /**
     * Gets the message value for this NotificationMessageHolderType.
     * 
     * @return message
     */
    public org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage getMessage() {
        return message;
    }


    /**
     * Sets the message value for this NotificationMessageHolderType.
     * 
     * @param message
     */
    public void setMessage(org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage message) {
        this.message = message;
    }


    /**
     * Gets the _any value for this NotificationMessageHolderType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this NotificationMessageHolderType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NotificationMessageHolderType)) return false;
        NotificationMessageHolderType other = (NotificationMessageHolderType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.subscriptionReference==null && other.getSubscriptionReference()==null) || 
             (this.subscriptionReference!=null &&
              this.subscriptionReference.equals(other.getSubscriptionReference()))) &&
            ((this.producerReference==null && other.getProducerReference()==null) || 
             (this.producerReference!=null &&
              this.producerReference.equals(other.getProducerReference()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any())));
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
        if (getSubscriptionReference() != null) {
            _hashCode += getSubscriptionReference().hashCode();
        }
        if (getProducerReference() != null) {
            _hashCode += getProducerReference().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (get_any() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(get_any());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(get_any(), i);
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
        new org.apache.axis.description.TypeDesc(NotificationMessageHolderType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotificationMessageHolderType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptionReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscriptionReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("producerReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "ProducerReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "Message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">NotificationMessageHolderType>Message"));
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
