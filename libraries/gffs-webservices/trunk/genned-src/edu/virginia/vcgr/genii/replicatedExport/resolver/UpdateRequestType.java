/**
 * UpdateRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class UpdateRequestType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType new_EPR;

    public UpdateRequestType() {
    }

    public UpdateRequestType(
           org.ws.addressing.EndpointReferenceType new_EPR) {
           this.new_EPR = new_EPR;
    }


    /**
     * Gets the new_EPR value for this UpdateRequestType.
     * 
     * @return new_EPR
     */
    public org.ws.addressing.EndpointReferenceType getNew_EPR() {
        return new_EPR;
    }


    /**
     * Sets the new_EPR value for this UpdateRequestType.
     * 
     * @param new_EPR
     */
    public void setNew_EPR(org.ws.addressing.EndpointReferenceType new_EPR) {
        this.new_EPR = new_EPR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateRequestType)) return false;
        UpdateRequestType other = (UpdateRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.new_EPR==null && other.getNew_EPR()==null) || 
             (this.new_EPR!=null &&
              this.new_EPR.equals(other.getNew_EPR())));
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
        if (getNew_EPR() != null) {
            _hashCode += getNew_EPR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "UpdateRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("new_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "new_EPR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
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
