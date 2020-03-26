/**
 * LogHierarchyEntryType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common;

public class LogHierarchyEntryType  implements java.io.Serializable {
    private edu.virginia.vcgr.genii.common.RPCCallerType parent;

    private edu.virginia.vcgr.genii.common.RPCCallerType[] children;

    public LogHierarchyEntryType() {
    }

    public LogHierarchyEntryType(
           edu.virginia.vcgr.genii.common.RPCCallerType parent,
           edu.virginia.vcgr.genii.common.RPCCallerType[] children) {
           this.parent = parent;
           this.children = children;
    }


    /**
     * Gets the parent value for this LogHierarchyEntryType.
     * 
     * @return parent
     */
    public edu.virginia.vcgr.genii.common.RPCCallerType getParent() {
        return parent;
    }


    /**
     * Sets the parent value for this LogHierarchyEntryType.
     * 
     * @param parent
     */
    public void setParent(edu.virginia.vcgr.genii.common.RPCCallerType parent) {
        this.parent = parent;
    }


    /**
     * Gets the children value for this LogHierarchyEntryType.
     * 
     * @return children
     */
    public edu.virginia.vcgr.genii.common.RPCCallerType[] getChildren() {
        return children;
    }


    /**
     * Sets the children value for this LogHierarchyEntryType.
     * 
     * @param children
     */
    public void setChildren(edu.virginia.vcgr.genii.common.RPCCallerType[] children) {
        this.children = children;
    }

    public edu.virginia.vcgr.genii.common.RPCCallerType getChildren(int i) {
        return this.children[i];
    }

    public void setChildren(int i, edu.virginia.vcgr.genii.common.RPCCallerType _value) {
        this.children[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LogHierarchyEntryType)) return false;
        LogHierarchyEntryType other = (LogHierarchyEntryType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.parent==null && other.getParent()==null) || 
             (this.parent!=null &&
              this.parent.equals(other.getParent()))) &&
            ((this.children==null && other.getChildren()==null) || 
             (this.children!=null &&
              java.util.Arrays.equals(this.children, other.getChildren())));
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
        if (getParent() != null) {
            _hashCode += getParent().hashCode();
        }
        if (getChildren() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getChildren());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getChildren(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LogHierarchyEntryType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "LogHierarchyEntryType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parent");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "parent"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCCallerType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("children");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "children"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCCallerType"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
