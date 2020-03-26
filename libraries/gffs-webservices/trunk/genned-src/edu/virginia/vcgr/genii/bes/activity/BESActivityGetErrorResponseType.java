/**
 * BESActivityGetErrorResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.bes.activity;

public class BESActivityGetErrorResponseType  implements java.io.Serializable {
    private byte[] serializedFault;

    public BESActivityGetErrorResponseType() {
    }

    public BESActivityGetErrorResponseType(
           byte[] serializedFault) {
           this.serializedFault = serializedFault;
    }


    /**
     * Gets the serializedFault value for this BESActivityGetErrorResponseType.
     * 
     * @return serializedFault
     */
    public byte[] getSerializedFault() {
        return serializedFault;
    }


    /**
     * Sets the serializedFault value for this BESActivityGetErrorResponseType.
     * 
     * @param serializedFault
     */
    public void setSerializedFault(byte[] serializedFault) {
        this.serializedFault = serializedFault;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BESActivityGetErrorResponseType)) return false;
        BESActivityGetErrorResponseType other = (BESActivityGetErrorResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.serializedFault==null && other.getSerializedFault()==null) || 
             (this.serializedFault!=null &&
              java.util.Arrays.equals(this.serializedFault, other.getSerializedFault())));
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
        if (getSerializedFault() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSerializedFault());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSerializedFault(), i);
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
        new org.apache.axis.description.TypeDesc(BESActivityGetErrorResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/bes/2006/06/bes-activity", "BESActivityGetErrorResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serializedFault");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/bes/2006/06/bes-activity", "SerializedFault"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
