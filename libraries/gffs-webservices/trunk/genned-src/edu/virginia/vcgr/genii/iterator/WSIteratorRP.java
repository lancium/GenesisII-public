/**
 * WSIteratorRP.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.iterator;

public class WSIteratorRP  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.UnsignedLong elementCount;

    private org.apache.axis.types.UnsignedInt preferredBlockSize;

    public WSIteratorRP() {
    }

    public WSIteratorRP(
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.UnsignedLong elementCount,
           org.apache.axis.types.UnsignedInt preferredBlockSize) {
           this._any = _any;
           this.elementCount = elementCount;
           this.preferredBlockSize = preferredBlockSize;
    }


    /**
     * Gets the _any value for this WSIteratorRP.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this WSIteratorRP.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the elementCount value for this WSIteratorRP.
     * 
     * @return elementCount
     */
    public org.apache.axis.types.UnsignedLong getElementCount() {
        return elementCount;
    }


    /**
     * Sets the elementCount value for this WSIteratorRP.
     * 
     * @param elementCount
     */
    public void setElementCount(org.apache.axis.types.UnsignedLong elementCount) {
        this.elementCount = elementCount;
    }


    /**
     * Gets the preferredBlockSize value for this WSIteratorRP.
     * 
     * @return preferredBlockSize
     */
    public org.apache.axis.types.UnsignedInt getPreferredBlockSize() {
        return preferredBlockSize;
    }


    /**
     * Sets the preferredBlockSize value for this WSIteratorRP.
     * 
     * @param preferredBlockSize
     */
    public void setPreferredBlockSize(org.apache.axis.types.UnsignedInt preferredBlockSize) {
        this.preferredBlockSize = preferredBlockSize;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WSIteratorRP)) return false;
        WSIteratorRP other = (WSIteratorRP) obj;
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
            ((this.elementCount==null && other.getElementCount()==null) || 
             (this.elementCount!=null &&
              this.elementCount.equals(other.getElementCount()))) &&
            ((this.preferredBlockSize==null && other.getPreferredBlockSize()==null) || 
             (this.preferredBlockSize!=null &&
              this.preferredBlockSize.equals(other.getPreferredBlockSize())));
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
        if (getElementCount() != null) {
            _hashCode += getElementCount().hashCode();
        }
        if (getPreferredBlockSize() != null) {
            _hashCode += getPreferredBlockSize().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WSIteratorRP.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", ">WSIteratorRP"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elementCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "elementCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedLong"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("preferredBlockSize");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "preferredBlockSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
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
