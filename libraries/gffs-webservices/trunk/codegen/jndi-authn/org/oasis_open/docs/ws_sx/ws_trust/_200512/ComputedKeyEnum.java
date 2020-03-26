/**
 * ComputedKeyEnum.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_trust._200512;

public class ComputedKeyEnum implements java.io.Serializable {
    private org.apache.axis.types.URI _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ComputedKeyEnum(org.apache.axis.types.URI value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final org.apache.axis.types.URI _value1;
    static {
    	try {
            _value1 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/CK/PSHA1");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final org.apache.axis.types.URI _value2;
    static {
    	try {
            _value2 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/CK/HASH");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final ComputedKeyEnum value1 = new ComputedKeyEnum(_value1);
    public static final ComputedKeyEnum value2 = new ComputedKeyEnum(_value2);
    public org.apache.axis.types.URI getValue() { return _value_;}
    public static ComputedKeyEnum fromValue(org.apache.axis.types.URI value)
          throws java.lang.IllegalArgumentException {
        ComputedKeyEnum enumeration = (ComputedKeyEnum)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ComputedKeyEnum fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        try {
            return fromValue(new org.apache.axis.types.URI(value));
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
        new org.apache.axis.description.TypeDesc(ComputedKeyEnum.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "ComputedKeyEnum"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
