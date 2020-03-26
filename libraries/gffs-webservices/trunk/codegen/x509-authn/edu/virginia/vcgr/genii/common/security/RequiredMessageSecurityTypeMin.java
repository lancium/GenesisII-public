/**
 * RequiredMessageSecurityTypeMin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common.security;

public class RequiredMessageSecurityTypeMin implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected RequiredMessageSecurityTypeMin(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _value1 = "NONE";
    public static final java.lang.String _value2 = "SIGN";
    public static final java.lang.String _value3 = "ENCRYPT";
    public static final java.lang.String _value4 = "SIGN|ENCRYPT";
    public static final RequiredMessageSecurityTypeMin value1 = new RequiredMessageSecurityTypeMin(_value1);
    public static final RequiredMessageSecurityTypeMin value2 = new RequiredMessageSecurityTypeMin(_value2);
    public static final RequiredMessageSecurityTypeMin value3 = new RequiredMessageSecurityTypeMin(_value3);
    public static final RequiredMessageSecurityTypeMin value4 = new RequiredMessageSecurityTypeMin(_value4);
    public java.lang.String getValue() { return _value_;}
    public static RequiredMessageSecurityTypeMin fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        RequiredMessageSecurityTypeMin enumeration = (RequiredMessageSecurityTypeMin)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static RequiredMessageSecurityTypeMin fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
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
        new org.apache.axis.description.TypeDesc(RequiredMessageSecurityTypeMin.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", ">RequiredMessageSecurityType>min"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
