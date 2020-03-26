/**
 * ZipJarSourceType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc.zipjar;

public class ZipJarSourceType  extends edu.virginia.vcgr.genii.appdesc.SourceElementType  implements java.io.Serializable {
    private edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarEnumeration packageType;  // attribute

    public ZipJarSourceType() {
    }

    public ZipJarSourceType(
           org.apache.axis.message.MessageElement [] _any,
           edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarEnumeration packageType) {
        super(
            _any);
        this.packageType = packageType;
    }


    /**
     * Gets the packageType value for this ZipJarSourceType.
     * 
     * @return packageType
     */
    public edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarEnumeration getPackageType() {
        return packageType;
    }


    /**
     * Sets the packageType value for this ZipJarSourceType.
     * 
     * @param packageType
     */
    public void setPackageType(edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarEnumeration packageType) {
        this.packageType = packageType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ZipJarSourceType)) return false;
        ZipJarSourceType other = (ZipJarSourceType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.packageType==null && other.getPackageType()==null) || 
             (this.packageType!=null &&
              this.packageType.equals(other.getPackageType())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getPackageType() != null) {
            _hashCode += getPackageType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ZipJarSourceType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/zip-jar", "ZipJarSourceType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("packageType");
        attrField.setXmlName(new javax.xml.namespace.QName("", "package-type"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/zip-jar", "ZipJarEnumeration"));
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
