/**
 * FaultCodesType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ws.addressing;

public class FaultCodesType implements java.io.Serializable {
    private javax.xml.namespace.QName _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected FaultCodesType(javax.xml.namespace.QName value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final javax.xml.namespace.QName _value1 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}InvalidAddressingHeader");
    public static final javax.xml.namespace.QName _value2 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}InvalidAddress");
    public static final javax.xml.namespace.QName _value3 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}InvalidEPR");
    public static final javax.xml.namespace.QName _value4 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}InvalidCardinality");
    public static final javax.xml.namespace.QName _value5 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}MissingAddressInEPR");
    public static final javax.xml.namespace.QName _value6 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}DuplicateMessageID");
    public static final javax.xml.namespace.QName _value7 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}ActionMismatch");
    public static final javax.xml.namespace.QName _value8 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}MessageAddressingHeaderRequired");
    public static final javax.xml.namespace.QName _value9 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}DestinationUnreachable");
    public static final javax.xml.namespace.QName _value10 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}ActionNotSupported");
    public static final javax.xml.namespace.QName _value11 = javax.xml.namespace.QName.valueOf("{http://www.w3.org/2005/08/addressing}EndpointUnavailable");
    public static final FaultCodesType value1 = new FaultCodesType(_value1);
    public static final FaultCodesType value2 = new FaultCodesType(_value2);
    public static final FaultCodesType value3 = new FaultCodesType(_value3);
    public static final FaultCodesType value4 = new FaultCodesType(_value4);
    public static final FaultCodesType value5 = new FaultCodesType(_value5);
    public static final FaultCodesType value6 = new FaultCodesType(_value6);
    public static final FaultCodesType value7 = new FaultCodesType(_value7);
    public static final FaultCodesType value8 = new FaultCodesType(_value8);
    public static final FaultCodesType value9 = new FaultCodesType(_value9);
    public static final FaultCodesType value10 = new FaultCodesType(_value10);
    public static final FaultCodesType value11 = new FaultCodesType(_value11);
    public javax.xml.namespace.QName getValue() { return _value_;}
    public static FaultCodesType fromValue(javax.xml.namespace.QName value)
          throws java.lang.IllegalArgumentException {
        FaultCodesType enumeration = (FaultCodesType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static FaultCodesType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(FaultCodesType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "FaultCodesType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
