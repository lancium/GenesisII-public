/**
 * CPUArchitecture_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class CPUArchitecture_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.ProcessorArchitectureEnumeration CPUArchitectureName;

    private org.apache.axis.message.MessageElement [] _any;

    public CPUArchitecture_Type() {
    }

    public CPUArchitecture_Type(
           org.ggf.jsdl.ProcessorArchitectureEnumeration CPUArchitectureName,
           org.apache.axis.message.MessageElement [] _any) {
           this.CPUArchitectureName = CPUArchitectureName;
           this._any = _any;
    }


    /**
     * Gets the CPUArchitectureName value for this CPUArchitecture_Type.
     * 
     * @return CPUArchitectureName
     */
    public org.ggf.jsdl.ProcessorArchitectureEnumeration getCPUArchitectureName() {
        return CPUArchitectureName;
    }


    /**
     * Sets the CPUArchitectureName value for this CPUArchitecture_Type.
     * 
     * @param CPUArchitectureName
     */
    public void setCPUArchitectureName(org.ggf.jsdl.ProcessorArchitectureEnumeration CPUArchitectureName) {
        this.CPUArchitectureName = CPUArchitectureName;
    }


    /**
     * Gets the _any value for this CPUArchitecture_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this CPUArchitecture_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CPUArchitecture_Type)) return false;
        CPUArchitecture_Type other = (CPUArchitecture_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.CPUArchitectureName==null && other.getCPUArchitectureName()==null) || 
             (this.CPUArchitectureName!=null &&
              this.CPUArchitectureName.equals(other.getCPUArchitectureName()))) &&
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
        if (getCPUArchitectureName() != null) {
            _hashCode += getCPUArchitectureName().hashCode();
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
        new org.apache.axis.description.TypeDesc(CPUArchitecture_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitecture_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUArchitectureName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitectureName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "ProcessorArchitectureEnumeration"));
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
