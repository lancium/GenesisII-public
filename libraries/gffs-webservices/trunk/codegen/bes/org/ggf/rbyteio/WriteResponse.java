/**
 * WriteResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rbyteio;

public class WriteResponse  implements java.io.Serializable {
    private org.ggf.byteio.TransferInformationType transferInformation;

    public WriteResponse() {
    }

    public WriteResponse(
           org.ggf.byteio.TransferInformationType transferInformation) {
           this.transferInformation = transferInformation;
    }


    /**
     * Gets the transferInformation value for this WriteResponse.
     * 
     * @return transferInformation
     */
    public org.ggf.byteio.TransferInformationType getTransferInformation() {
        return transferInformation;
    }


    /**
     * Sets the transferInformation value for this WriteResponse.
     * 
     * @param transferInformation
     */
    public void setTransferInformation(org.ggf.byteio.TransferInformationType transferInformation) {
        this.transferInformation = transferInformation;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WriteResponse)) return false;
        WriteResponse other = (WriteResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.transferInformation==null && other.getTransferInformation()==null) || 
             (this.transferInformation!=null &&
              this.transferInformation.equals(other.getTransferInformation())));
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
        if (getTransferInformation() != null) {
            _hashCode += getTransferInformation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WriteResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", ">writeResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transferInformation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "transfer-information"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/byte-io", "transfer-information-type"));
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
