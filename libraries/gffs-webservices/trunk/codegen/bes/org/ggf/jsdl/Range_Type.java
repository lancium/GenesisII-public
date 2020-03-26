/**
 * Range_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class Range_Type  implements java.io.Serializable {
    private org.ggf.jsdl.Boundary_Type lowerBound;

    private org.ggf.jsdl.Boundary_Type upperBound;

    public Range_Type() {
    }

    public Range_Type(
           org.ggf.jsdl.Boundary_Type lowerBound,
           org.ggf.jsdl.Boundary_Type upperBound) {
           this.lowerBound = lowerBound;
           this.upperBound = upperBound;
    }


    /**
     * Gets the lowerBound value for this Range_Type.
     * 
     * @return lowerBound
     */
    public org.ggf.jsdl.Boundary_Type getLowerBound() {
        return lowerBound;
    }


    /**
     * Sets the lowerBound value for this Range_Type.
     * 
     * @param lowerBound
     */
    public void setLowerBound(org.ggf.jsdl.Boundary_Type lowerBound) {
        this.lowerBound = lowerBound;
    }


    /**
     * Gets the upperBound value for this Range_Type.
     * 
     * @return upperBound
     */
    public org.ggf.jsdl.Boundary_Type getUpperBound() {
        return upperBound;
    }


    /**
     * Sets the upperBound value for this Range_Type.
     * 
     * @param upperBound
     */
    public void setUpperBound(org.ggf.jsdl.Boundary_Type upperBound) {
        this.upperBound = upperBound;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Range_Type)) return false;
        Range_Type other = (Range_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.lowerBound==null && other.getLowerBound()==null) || 
             (this.lowerBound!=null &&
              this.lowerBound.equals(other.getLowerBound()))) &&
            ((this.upperBound==null && other.getUpperBound()==null) || 
             (this.upperBound!=null &&
              this.upperBound.equals(other.getUpperBound())));
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
        if (getLowerBound() != null) {
            _hashCode += getLowerBound().hashCode();
        }
        if (getUpperBound() != null) {
            _hashCode += getUpperBound().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Range_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Range_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lowerBound");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "LowerBound"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Boundary_Type"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("upperBound");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "UpperBound"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Boundary_Type"));
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
