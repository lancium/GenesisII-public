/**
 * RangeValue_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class RangeValue_Type  implements java.io.Serializable {
    private org.ggf.jsdl.Boundary_Type upperBoundedRange;

    private org.ggf.jsdl.Boundary_Type lowerBoundedRange;

    private org.ggf.jsdl.Exact_Type[] exact;

    private org.ggf.jsdl.Range_Type[] range;

    public RangeValue_Type() {
    }

    public RangeValue_Type(
           org.ggf.jsdl.Boundary_Type upperBoundedRange,
           org.ggf.jsdl.Boundary_Type lowerBoundedRange,
           org.ggf.jsdl.Exact_Type[] exact,
           org.ggf.jsdl.Range_Type[] range) {
           this.upperBoundedRange = upperBoundedRange;
           this.lowerBoundedRange = lowerBoundedRange;
           this.exact = exact;
           this.range = range;
    }


    /**
     * Gets the upperBoundedRange value for this RangeValue_Type.
     * 
     * @return upperBoundedRange
     */
    public org.ggf.jsdl.Boundary_Type getUpperBoundedRange() {
        return upperBoundedRange;
    }


    /**
     * Sets the upperBoundedRange value for this RangeValue_Type.
     * 
     * @param upperBoundedRange
     */
    public void setUpperBoundedRange(org.ggf.jsdl.Boundary_Type upperBoundedRange) {
        this.upperBoundedRange = upperBoundedRange;
    }


    /**
     * Gets the lowerBoundedRange value for this RangeValue_Type.
     * 
     * @return lowerBoundedRange
     */
    public org.ggf.jsdl.Boundary_Type getLowerBoundedRange() {
        return lowerBoundedRange;
    }


    /**
     * Sets the lowerBoundedRange value for this RangeValue_Type.
     * 
     * @param lowerBoundedRange
     */
    public void setLowerBoundedRange(org.ggf.jsdl.Boundary_Type lowerBoundedRange) {
        this.lowerBoundedRange = lowerBoundedRange;
    }


    /**
     * Gets the exact value for this RangeValue_Type.
     * 
     * @return exact
     */
    public org.ggf.jsdl.Exact_Type[] getExact() {
        return exact;
    }


    /**
     * Sets the exact value for this RangeValue_Type.
     * 
     * @param exact
     */
    public void setExact(org.ggf.jsdl.Exact_Type[] exact) {
        this.exact = exact;
    }

    public org.ggf.jsdl.Exact_Type getExact(int i) {
        return this.exact[i];
    }

    public void setExact(int i, org.ggf.jsdl.Exact_Type _value) {
        this.exact[i] = _value;
    }


    /**
     * Gets the range value for this RangeValue_Type.
     * 
     * @return range
     */
    public org.ggf.jsdl.Range_Type[] getRange() {
        return range;
    }


    /**
     * Sets the range value for this RangeValue_Type.
     * 
     * @param range
     */
    public void setRange(org.ggf.jsdl.Range_Type[] range) {
        this.range = range;
    }

    public org.ggf.jsdl.Range_Type getRange(int i) {
        return this.range[i];
    }

    public void setRange(int i, org.ggf.jsdl.Range_Type _value) {
        this.range[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RangeValue_Type)) return false;
        RangeValue_Type other = (RangeValue_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.upperBoundedRange==null && other.getUpperBoundedRange()==null) || 
             (this.upperBoundedRange!=null &&
              this.upperBoundedRange.equals(other.getUpperBoundedRange()))) &&
            ((this.lowerBoundedRange==null && other.getLowerBoundedRange()==null) || 
             (this.lowerBoundedRange!=null &&
              this.lowerBoundedRange.equals(other.getLowerBoundedRange()))) &&
            ((this.exact==null && other.getExact()==null) || 
             (this.exact!=null &&
              java.util.Arrays.equals(this.exact, other.getExact()))) &&
            ((this.range==null && other.getRange()==null) || 
             (this.range!=null &&
              java.util.Arrays.equals(this.range, other.getRange())));
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
        if (getUpperBoundedRange() != null) {
            _hashCode += getUpperBoundedRange().hashCode();
        }
        if (getLowerBoundedRange() != null) {
            _hashCode += getLowerBoundedRange().hashCode();
        }
        if (getExact() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getExact());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getExact(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRange() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRange());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRange(), i);
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
        new org.apache.axis.description.TypeDesc(RangeValue_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("upperBoundedRange");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "UpperBoundedRange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Boundary_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lowerBoundedRange");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "LowerBoundedRange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Boundary_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exact");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Exact"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Exact_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("range");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Range"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Range_Type"));
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
