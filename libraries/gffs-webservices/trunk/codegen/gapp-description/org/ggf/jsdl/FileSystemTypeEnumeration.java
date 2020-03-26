/**
 * FileSystemTypeEnumeration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class FileSystemTypeEnumeration implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected FileSystemTypeEnumeration(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _swap = "swap";
    public static final java.lang.String _temporary = "temporary";
    public static final java.lang.String _spool = "spool";
    public static final java.lang.String _normal = "normal";
    public static final FileSystemTypeEnumeration swap = new FileSystemTypeEnumeration(_swap);
    public static final FileSystemTypeEnumeration temporary = new FileSystemTypeEnumeration(_temporary);
    public static final FileSystemTypeEnumeration spool = new FileSystemTypeEnumeration(_spool);
    public static final FileSystemTypeEnumeration normal = new FileSystemTypeEnumeration(_normal);
    public java.lang.String getValue() { return _value_;}
    public static FileSystemTypeEnumeration fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        FileSystemTypeEnumeration enumeration = (FileSystemTypeEnumeration)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static FileSystemTypeEnumeration fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(FileSystemTypeEnumeration.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileSystemTypeEnumeration"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
