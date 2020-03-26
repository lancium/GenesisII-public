/**
 * BaseFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsrf.basefaults;

public class BaseFaultType  extends org.apache.axis.AxisFault  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.apache.axis.message.MessageElement [] _any;

    private java.util.Calendar timestamp;

    private org.ws.addressing.EndpointReferenceType originator;

    private org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode;

    private org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description;

    private org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause;

    public BaseFaultType() {
    }

    public BaseFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause) {
        this._any = _any;
        this.timestamp = timestamp;
        this.originator = originator;
        this.errorCode = errorCode;
        this.description = description;
        this.faultCause = faultCause;
    }


    /**
     * Gets the _any value for this BaseFaultType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this BaseFaultType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the timestamp value for this BaseFaultType.
     * 
     * @return timestamp
     */
    public java.util.Calendar getTimestamp() {
        return timestamp;
    }


    /**
     * Sets the timestamp value for this BaseFaultType.
     * 
     * @param timestamp
     */
    public void setTimestamp(java.util.Calendar timestamp) {
        this.timestamp = timestamp;
    }


    /**
     * Gets the originator value for this BaseFaultType.
     * 
     * @return originator
     */
    public org.ws.addressing.EndpointReferenceType getOriginator() {
        return originator;
    }


    /**
     * Sets the originator value for this BaseFaultType.
     * 
     * @param originator
     */
    public void setOriginator(org.ws.addressing.EndpointReferenceType originator) {
        this.originator = originator;
    }


    /**
     * Gets the errorCode value for this BaseFaultType.
     * 
     * @return errorCode
     */
    public org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode getErrorCode() {
        return errorCode;
    }


    /**
     * Sets the errorCode value for this BaseFaultType.
     * 
     * @param errorCode
     */
    public void setErrorCode(org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * Gets the description value for this BaseFaultType.
     * 
     * @return description
     */
    public org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] getDescription() {
        return description;
    }


    /**
     * Sets the description value for this BaseFaultType.
     * 
     * @param description
     */
    public void setDescription(org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description) {
        this.description = description;
    }

    public org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription getDescription(int i) {
        return this.description[i];
    }

    public void setDescription(int i, org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription _value) {
        this.description[i] = _value;
    }


    /**
     * Gets the faultCause value for this BaseFaultType.
     * 
     * @return faultCause
     */
    public org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause getFaultCause() {
        return faultCause;
    }


    /**
     * Sets the faultCause value for this BaseFaultType.
     * 
     * @param faultCause
     */
    public void setFaultCause(org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause) {
        this.faultCause = faultCause;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BaseFaultType)) return false;
        BaseFaultType other = (BaseFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.timestamp==null && other.getTimestamp()==null) || 
             (this.timestamp!=null &&
              this.timestamp.equals(other.getTimestamp()))) &&
            ((this.originator==null && other.getOriginator()==null) || 
             (this.originator!=null &&
              this.originator.equals(other.getOriginator()))) &&
            ((this.errorCode==null && other.getErrorCode()==null) || 
             (this.errorCode!=null &&
              this.errorCode.equals(other.getErrorCode()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              java.util.Arrays.equals(this.description, other.getDescription()))) &&
            ((this.faultCause==null && other.getFaultCause()==null) || 
             (this.faultCause!=null &&
              this.faultCause.equals(other.getFaultCause())));
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
        if (getTimestamp() != null) {
            _hashCode += getTimestamp().hashCode();
        }
        if (getOriginator() != null) {
            _hashCode += getOriginator().hashCode();
        }
        if (getErrorCode() != null) {
            _hashCode += getErrorCode().hashCode();
        }
        if (getDescription() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDescription());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDescription(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFaultCause() != null) {
            _hashCode += getFaultCause().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BaseFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "BaseFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timestamp");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "Timestamp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("originator");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "Originator"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "ErrorCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", ">BaseFaultType>ErrorCode"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", ">BaseFaultType>Description"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("faultCause");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "FaultCause"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", ">BaseFaultType>FaultCause"));
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
