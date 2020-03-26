/**
 * InvalidRequestMessageFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class InvalidRequestMessageFaultType  extends org.apache.axis.AxisFault  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private java.lang.String[] invalidElement;

    private java.lang.String message1;

    private org.apache.axis.message.MessageElement [] _any;

    public InvalidRequestMessageFaultType() {
    }

    public InvalidRequestMessageFaultType(
           java.lang.String[] invalidElement,
           java.lang.String message1,
           org.apache.axis.message.MessageElement [] _any) {
        this.invalidElement = invalidElement;
        this.message1 = message1;
        this._any = _any;
    }


    /**
     * Gets the invalidElement value for this InvalidRequestMessageFaultType.
     * 
     * @return invalidElement
     */
    public java.lang.String[] getInvalidElement() {
        return invalidElement;
    }


    /**
     * Sets the invalidElement value for this InvalidRequestMessageFaultType.
     * 
     * @param invalidElement
     */
    public void setInvalidElement(java.lang.String[] invalidElement) {
        this.invalidElement = invalidElement;
    }

    public java.lang.String getInvalidElement(int i) {
        return this.invalidElement[i];
    }

    public void setInvalidElement(int i, java.lang.String _value) {
        this.invalidElement[i] = _value;
    }


    /**
     * Gets the message1 value for this InvalidRequestMessageFaultType.
     * 
     * @return message1
     */
    public java.lang.String getMessage1() {
        return message1;
    }


    /**
     * Sets the message1 value for this InvalidRequestMessageFaultType.
     * 
     * @param message1
     */
    public void setMessage1(java.lang.String message1) {
        this.message1 = message1;
    }


    /**
     * Gets the _any value for this InvalidRequestMessageFaultType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this InvalidRequestMessageFaultType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InvalidRequestMessageFaultType)) return false;
        InvalidRequestMessageFaultType other = (InvalidRequestMessageFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.invalidElement==null && other.getInvalidElement()==null) || 
             (this.invalidElement!=null &&
              java.util.Arrays.equals(this.invalidElement, other.getInvalidElement()))) &&
            ((this.message1==null && other.getMessage1()==null) || 
             (this.message1!=null &&
              this.message1.equals(other.getMessage1()))) &&
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
        if (getInvalidElement() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getInvalidElement());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getInvalidElement(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMessage1() != null) {
            _hashCode += getMessage1().hashCode();
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
        new org.apache.axis.description.TypeDesc(InvalidRequestMessageFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "InvalidRequestMessageFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invalidElement");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "InvalidElement"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message1");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "Message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
