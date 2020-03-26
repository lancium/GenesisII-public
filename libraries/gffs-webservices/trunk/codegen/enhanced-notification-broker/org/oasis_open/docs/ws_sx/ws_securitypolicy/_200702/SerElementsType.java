/**
 * SerElementsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_securitypolicy._200702;

public class SerElementsType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private java.lang.String[] XPath;

    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.URI XPathVersion;  // attribute

    public SerElementsType() {
    }

    public SerElementsType(
           java.lang.String[] XPath,
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.URI XPathVersion) {
           this.XPath = XPath;
           this._any = _any;
           this.XPathVersion = XPathVersion;
    }


    /**
     * Gets the XPath value for this SerElementsType.
     * 
     * @return XPath
     */
    public java.lang.String[] getXPath() {
        return XPath;
    }


    /**
     * Sets the XPath value for this SerElementsType.
     * 
     * @param XPath
     */
    public void setXPath(java.lang.String[] XPath) {
        this.XPath = XPath;
    }

    public java.lang.String getXPath(int i) {
        return this.XPath[i];
    }

    public void setXPath(int i, java.lang.String _value) {
        this.XPath[i] = _value;
    }


    /**
     * Gets the _any value for this SerElementsType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this SerElementsType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the XPathVersion value for this SerElementsType.
     * 
     * @return XPathVersion
     */
    public org.apache.axis.types.URI getXPathVersion() {
        return XPathVersion;
    }


    /**
     * Sets the XPathVersion value for this SerElementsType.
     * 
     * @param XPathVersion
     */
    public void setXPathVersion(org.apache.axis.types.URI XPathVersion) {
        this.XPathVersion = XPathVersion;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SerElementsType)) return false;
        SerElementsType other = (SerElementsType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.XPath==null && other.getXPath()==null) || 
             (this.XPath!=null &&
              java.util.Arrays.equals(this.XPath, other.getXPath()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.XPathVersion==null && other.getXPathVersion()==null) || 
             (this.XPathVersion!=null &&
              this.XPathVersion.equals(other.getXPathVersion())));
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
        if (getXPath() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getXPath());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getXPath(), i);
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
        if (getXPathVersion() != null) {
            _hashCode += getXPathVersion().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SerElementsType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "SerElementsType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("XPathVersion");
        attrField.setXmlName(new javax.xml.namespace.QName("", "XPathVersion"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("XPath");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "XPath"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
