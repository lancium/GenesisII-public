/**
 * GenerateX509V3CertificateChainResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.certGenerator;

public class GenerateX509V3CertificateChainResponseType  implements java.io.Serializable {
    private edu.virginia.vcgr.genii.certGenerator.CertificateChainType certificateChain;

    public GenerateX509V3CertificateChainResponseType() {
    }

    public GenerateX509V3CertificateChainResponseType(
           edu.virginia.vcgr.genii.certGenerator.CertificateChainType certificateChain) {
           this.certificateChain = certificateChain;
    }


    /**
     * Gets the certificateChain value for this GenerateX509V3CertificateChainResponseType.
     * 
     * @return certificateChain
     */
    public edu.virginia.vcgr.genii.certGenerator.CertificateChainType getCertificateChain() {
        return certificateChain;
    }


    /**
     * Sets the certificateChain value for this GenerateX509V3CertificateChainResponseType.
     * 
     * @param certificateChain
     */
    public void setCertificateChain(edu.virginia.vcgr.genii.certGenerator.CertificateChainType certificateChain) {
        this.certificateChain = certificateChain;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GenerateX509V3CertificateChainResponseType)) return false;
        GenerateX509V3CertificateChainResponseType other = (GenerateX509V3CertificateChainResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.certificateChain==null && other.getCertificateChain()==null) || 
             (this.certificateChain!=null &&
              this.certificateChain.equals(other.getCertificateChain())));
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
        if (getCertificateChain() != null) {
            _hashCode += getCertificateChain().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GenerateX509V3CertificateChainResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "generateX509V3CertificateChainResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("certificateChain");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "CertificateChain"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "CertificateChainType"));
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
