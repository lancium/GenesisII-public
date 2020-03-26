/**
 * SubmitJobRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class SubmitJobRequestType  implements java.io.Serializable {
    private org.ggf.jsdl.JobDefinition_Type jobDefinition;

    private byte priority;

    public SubmitJobRequestType() {
    }

    public SubmitJobRequestType(
           org.ggf.jsdl.JobDefinition_Type jobDefinition,
           byte priority) {
           this.jobDefinition = jobDefinition;
           this.priority = priority;
    }


    /**
     * Gets the jobDefinition value for this SubmitJobRequestType.
     * 
     * @return jobDefinition
     */
    public org.ggf.jsdl.JobDefinition_Type getJobDefinition() {
        return jobDefinition;
    }


    /**
     * Sets the jobDefinition value for this SubmitJobRequestType.
     * 
     * @param jobDefinition
     */
    public void setJobDefinition(org.ggf.jsdl.JobDefinition_Type jobDefinition) {
        this.jobDefinition = jobDefinition;
    }


    /**
     * Gets the priority value for this SubmitJobRequestType.
     * 
     * @return priority
     */
    public byte getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this SubmitJobRequestType.
     * 
     * @param priority
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubmitJobRequestType)) return false;
        SubmitJobRequestType other = (SubmitJobRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.jobDefinition==null && other.getJobDefinition()==null) || 
             (this.jobDefinition!=null &&
              this.jobDefinition.equals(other.getJobDefinition()))) &&
            this.priority == other.getPriority();
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
        if (getJobDefinition() != null) {
            _hashCode += getJobDefinition().hashCode();
        }
        _hashCode += getPriority();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubmitJobRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "SubmitJobRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobDefinition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "job-definition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDefinition_Type"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("priority");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "priority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "byte"));
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
