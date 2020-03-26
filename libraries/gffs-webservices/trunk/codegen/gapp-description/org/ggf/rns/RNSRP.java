/**
 * RNSRP.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class RNSRP  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.UnsignedLong elementCount;

    private java.util.Calendar createTime;

    private java.util.Calendar accessTime;

    private java.util.Calendar modificationTime;

    private boolean readable;

    private boolean writable;

    public RNSRP() {
    }

    public RNSRP(
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.UnsignedLong elementCount,
           java.util.Calendar createTime,
           java.util.Calendar accessTime,
           java.util.Calendar modificationTime,
           boolean readable,
           boolean writable) {
           this._any = _any;
           this.elementCount = elementCount;
           this.createTime = createTime;
           this.accessTime = accessTime;
           this.modificationTime = modificationTime;
           this.readable = readable;
           this.writable = writable;
    }


    /**
     * Gets the _any value for this RNSRP.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this RNSRP.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the elementCount value for this RNSRP.
     * 
     * @return elementCount
     */
    public org.apache.axis.types.UnsignedLong getElementCount() {
        return elementCount;
    }


    /**
     * Sets the elementCount value for this RNSRP.
     * 
     * @param elementCount
     */
    public void setElementCount(org.apache.axis.types.UnsignedLong elementCount) {
        this.elementCount = elementCount;
    }


    /**
     * Gets the createTime value for this RNSRP.
     * 
     * @return createTime
     */
    public java.util.Calendar getCreateTime() {
        return createTime;
    }


    /**
     * Sets the createTime value for this RNSRP.
     * 
     * @param createTime
     */
    public void setCreateTime(java.util.Calendar createTime) {
        this.createTime = createTime;
    }


    /**
     * Gets the accessTime value for this RNSRP.
     * 
     * @return accessTime
     */
    public java.util.Calendar getAccessTime() {
        return accessTime;
    }


    /**
     * Sets the accessTime value for this RNSRP.
     * 
     * @param accessTime
     */
    public void setAccessTime(java.util.Calendar accessTime) {
        this.accessTime = accessTime;
    }


    /**
     * Gets the modificationTime value for this RNSRP.
     * 
     * @return modificationTime
     */
    public java.util.Calendar getModificationTime() {
        return modificationTime;
    }


    /**
     * Sets the modificationTime value for this RNSRP.
     * 
     * @param modificationTime
     */
    public void setModificationTime(java.util.Calendar modificationTime) {
        this.modificationTime = modificationTime;
    }


    /**
     * Gets the readable value for this RNSRP.
     * 
     * @return readable
     */
    public boolean isReadable() {
        return readable;
    }


    /**
     * Sets the readable value for this RNSRP.
     * 
     * @param readable
     */
    public void setReadable(boolean readable) {
        this.readable = readable;
    }


    /**
     * Gets the writable value for this RNSRP.
     * 
     * @return writable
     */
    public boolean isWritable() {
        return writable;
    }


    /**
     * Sets the writable value for this RNSRP.
     * 
     * @param writable
     */
    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RNSRP)) return false;
        RNSRP other = (RNSRP) obj;
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
            ((this.createTime==null && other.getCreateTime()==null) || 
             (this.createTime!=null &&
              this.createTime.equals(other.getCreateTime()))) &&
            ((this.accessTime==null && other.getAccessTime()==null) || 
             (this.accessTime!=null &&
              this.accessTime.equals(other.getAccessTime()))) &&
            ((this.modificationTime==null && other.getModificationTime()==null) || 
             (this.modificationTime!=null &&
              this.modificationTime.equals(other.getModificationTime()))) &&
            this.readable == other.isReadable() &&
            this.writable == other.isWritable();
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
        if (getCreateTime() != null) {
            _hashCode += getCreateTime().hashCode();
        }
        if (getAccessTime() != null) {
            _hashCode += getAccessTime().hashCode();
        }
        if (getModificationTime() != null) {
            _hashCode += getModificationTime().hashCode();
        }
        _hashCode += (isReadable() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isWritable() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RNSRP.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", ">RNSRP"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elementCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "elementCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedLong"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("createTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "createTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accessTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "accessTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modificationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "modificationTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("readable");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "readable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("writable");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "writable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
