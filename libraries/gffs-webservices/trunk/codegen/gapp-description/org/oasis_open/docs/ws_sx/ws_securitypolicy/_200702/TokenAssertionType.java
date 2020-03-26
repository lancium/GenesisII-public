/**
 * TokenAssertionType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_securitypolicy._200702;

public class TokenAssertionType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType issuer;

    private org.apache.axis.types.URI issuerName;

    private org.apache.axis.message.MessageElement [] _any;

    private org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType includeToken;  // attribute

    public TokenAssertionType() {
    }

    public TokenAssertionType(
           org.ws.addressing.EndpointReferenceType issuer,
           org.apache.axis.types.URI issuerName,
           org.apache.axis.message.MessageElement [] _any,
           org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType includeToken) {
           this.issuer = issuer;
           this.issuerName = issuerName;
           this._any = _any;
           this.includeToken = includeToken;
    }


    /**
     * Gets the issuer value for this TokenAssertionType.
     * 
     * @return issuer
     */
    public org.ws.addressing.EndpointReferenceType getIssuer() {
        return issuer;
    }


    /**
     * Sets the issuer value for this TokenAssertionType.
     * 
     * @param issuer
     */
    public void setIssuer(org.ws.addressing.EndpointReferenceType issuer) {
        this.issuer = issuer;
    }


    /**
     * Gets the issuerName value for this TokenAssertionType.
     * 
     * @return issuerName
     */
    public org.apache.axis.types.URI getIssuerName() {
        return issuerName;
    }


    /**
     * Sets the issuerName value for this TokenAssertionType.
     * 
     * @param issuerName
     */
    public void setIssuerName(org.apache.axis.types.URI issuerName) {
        this.issuerName = issuerName;
    }


    /**
     * Gets the _any value for this TokenAssertionType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this TokenAssertionType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the includeToken value for this TokenAssertionType.
     * 
     * @return includeToken
     */
    public org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType getIncludeToken() {
        return includeToken;
    }


    /**
     * Sets the includeToken value for this TokenAssertionType.
     * 
     * @param includeToken
     */
    public void setIncludeToken(org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType includeToken) {
        this.includeToken = includeToken;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TokenAssertionType)) return false;
        TokenAssertionType other = (TokenAssertionType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.issuer==null && other.getIssuer()==null) || 
             (this.issuer!=null &&
              this.issuer.equals(other.getIssuer()))) &&
            ((this.issuerName==null && other.getIssuerName()==null) || 
             (this.issuerName!=null &&
              this.issuerName.equals(other.getIssuerName()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.includeToken==null && other.getIncludeToken()==null) || 
             (this.includeToken!=null &&
              this.includeToken.equals(other.getIncludeToken())));
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
        if (getIssuer() != null) {
            _hashCode += getIssuer().hashCode();
        }
        if (getIssuerName() != null) {
            _hashCode += getIssuerName().hashCode();
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
        if (getIncludeToken() != null) {
            _hashCode += getIncludeToken().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TokenAssertionType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "TokenAssertionType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("includeToken");
        attrField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "IncludeToken"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "IncludeTokenOpenType"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("issuer");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "Issuer"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("issuerName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "IssuerName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
