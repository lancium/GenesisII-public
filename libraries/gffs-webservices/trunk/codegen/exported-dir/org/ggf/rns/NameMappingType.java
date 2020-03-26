/**
 * NameMappingType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class NameMappingType  implements java.io.Serializable {
    private java.lang.String sourceName;  // attribute

    private java.lang.String targetName;  // attribute

    public NameMappingType() {
    }

    public NameMappingType(
           java.lang.String sourceName,
           java.lang.String targetName) {
           this.sourceName = sourceName;
           this.targetName = targetName;
    }


    /**
     * Gets the sourceName value for this NameMappingType.
     * 
     * @return sourceName
     */
    public java.lang.String getSourceName() {
        return sourceName;
    }


    /**
     * Sets the sourceName value for this NameMappingType.
     * 
     * @param sourceName
     */
    public void setSourceName(java.lang.String sourceName) {
        this.sourceName = sourceName;
    }


    /**
     * Gets the targetName value for this NameMappingType.
     * 
     * @return targetName
     */
    public java.lang.String getTargetName() {
        return targetName;
    }


    /**
     * Sets the targetName value for this NameMappingType.
     * 
     * @param targetName
     */
    public void setTargetName(java.lang.String targetName) {
        this.targetName = targetName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NameMappingType)) return false;
        NameMappingType other = (NameMappingType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.sourceName==null && other.getSourceName()==null) || 
             (this.sourceName!=null &&
              this.sourceName.equals(other.getSourceName()))) &&
            ((this.targetName==null && other.getTargetName()==null) || 
             (this.targetName!=null &&
              this.targetName.equals(other.getTargetName())));
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
        if (getSourceName() != null) {
            _hashCode += getSourceName().hashCode();
        }
        if (getTargetName() != null) {
            _hashCode += getTargetName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NameMappingType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "NameMappingType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("sourceName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "source-name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "EntryNameType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("targetName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "target-name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "EntryNameType"));
        typeDesc.addFieldDesc(attrField);
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
