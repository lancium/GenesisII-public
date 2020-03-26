/**
 * GetMessagesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class GetMessagesResponse  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.oasis_open.wsn.base.NotificationMessageHolderType[] notificationMessage;

    private org.apache.axis.message.MessageElement [] _any;

    public GetMessagesResponse() {
    }

    public GetMessagesResponse(
           org.oasis_open.wsn.base.NotificationMessageHolderType[] notificationMessage,
           org.apache.axis.message.MessageElement [] _any) {
           this.notificationMessage = notificationMessage;
           this._any = _any;
    }


    /**
     * Gets the notificationMessage value for this GetMessagesResponse.
     * 
     * @return notificationMessage
     */
    public org.oasis_open.wsn.base.NotificationMessageHolderType[] getNotificationMessage() {
        return notificationMessage;
    }


    /**
     * Sets the notificationMessage value for this GetMessagesResponse.
     * 
     * @param notificationMessage
     */
    public void setNotificationMessage(org.oasis_open.wsn.base.NotificationMessageHolderType[] notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public org.oasis_open.wsn.base.NotificationMessageHolderType getNotificationMessage(int i) {
        return this.notificationMessage[i];
    }

    public void setNotificationMessage(int i, org.oasis_open.wsn.base.NotificationMessageHolderType _value) {
        this.notificationMessage[i] = _value;
    }


    /**
     * Gets the _any value for this GetMessagesResponse.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this GetMessagesResponse.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetMessagesResponse)) return false;
        GetMessagesResponse other = (GetMessagesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.notificationMessage==null && other.getNotificationMessage()==null) || 
             (this.notificationMessage!=null &&
              java.util.Arrays.equals(this.notificationMessage, other.getNotificationMessage()))) &&
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
        if (getNotificationMessage() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNotificationMessage());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNotificationMessage(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
        new org.apache.axis.description.TypeDesc(GetMessagesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetMessagesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notificationMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotificationMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotificationMessageHolderType"));
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

}
