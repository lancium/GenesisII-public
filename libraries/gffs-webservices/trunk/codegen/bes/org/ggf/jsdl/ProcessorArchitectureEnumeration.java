/**
 * ProcessorArchitectureEnumeration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class ProcessorArchitectureEnumeration implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ProcessorArchitectureEnumeration(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _sparc = "sparc";
    public static final java.lang.String _powerpc = "powerpc";
    public static final java.lang.String _x86 = "x86";
    public static final java.lang.String _x86_32 = "x86_32";
    public static final java.lang.String _x86_64 = "x86_64";
    public static final java.lang.String _parisc = "parisc";
    public static final java.lang.String _mips = "mips";
    public static final java.lang.String _ia64 = "ia64";
    public static final java.lang.String _arm = "arm";
    public static final java.lang.String _other = "other";
    public static final ProcessorArchitectureEnumeration sparc = new ProcessorArchitectureEnumeration(_sparc);
    public static final ProcessorArchitectureEnumeration powerpc = new ProcessorArchitectureEnumeration(_powerpc);
    public static final ProcessorArchitectureEnumeration x86 = new ProcessorArchitectureEnumeration(_x86);
    public static final ProcessorArchitectureEnumeration x86_32 = new ProcessorArchitectureEnumeration(_x86_32);
    public static final ProcessorArchitectureEnumeration x86_64 = new ProcessorArchitectureEnumeration(_x86_64);
    public static final ProcessorArchitectureEnumeration parisc = new ProcessorArchitectureEnumeration(_parisc);
    public static final ProcessorArchitectureEnumeration mips = new ProcessorArchitectureEnumeration(_mips);
    public static final ProcessorArchitectureEnumeration ia64 = new ProcessorArchitectureEnumeration(_ia64);
    public static final ProcessorArchitectureEnumeration arm = new ProcessorArchitectureEnumeration(_arm);
    public static final ProcessorArchitectureEnumeration other = new ProcessorArchitectureEnumeration(_other);
    public java.lang.String getValue() { return _value_;}
    public static ProcessorArchitectureEnumeration fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ProcessorArchitectureEnumeration enumeration = (ProcessorArchitectureEnumeration)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ProcessorArchitectureEnumeration fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(ProcessorArchitectureEnumeration.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "ProcessorArchitectureEnumeration"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
