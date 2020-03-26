/**
 * ZipJarDeploymentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc.zipjar;

public class ZipJarDeploymentType  extends edu.virginia.vcgr.genii.appdesc.CommonDeploymentDescriptionType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarSourceType source;

    public ZipJarDeploymentType() {
    }

    public ZipJarDeploymentType(
           java.lang.String relativeCwd,
           java.lang.String binaryName,
           org.apache.axis.message.MessageElement [] _any,
           edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarSourceType source) {
        super(
            relativeCwd,
            binaryName,
            _any);
        this.source = source;
    }


    /**
     * Gets the source value for this ZipJarDeploymentType.
     * 
     * @return source
     */
    public edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarSourceType getSource() {
        return source;
    }


    /**
     * Sets the source value for this ZipJarDeploymentType.
     * 
     * @param source
     */
    public void setSource(edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarSourceType source) {
        this.source = source;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ZipJarDeploymentType)) return false;
        ZipJarDeploymentType other = (ZipJarDeploymentType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.source==null && other.getSource()==null) || 
             (this.source!=null &&
              this.source.equals(other.getSource())));
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
        if (getSource() != null) {
            _hashCode += getSource().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ZipJarDeploymentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/zip-jar", "ZipJarDeploymentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("source");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/zip-jar", "source"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/zip-jar", "ZipJarSourceType"));
        elemField.setNillable(false);
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
