/**
 * ReifyJSDLResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.deployer;

public class ReifyJSDLResponseType  implements java.io.Serializable {
    private org.ggf.jsdl.JobDefinition_Type reifiedDocument;

    public ReifyJSDLResponseType() {
    }

    public ReifyJSDLResponseType(
           org.ggf.jsdl.JobDefinition_Type reifiedDocument) {
           this.reifiedDocument = reifiedDocument;
    }


    /**
     * Gets the reifiedDocument value for this ReifyJSDLResponseType.
     * 
     * @return reifiedDocument
     */
    public org.ggf.jsdl.JobDefinition_Type getReifiedDocument() {
        return reifiedDocument;
    }


    /**
     * Sets the reifiedDocument value for this ReifyJSDLResponseType.
     * 
     * @param reifiedDocument
     */
    public void setReifiedDocument(org.ggf.jsdl.JobDefinition_Type reifiedDocument) {
        this.reifiedDocument = reifiedDocument;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReifyJSDLResponseType)) return false;
        ReifyJSDLResponseType other = (ReifyJSDLResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.reifiedDocument==null && other.getReifiedDocument()==null) || 
             (this.reifiedDocument!=null &&
              this.reifiedDocument.equals(other.getReifiedDocument())));
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
        if (getReifiedDocument() != null) {
            _hashCode += getReifiedDocument().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReifyJSDLResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-deployer", "ReifyJSDLResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reifiedDocument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-deployer", "reified-document"));
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
