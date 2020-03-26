/**
 * Read.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rbyteio;

public class Read  implements java.io.Serializable {
    private long startOffset;

    private int bytesPerBlock;

    private int numBlocks;

    private long stride;

    private org.ggf.byteio.TransferInformationType transferInformation;

    public Read() {
    }

    public Read(
           long startOffset,
           int bytesPerBlock,
           int numBlocks,
           long stride,
           org.ggf.byteio.TransferInformationType transferInformation) {
           this.startOffset = startOffset;
           this.bytesPerBlock = bytesPerBlock;
           this.numBlocks = numBlocks;
           this.stride = stride;
           this.transferInformation = transferInformation;
    }


    /**
     * Gets the startOffset value for this Read.
     * 
     * @return startOffset
     */
    public long getStartOffset() {
        return startOffset;
    }


    /**
     * Sets the startOffset value for this Read.
     * 
     * @param startOffset
     */
    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }


    /**
     * Gets the bytesPerBlock value for this Read.
     * 
     * @return bytesPerBlock
     */
    public int getBytesPerBlock() {
        return bytesPerBlock;
    }


    /**
     * Sets the bytesPerBlock value for this Read.
     * 
     * @param bytesPerBlock
     */
    public void setBytesPerBlock(int bytesPerBlock) {
        this.bytesPerBlock = bytesPerBlock;
    }


    /**
     * Gets the numBlocks value for this Read.
     * 
     * @return numBlocks
     */
    public int getNumBlocks() {
        return numBlocks;
    }


    /**
     * Sets the numBlocks value for this Read.
     * 
     * @param numBlocks
     */
    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }


    /**
     * Gets the stride value for this Read.
     * 
     * @return stride
     */
    public long getStride() {
        return stride;
    }


    /**
     * Sets the stride value for this Read.
     * 
     * @param stride
     */
    public void setStride(long stride) {
        this.stride = stride;
    }


    /**
     * Gets the transferInformation value for this Read.
     * 
     * @return transferInformation
     */
    public org.ggf.byteio.TransferInformationType getTransferInformation() {
        return transferInformation;
    }


    /**
     * Sets the transferInformation value for this Read.
     * 
     * @param transferInformation
     */
    public void setTransferInformation(org.ggf.byteio.TransferInformationType transferInformation) {
        this.transferInformation = transferInformation;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Read)) return false;
        Read other = (Read) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.startOffset == other.getStartOffset() &&
            this.bytesPerBlock == other.getBytesPerBlock() &&
            this.numBlocks == other.getNumBlocks() &&
            this.stride == other.getStride() &&
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
        _hashCode += new Long(getStartOffset()).hashCode();
        _hashCode += getBytesPerBlock();
        _hashCode += getNumBlocks();
        _hashCode += new Long(getStride()).hashCode();
        if (getTransferInformation() != null) {
            _hashCode += getTransferInformation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Read.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", ">read"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startOffset");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "start-offset"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bytesPerBlock");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "bytes-per-block"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numBlocks");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "num-blocks"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stride");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "stride"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
