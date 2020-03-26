/**
 * ReifyJSDLRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.deployer;

public class ReifyJSDLRequestType  implements java.io.Serializable {
    private org.ggf.jsdl.JobDefinition_Type unreifiedDocument;

    public ReifyJSDLRequestType() {
    }

    public ReifyJSDLRequestType(
           org.ggf.jsdl.JobDefinition_Type unreifiedDocument) {
           this.unreifiedDocument = unreifiedDocument;
    }


    /**
     * Gets the unreifiedDocument value for this ReifyJSDLRequestType.
     * 
     * @return unreifiedDocument
     */
    public org.ggf.jsdl.JobDefinition_Type getUnreifiedDocument() {
        return unreifiedDocument;
    }


    /**
     * Sets the unreifiedDocument value for this ReifyJSDLRequestType.
     * 
     * @param unreifiedDocument
     */
    public void setUnreifiedDocument(org.ggf.jsdl.JobDefinition_Type unreifiedDocument) {
        this.unreifiedDocument = unreifiedDocument;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReifyJSDLRequestType)) return false;
        ReifyJSDLRequestType other = (ReifyJSDLRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.unreifiedDocument==null && other.getUnreifiedDocument()==null) || 
             (this.unreifiedDocument!=null &&
              this.unreifiedDocument.equals(other.getUnreifiedDocument())));
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
        if (getUnreifiedDocument() != null) {
            _hashCode += getUnreifiedDocument().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReifyJSDLRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-deployer", "ReifyJSDLRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unreifiedDocument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-deployer", "unreified-document"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDefinition_Type"));
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
