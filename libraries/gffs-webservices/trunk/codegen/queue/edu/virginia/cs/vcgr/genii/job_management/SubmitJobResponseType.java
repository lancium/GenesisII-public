/**
 * SubmitJobResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class SubmitJobResponseType  implements java.io.Serializable {
    private java.lang.String jobTicket;

    public SubmitJobResponseType() {
    }

    public SubmitJobResponseType(
           java.lang.String jobTicket) {
           this.jobTicket = jobTicket;
    }


    /**
     * Gets the jobTicket value for this SubmitJobResponseType.
     * 
     * @return jobTicket
     */
    public java.lang.String getJobTicket() {
        return jobTicket;
    }


    /**
     * Sets the jobTicket value for this SubmitJobResponseType.
     * 
     * @param jobTicket
     */
    public void setJobTicket(java.lang.String jobTicket) {
        this.jobTicket = jobTicket;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubmitJobResponseType)) return false;
        SubmitJobResponseType other = (SubmitJobResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.jobTicket==null && other.getJobTicket()==null) || 
             (this.jobTicket!=null &&
              this.jobTicket.equals(other.getJobTicket())));
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
        if (getJobTicket() != null) {
            _hashCode += getJobTicket().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubmitJobResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "SubmitJobResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobTicket");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "job-ticket"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
