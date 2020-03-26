/**
 * GPUArchitecture_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class GPUArchitecture_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.GPUArchitectureEnumeration GPUArchitectureName;

    private org.apache.axis.message.MessageElement [] _any;

    public GPUArchitecture_Type() {
    }

    public GPUArchitecture_Type(
           org.ggf.jsdl.GPUArchitectureEnumeration GPUArchitectureName,
           org.apache.axis.message.MessageElement [] _any) {
           this.GPUArchitectureName = GPUArchitectureName;
           this._any = _any;
    }


    /**
     * Gets the GPUArchitectureName value for this GPUArchitecture_Type.
     * 
     * @return GPUArchitectureName
     */
    public org.ggf.jsdl.GPUArchitectureEnumeration getGPUArchitectureName() {
        return GPUArchitectureName;
    }


    /**
     * Sets the GPUArchitectureName value for this GPUArchitecture_Type.
     * 
     * @param GPUArchitectureName
     */
    public void setGPUArchitectureName(org.ggf.jsdl.GPUArchitectureEnumeration GPUArchitectureName) {
        this.GPUArchitectureName = GPUArchitectureName;
    }


    /**
     * Gets the _any value for this GPUArchitecture_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this GPUArchitecture_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GPUArchitecture_Type)) return false;
        GPUArchitecture_Type other = (GPUArchitecture_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.GPUArchitectureName==null && other.getGPUArchitectureName()==null) || 
             (this.GPUArchitectureName!=null &&
              this.GPUArchitectureName.equals(other.getGPUArchitectureName()))) &&
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
        if (getGPUArchitectureName() != null) {
            _hashCode += getGPUArchitectureName().hashCode();
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
        new org.apache.axis.description.TypeDesc(GPUArchitecture_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitecture_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUArchitectureName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitectureName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitectureEnumeration"));
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
