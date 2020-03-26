/**
 * IterateRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.iterator;

public class IterateRequestType  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedLong startOffset;

    private org.apache.axis.types.UnsignedLong elementCount;

    public IterateRequestType() {
    }

    public IterateRequestType(
           org.apache.axis.types.UnsignedLong startOffset,
           org.apache.axis.types.UnsignedLong elementCount) {
           this.startOffset = startOffset;
           this.elementCount = elementCount;
    }


    /**
     * Gets the startOffset value for this IterateRequestType.
     * 
     * @return startOffset
     */
    public org.apache.axis.types.UnsignedLong getStartOffset() {
        return startOffset;
    }


    /**
     * Sets the startOffset value for this IterateRequestType.
     * 
     * @param startOffset
     */
    public void setStartOffset(org.apache.axis.types.UnsignedLong startOffset) {
        this.startOffset = startOffset;
    }


    /**
     * Gets the elementCount value for this IterateRequestType.
     * 
     * @return elementCount
     */
    public org.apache.axis.types.UnsignedLong getElementCount() {
        return elementCount;
    }


    /**
     * Sets the elementCount value for this IterateRequestType.
     * 
     * @param elementCount
     */
    public void setElementCount(org.apache.axis.types.UnsignedLong elementCount) {
        this.elementCount = elementCount;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IterateRequestType)) return false;
        IterateRequestType other = (IterateRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.startOffset==null && other.getStartOffset()==null) || 
             (this.startOffset!=null &&
              this.startOffset.equals(other.getStartOffset()))) &&
            ((this.elementCount==null && other.getElementCount()==null) || 
             (this.elementCount!=null &&
              this.elementCount.equals(other.getElementCount())));
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
        if (getStartOffset() != null) {
            _hashCode += getStartOffset().hashCode();
        }
        if (getElementCount() != null) {
            _hashCode += getElementCount().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(IterateRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterateRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startOffset");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "start-offset"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedLong"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elementCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "element-count"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedLong"));
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
