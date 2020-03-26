/**
 * IterateResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.iterator;

public class IterateResponseType  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedLong iteratorSize;

    private edu.virginia.vcgr.genii.iterator.IterableElementType[] iterableElement;

    public IterateResponseType() {
    }

    public IterateResponseType(
           org.apache.axis.types.UnsignedLong iteratorSize,
           edu.virginia.vcgr.genii.iterator.IterableElementType[] iterableElement) {
           this.iteratorSize = iteratorSize;
           this.iterableElement = iterableElement;
    }


    /**
     * Gets the iteratorSize value for this IterateResponseType.
     * 
     * @return iteratorSize
     */
    public org.apache.axis.types.UnsignedLong getIteratorSize() {
        return iteratorSize;
    }


    /**
     * Sets the iteratorSize value for this IterateResponseType.
     * 
     * @param iteratorSize
     */
    public void setIteratorSize(org.apache.axis.types.UnsignedLong iteratorSize) {
        this.iteratorSize = iteratorSize;
    }


    /**
     * Gets the iterableElement value for this IterateResponseType.
     * 
     * @return iterableElement
     */
    public edu.virginia.vcgr.genii.iterator.IterableElementType[] getIterableElement() {
        return iterableElement;
    }


    /**
     * Sets the iterableElement value for this IterateResponseType.
     * 
     * @param iterableElement
     */
    public void setIterableElement(edu.virginia.vcgr.genii.iterator.IterableElementType[] iterableElement) {
        this.iterableElement = iterableElement;
    }

    public edu.virginia.vcgr.genii.iterator.IterableElementType getIterableElement(int i) {
        return this.iterableElement[i];
    }

    public void setIterableElement(int i, edu.virginia.vcgr.genii.iterator.IterableElementType _value) {
        this.iterableElement[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IterateResponseType)) return false;
        IterateResponseType other = (IterateResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.iteratorSize==null && other.getIteratorSize()==null) || 
             (this.iteratorSize!=null &&
              this.iteratorSize.equals(other.getIteratorSize()))) &&
            ((this.iterableElement==null && other.getIterableElement()==null) || 
             (this.iterableElement!=null &&
              java.util.Arrays.equals(this.iterableElement, other.getIterableElement())));
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
        if (getIteratorSize() != null) {
            _hashCode += getIteratorSize().hashCode();
        }
        if (getIterableElement() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getIterableElement());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getIterableElement(), i);
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
        new org.apache.axis.description.TypeDesc(IterateResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterateResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iteratorSize");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "iterator-size"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedLong"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iterableElement");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "iterable-element"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterableElementType"));
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
