/**
 * FaultcodeEnum.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd;

public class FaultcodeEnum implements java.io.Serializable {
    private javax.xml.namespace.QName _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected FaultcodeEnum(javax.xml.namespace.QName value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final javax.xml.namespace.QName _value1 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}UnsupportedSecurityToken");
    public static final javax.xml.namespace.QName _value2 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}UnsupportedAlgorithm");
    public static final javax.xml.namespace.QName _value3 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}InvalidSecurity");
    public static final javax.xml.namespace.QName _value4 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}InvalidSecurityToken");
    public static final javax.xml.namespace.QName _value5 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}FailedAuthentication");
    public static final javax.xml.namespace.QName _value6 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}FailedCheck");
    public static final javax.xml.namespace.QName _value7 = javax.xml.namespace.QName.valueOf("{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}SecurityTokenUnavailable");
    public static final FaultcodeEnum value1 = new FaultcodeEnum(_value1);
    public static final FaultcodeEnum value2 = new FaultcodeEnum(_value2);
    public static final FaultcodeEnum value3 = new FaultcodeEnum(_value3);
    public static final FaultcodeEnum value4 = new FaultcodeEnum(_value4);
    public static final FaultcodeEnum value5 = new FaultcodeEnum(_value5);
    public static final FaultcodeEnum value6 = new FaultcodeEnum(_value6);
    public static final FaultcodeEnum value7 = new FaultcodeEnum(_value7);
    public javax.xml.namespace.QName getValue() { return _value_;}
    public static FaultcodeEnum fromValue(javax.xml.namespace.QName value)
          throws java.lang.IllegalArgumentException {
        FaultcodeEnum enumeration = (FaultcodeEnum)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static FaultcodeEnum fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        try {
            return fromValue(javax.xml.namespace.QName.valueOf(value));
        } catch (Exception e) {
            throw new java.lang.IllegalArgumentException();
        }
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_.toString();}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FaultcodeEnum.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "FaultcodeEnum"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
