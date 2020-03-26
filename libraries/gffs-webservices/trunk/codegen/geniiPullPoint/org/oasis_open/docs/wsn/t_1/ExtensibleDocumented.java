/**
 * ExtensibleDocumented.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsn.t_1;

public abstract class ExtensibleDocumented  implements java.io.Serializable {
    private org.oasis_open.docs.wsn.t_1.Documentation documentation;

    public ExtensibleDocumented() {
    }

    public ExtensibleDocumented(
           org.oasis_open.docs.wsn.t_1.Documentation documentation) {
           this.documentation = documentation;
    }


    /**
     * Gets the documentation value for this ExtensibleDocumented.
     * 
     * @return documentation
     */
    public org.oasis_open.docs.wsn.t_1.Documentation getDocumentation() {
        return documentation;
    }


    /**
     * Sets the documentation value for this ExtensibleDocumented.
     * 
     * @param documentation
     */
    public void setDocumentation(org.oasis_open.docs.wsn.t_1.Documentation documentation) {
        this.documentation = documentation;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExtensibleDocumented)) return false;
        ExtensibleDocumented other = (ExtensibleDocumented) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.documentation==null && other.getDocumentation()==null) || 
             (this.documentation!=null &&
              this.documentation.equals(other.getDocumentation())));
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
        if (getDocumentation() != null) {
            _hashCode += getDocumentation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ExtensibleDocumented.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "ExtensibleDocumented"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "documentation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "Documentation"));
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
