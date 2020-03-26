/**
 * TerminateActivityResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class TerminateActivityResponseType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType activityIdentifier;

    private boolean terminated;

    private org.xmlsoap.schemas.soap.envelope.Fault fault;

    private org.apache.axis.message.MessageElement [] _any;

    public TerminateActivityResponseType() {
    }

    public TerminateActivityResponseType(
           org.ws.addressing.EndpointReferenceType activityIdentifier,
           boolean terminated,
           org.xmlsoap.schemas.soap.envelope.Fault fault,
           org.apache.axis.message.MessageElement [] _any) {
           this.activityIdentifier = activityIdentifier;
           this.terminated = terminated;
           this.fault = fault;
           this._any = _any;
    }


    /**
     * Gets the activityIdentifier value for this TerminateActivityResponseType.
     * 
     * @return activityIdentifier
     */
    public org.ws.addressing.EndpointReferenceType getActivityIdentifier() {
        return activityIdentifier;
    }


    /**
     * Sets the activityIdentifier value for this TerminateActivityResponseType.
     * 
     * @param activityIdentifier
     */
    public void setActivityIdentifier(org.ws.addressing.EndpointReferenceType activityIdentifier) {
        this.activityIdentifier = activityIdentifier;
    }


    /**
     * Gets the terminated value for this TerminateActivityResponseType.
     * 
     * @return terminated
     */
    public boolean isTerminated() {
        return terminated;
    }


    /**
     * Sets the terminated value for this TerminateActivityResponseType.
     * 
     * @param terminated
     */
    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }


    /**
     * Gets the fault value for this TerminateActivityResponseType.
     * 
     * @return fault
     */
    public org.xmlsoap.schemas.soap.envelope.Fault getFault() {
        return fault;
    }


    /**
     * Sets the fault value for this TerminateActivityResponseType.
     * 
     * @param fault
     */
    public void setFault(org.xmlsoap.schemas.soap.envelope.Fault fault) {
        this.fault = fault;
    }


    /**
     * Gets the _any value for this TerminateActivityResponseType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this TerminateActivityResponseType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TerminateActivityResponseType)) return false;
        TerminateActivityResponseType other = (TerminateActivityResponseType) obj;
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
            this.terminated == other.isTerminated() &&
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
        _hashCode += (isTerminated() ? Boolean.TRUE : Boolean.FALSE).hashCode();
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
        new org.apache.axis.description.TypeDesc(TerminateActivityResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "TerminateActivityResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("terminated");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "Terminated"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
