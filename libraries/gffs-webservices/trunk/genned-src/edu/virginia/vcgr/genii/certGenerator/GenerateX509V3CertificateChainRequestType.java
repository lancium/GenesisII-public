/**
 * GenerateX509V3CertificateChainRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.certGenerator;

public class GenerateX509V3CertificateChainRequestType  implements java.io.Serializable {
    private edu.virginia.vcgr.genii.certGenerator.X509NameType x509Name;

    private edu.virginia.vcgr.genii.certGenerator.PublicKeyType publicKey;

    public GenerateX509V3CertificateChainRequestType() {
    }

    public GenerateX509V3CertificateChainRequestType(
           edu.virginia.vcgr.genii.certGenerator.X509NameType x509Name,
           edu.virginia.vcgr.genii.certGenerator.PublicKeyType publicKey) {
           this.x509Name = x509Name;
           this.publicKey = publicKey;
    }


    /**
     * Gets the x509Name value for this GenerateX509V3CertificateChainRequestType.
     * 
     * @return x509Name
     */
    public edu.virginia.vcgr.genii.certGenerator.X509NameType getX509Name() {
        return x509Name;
    }


    /**
     * Sets the x509Name value for this GenerateX509V3CertificateChainRequestType.
     * 
     * @param x509Name
     */
    public void setX509Name(edu.virginia.vcgr.genii.certGenerator.X509NameType x509Name) {
        this.x509Name = x509Name;
    }


    /**
     * Gets the publicKey value for this GenerateX509V3CertificateChainRequestType.
     * 
     * @return publicKey
     */
    public edu.virginia.vcgr.genii.certGenerator.PublicKeyType getPublicKey() {
        return publicKey;
    }


    /**
     * Sets the publicKey value for this GenerateX509V3CertificateChainRequestType.
     * 
     * @param publicKey
     */
    public void setPublicKey(edu.virginia.vcgr.genii.certGenerator.PublicKeyType publicKey) {
        this.publicKey = publicKey;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GenerateX509V3CertificateChainRequestType)) return false;
        GenerateX509V3CertificateChainRequestType other = (GenerateX509V3CertificateChainRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.x509Name==null && other.getX509Name()==null) || 
             (this.x509Name!=null &&
              this.x509Name.equals(other.getX509Name()))) &&
            ((this.publicKey==null && other.getPublicKey()==null) || 
             (this.publicKey!=null &&
              this.publicKey.equals(other.getPublicKey())));
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
        if (getX509Name() != null) {
            _hashCode += getX509Name().hashCode();
        }
        if (getPublicKey() != null) {
            _hashCode += getPublicKey().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GenerateX509V3CertificateChainRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "generateX509V3CertificateChainRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("x509Name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "X509Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "X509NameType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publicKey");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "PublicKey"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2007/08/certGenerator", "PublicKeyType"));
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
