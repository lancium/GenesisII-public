/**
 * GPUArchitectureEnumeration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class GPUArchitectureEnumeration implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected GPUArchitectureEnumeration(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _k40 = "k40";
    public static final java.lang.String _k80 = "k80";
    public static final java.lang.String _g1070 = "g1070";
    public static final java.lang.String _g1080 = "g1080";
    public static final java.lang.String _g2080 = "g2080";
    public static final java.lang.String _g2080ti = "g2080ti";
    public static final java.lang.String _p100 = "p100";
    public static final java.lang.String _v100 = "v100";
    public static final java.lang.String _other = "other";
    public static final GPUArchitectureEnumeration k40 = new GPUArchitectureEnumeration(_k40);
    public static final GPUArchitectureEnumeration k80 = new GPUArchitectureEnumeration(_k80);
    public static final GPUArchitectureEnumeration g1070 = new GPUArchitectureEnumeration(_g1070);
    public static final GPUArchitectureEnumeration g1080 = new GPUArchitectureEnumeration(_g1080);
    public static final GPUArchitectureEnumeration g2080 = new GPUArchitectureEnumeration(_g2080);
    public static final GPUArchitectureEnumeration g2080ti = new GPUArchitectureEnumeration(_g2080ti);
    public static final GPUArchitectureEnumeration p100 = new GPUArchitectureEnumeration(_p100);
    public static final GPUArchitectureEnumeration v100 = new GPUArchitectureEnumeration(_v100);
    public static final GPUArchitectureEnumeration other = new GPUArchitectureEnumeration(_other);
    public java.lang.String getValue() { return _value_;}
    public static GPUArchitectureEnumeration fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        GPUArchitectureEnumeration enumeration = (GPUArchitectureEnumeration)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static GPUArchitectureEnumeration fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(GPUArchitectureEnumeration.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitectureEnumeration"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
