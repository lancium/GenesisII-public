/**
 * GetActivityStatusResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class GetActivityStatusResponseType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType activityIdentifier;

    private org.ggf.bes.factory.ActivityStatusType activityStatus;

    private org.xmlsoap.schemas.soap.envelope.Fault fault;

    private org.apache.axis.message.MessageElement [] _any;

    public GetActivityStatusResponseType() {
    }

    public GetActivityStatusResponseType(
           org.ws.addressing.EndpointReferenceType activityIdentifier,
           org.ggf.bes.factory.ActivityStatusType activityStatus,
           org.xmlsoap.schemas.soap.envelope.Fault fault,
           org.apache.axis.message.MessageElement [] _any) {
           this.activityIdentifier = activityIdentifier;
           this.activityStatus = activityStatus;
           this.fault = fault;
           this._any = _any;
    }


    /**
     * Gets the activityIdentifier value for this GetActivityStatusResponseType.
     * 
     * @return activityIdentifier
     */
    public org.ws.addressing.EndpointReferenceType getActivityIdentifier() {
        return activityIdentifier;
    }


    /**
     * Sets the activityIdentifier value for this GetActivityStatusResponseType.
     * 
     * @param activityIdentifier
     */
    public void setActivityIdentifier(org.ws.addressing.EndpointReferenceType activityIdentifier) {
        this.activityIdentifier = activityIdentifier;
    }


    /**
     * Gets the activityStatus value for this GetActivityStatusResponseType.
     * 
     * @return activityStatus
     */
    public org.ggf.bes.factory.ActivityStatusType getActivityStatus() {
        return activityStatus;
    }


    /**
     * Sets the activityStatus value for this GetActivityStatusResponseType.
     * 
     * @param activityStatus
     */
    public void setActivityStatus(org.ggf.bes.factory.ActivityStatusType activityStatus) {
        this.activityStatus = activityStatus;
    }


    /**
     * Gets the fault value for this GetActivityStatusResponseType.
     * 
     * @return fault
     */
    public org.xmlsoap.schemas.soap.envelope.Fault getFault() {
        return fault;
    }


    /**
     * Sets the fault value for this GetActivityStatusResponseType.
     * 
     * @param fault
     */
    public void setFault(org.xmlsoap.schemas.soap.envelope.Fault fault) {
        this.fault = fault;
    }


    /**
     * Gets the _any value for this GetActivityStatusResponseType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this GetActivityStatusResponseType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetActivityStatusResponseType)) return false;
        GetActivityStatusResponseType other = (GetActivityStatusResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.activityIdentifier==null && other.getActivityIdentifier()==null) || 
             (this.activityIdentifier!=null &&
              this.activityIdentifier.equals(other.getActivityIdentifier()))) &&
            ((this.activityStatus==null && other.getActivityStatus()==null) || 
             (this.activityStatus!=null &&
              this.activityStatus.equals(other.getActivityStatus()))) &&
            ((this.fault==null && other.getFault()==null) || 
             (this.fault!=null &&
              this.fault.equals(other.getFault()))) &&
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
        if (getActivityIdentifier() != null) {
            _hashCode += getActivityIdentifier().hashCode();
        }
        if (getActivityStatus() != null) {
            _hashCode += getActivityStatus().hashCode();
        }
        if (getFault() != null) {
            _hashCode += getFault().hashCode();
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
        new org.apache.axis.description.TypeDesc(GetActivityStatusResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GetActivityStatusResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityStatusType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fault");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "Fault"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/envelope/", "Fault"));
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
