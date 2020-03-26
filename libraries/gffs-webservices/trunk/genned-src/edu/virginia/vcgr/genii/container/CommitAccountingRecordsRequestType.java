/**
 * CommitAccountingRecordsRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.container;

public class CommitAccountingRecordsRequestType  implements java.io.Serializable {
    private long lastRecordIdToCommit;

    public CommitAccountingRecordsRequestType() {
    }

    public CommitAccountingRecordsRequestType(
           long lastRecordIdToCommit) {
           this.lastRecordIdToCommit = lastRecordIdToCommit;
    }


    /**
     * Gets the lastRecordIdToCommit value for this CommitAccountingRecordsRequestType.
     * 
     * @return lastRecordIdToCommit
     */
    public long getLastRecordIdToCommit() {
        return lastRecordIdToCommit;
    }


    /**
     * Sets the lastRecordIdToCommit value for this CommitAccountingRecordsRequestType.
     * 
     * @param lastRecordIdToCommit
     */
    public void setLastRecordIdToCommit(long lastRecordIdToCommit) {
        this.lastRecordIdToCommit = lastRecordIdToCommit;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CommitAccountingRecordsRequestType)) return false;
        CommitAccountingRecordsRequestType other = (CommitAccountingRecordsRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.lastRecordIdToCommit == other.getLastRecordIdToCommit();
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
        _hashCode += new Long(getLastRecordIdToCommit()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CommitAccountingRecordsRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "CommitAccountingRecordsRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastRecordIdToCommit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "last-record-id-to-commit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
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
