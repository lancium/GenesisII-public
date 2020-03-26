/**
 * RequestSecurityTokenResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_trust._200512;


/**
 * Actual content model is non-deterministic, hence wildcard. The
 * following shows intended content model:
 * 
 *         <xs:element ref='wst:TokenType' minOccurs='0' />
 *         <xs:element ref='wst:RequestType' />
 *         <xs:element ref='wst:RequestedSecurityToken'  minOccurs='0'
 * />
 *         <xs:element ref='wsp:AppliesTo' minOccurs='0' />
 *         <xs:element ref='wst:RequestedAttachedReference' minOccurs='0'
 * />
 *         <xs:element ref='wst:RequestedUnattachedReference' minOccurs='0'
 * />
 *         <xs:element ref='wst:RequestedProofToken' minOccurs='0' />
 * <xs:element ref='wst:Entropy' minOccurs='0' />
 *         <xs:element ref='wst:Lifetime' minOccurs='0' />
 *         <xs:element ref='wst:Status' minOccurs='0' />
 *         <xs:element ref='wst:AllowPostdating' minOccurs='0' />
 *         <xs:element ref='wst:Renewing' minOccurs='0' />
 *         <xs:element ref='wst:OnBehalfOf' minOccurs='0' />
 *         <xs:element ref='wst:Issuer' minOccurs='0' />
 *         <xs:element ref='wst:AuthenticationType' minOccurs='0' />
 *         <xs:element ref='wst:Authenticator' minOccurs='0' />
 *         <xs:element ref='wst:KeyType' minOccurs='0' />
 *         <xs:element ref='wst:KeySize' minOccurs='0' />
 *         <xs:element ref='wst:SignatureAlgorithm' minOccurs='0' />
 *         <xs:element ref='wst:Encryption' minOccurs='0' />
 *         <xs:element ref='wst:EncryptionAlgorithm' minOccurs='0' />
 * <xs:element ref='wst:CanonicalizationAlgorithm' minOccurs='0' />
 *         <xs:element ref='wst:ProofEncryption' minOccurs='0' />
 *         <xs:element ref='wst:UseKey' minOccurs='0' />
 *         <xs:element ref='wst:SignWith' minOccurs='0' />
 *         <xs:element ref='wst:EncryptWith' minOccurs='0' />
 *         <xs:element ref='wst:DelegateTo' minOccurs='0' />
 *         <xs:element ref='wst:Forwardable' minOccurs='0' />
 *         <xs:element ref='wst:Delegatable' minOccurs='0' />
 *         <xs:element ref='wsp:Policy' minOccurs='0' />
 *         <xs:element ref='wsp:PolicyReference' minOccurs='0' />
 *         <xs:any namespace='##other' processContents='lax' minOccurs='0'
 * maxOccurs='unbounded' />
 */
public class RequestSecurityTokenResponseType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.URI context;  // attribute

    public RequestSecurityTokenResponseType() {
    }

    public RequestSecurityTokenResponseType(
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.URI context) {
           this._any = _any;
           this.context = context;
    }


    /**
     * Gets the _any value for this RequestSecurityTokenResponseType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this RequestSecurityTokenResponseType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the context value for this RequestSecurityTokenResponseType.
     * 
     * @return context
     */
    public org.apache.axis.types.URI getContext() {
        return context;
    }


    /**
     * Sets the context value for this RequestSecurityTokenResponseType.
     * 
     * @param context
     */
    public void setContext(org.apache.axis.types.URI context) {
        this.context = context;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RequestSecurityTokenResponseType)) return false;
        RequestSecurityTokenResponseType other = (RequestSecurityTokenResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.context==null && other.getContext()==null) || 
             (this.context!=null &&
              this.context.equals(other.getContext())));
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
        if (getContext() != null) {
            _hashCode += getContext().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RequestSecurityTokenResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RequestSecurityTokenResponseType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("context");
        attrField.setXmlName(new javax.xml.namespace.QName("", "Context"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        typeDesc.addFieldDesc(attrField);
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
