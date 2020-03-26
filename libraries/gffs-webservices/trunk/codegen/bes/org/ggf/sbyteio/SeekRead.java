/**
 * SeekRead.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.sbyteio;

public class SeekRead  implements java.io.Serializable {
    private long offset;

    private org.apache.axis.types.URI seekOrigin;

    private org.apache.axis.types.UnsignedInt numBytes;

    private org.ggf.byteio.TransferInformationType transferInformation;

    public SeekRead() {
    }

    public SeekRead(
           long offset,
           org.apache.axis.types.URI seekOrigin,
           org.apache.axis.types.UnsignedInt numBytes,
           org.ggf.byteio.TransferInformationType transferInformation) {
           this.offset = offset;
           this.seekOrigin = seekOrigin;
           this.numBytes = numBytes;
           this.transferInformation = transferInformation;
    }


    /**
     * Gets the offset value for this SeekRead.
     * 
     * @return offset
     */
    public long getOffset() {
        return offset;
    }


    /**
     * Sets the offset value for this SeekRead.
     * 
     * @param offset
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }


    /**
     * Gets the seekOrigin value for this SeekRead.
     * 
     * @return seekOrigin
     */
    public org.apache.axis.types.URI getSeekOrigin() {
        return seekOrigin;
    }


    /**
     * Sets the seekOrigin value for this SeekRead.
     * 
     * @param seekOrigin
     */
    public void setSeekOrigin(org.apache.axis.types.URI seekOrigin) {
        this.seekOrigin = seekOrigin;
    }


    /**
     * Gets the numBytes value for this SeekRead.
     * 
     * @return numBytes
     */
    public org.apache.axis.types.UnsignedInt getNumBytes() {
        return numBytes;
    }


    /**
     * Sets the numBytes value for this SeekRead.
     * 
     * @param numBytes
     */
    public void setNumBytes(org.apache.axis.types.UnsignedInt numBytes) {
        this.numBytes = numBytes;
    }


    /**
     * Gets the transferInformation value for this SeekRead.
     * 
     * @return transferInformation
     */
    public org.ggf.byteio.TransferInformationType getTransferInformation() {
        return transferInformation;
    }


    /**
     * Sets the transferInformation value for this SeekRead.
     * 
     * @param transferInformation
     */
    public void setTransferInformation(org.ggf.byteio.TransferInformationType transferInformation) {
        this.transferInformation = transferInformation;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SeekRead)) return false;
        SeekRead other = (SeekRead) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.offset == other.getOffset() &&
            ((this.seekOrigin==null && other.getSeekOrigin()==null) || 
             (this.seekOrigin!=null &&
              this.seekOrigin.equals(other.getSeekOrigin()))) &&
            ((this.numBytes==null && other.getNumBytes()==null) || 
             (this.numBytes!=null &&
              this.numBytes.equals(other.getNumBytes()))) &&
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
        _hashCode += new Long(getOffset()).hashCode();
        if (getSeekOrigin() != null) {
            _hashCode += getSeekOrigin().hashCode();
        }
        if (getNumBytes() != null) {
            _hashCode += getNumBytes().hashCode();
        }
        if (getTransferInformation() != null) {
            _hashCode += getTransferInformation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SeekRead.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", ">seekRead"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("offset");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", "offset"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("seekOrigin");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", "seek-origin"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numBytes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", "num-bytes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transferInformation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", "transfer-information"));
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
