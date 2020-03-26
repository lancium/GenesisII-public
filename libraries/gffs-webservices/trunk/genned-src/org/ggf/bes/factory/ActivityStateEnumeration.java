/**
 * ActivityStateEnumeration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class ActivityStateEnumeration implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ActivityStateEnumeration(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _Pending = "Pending";
    public static final java.lang.String _Running = "Running";
    public static final java.lang.String _Cancelled = "Cancelled";
    public static final java.lang.String _Failed = "Failed";
    public static final java.lang.String _Finished = "Finished";
    public static final ActivityStateEnumeration Pending = new ActivityStateEnumeration(_Pending);
    public static final ActivityStateEnumeration Running = new ActivityStateEnumeration(_Running);
    public static final ActivityStateEnumeration Cancelled = new ActivityStateEnumeration(_Cancelled);
    public static final ActivityStateEnumeration Failed = new ActivityStateEnumeration(_Failed);
    public static final ActivityStateEnumeration Finished = new ActivityStateEnumeration(_Finished);
    public java.lang.String getValue() { return _value_;}
    public static ActivityStateEnumeration fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ActivityStateEnumeration enumeration = (ActivityStateEnumeration)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ActivityStateEnumeration fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(ActivityStateEnumeration.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityStateEnumeration"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
