/**
 * JobInformationType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class JobInformationType  extends edu.virginia.cs.vcgr.genii.job_management.ReducedJobInformationType  implements java.io.Serializable {
    private byte priority;

    private java.util.Calendar submitTime;

    private java.util.Calendar startTime;

    private java.util.Calendar finishTime;

    private org.apache.axis.types.UnsignedShort attempts;

    private java.lang.String scheduledOn;

    private org.ggf.bes.factory.ActivityStatusType besStatus;

    private java.lang.String jobName;  // attribute

    public JobInformationType() {
    }

    public JobInformationType(
           java.lang.String jobTicket,
           byte[][] owner,
           edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType jobStatus,
           byte priority,
           java.util.Calendar submitTime,
           java.util.Calendar startTime,
           java.util.Calendar finishTime,
           org.apache.axis.types.UnsignedShort attempts,
           java.lang.String scheduledOn,
           org.ggf.bes.factory.ActivityStatusType besStatus,
           java.lang.String jobName) {
        super(
            jobTicket,
            owner,
            jobStatus);
        this.priority = priority;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.attempts = attempts;
        this.scheduledOn = scheduledOn;
        this.besStatus = besStatus;
        this.jobName = jobName;
    }


    /**
     * Gets the priority value for this JobInformationType.
     * 
     * @return priority
     */
    public byte getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this JobInformationType.
     * 
     * @param priority
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }


    /**
     * Gets the submitTime value for this JobInformationType.
     * 
     * @return submitTime
     */
    public java.util.Calendar getSubmitTime() {
        return submitTime;
    }


    /**
     * Sets the submitTime value for this JobInformationType.
     * 
     * @param submitTime
     */
    public void setSubmitTime(java.util.Calendar submitTime) {
        this.submitTime = submitTime;
    }


    /**
     * Gets the startTime value for this JobInformationType.
     * 
     * @return startTime
     */
    public java.util.Calendar getStartTime() {
        return startTime;
    }


    /**
     * Sets the startTime value for this JobInformationType.
     * 
     * @param startTime
     */
    public void setStartTime(java.util.Calendar startTime) {
        this.startTime = startTime;
    }


    /**
     * Gets the finishTime value for this JobInformationType.
     * 
     * @return finishTime
     */
    public java.util.Calendar getFinishTime() {
        return finishTime;
    }


    /**
     * Sets the finishTime value for this JobInformationType.
     * 
     * @param finishTime
     */
    public void setFinishTime(java.util.Calendar finishTime) {
        this.finishTime = finishTime;
    }


    /**
     * Gets the attempts value for this JobInformationType.
     * 
     * @return attempts
     */
    public org.apache.axis.types.UnsignedShort getAttempts() {
        return attempts;
    }


    /**
     * Sets the attempts value for this JobInformationType.
     * 
     * @param attempts
     */
    public void setAttempts(org.apache.axis.types.UnsignedShort attempts) {
        this.attempts = attempts;
    }


    /**
     * Gets the scheduledOn value for this JobInformationType.
     * 
     * @return scheduledOn
     */
    public java.lang.String getScheduledOn() {
        return scheduledOn;
    }


    /**
     * Sets the scheduledOn value for this JobInformationType.
     * 
     * @param scheduledOn
     */
    public void setScheduledOn(java.lang.String scheduledOn) {
        this.scheduledOn = scheduledOn;
    }


    /**
     * Gets the besStatus value for this JobInformationType.
     * 
     * @return besStatus
     */
    public org.ggf.bes.factory.ActivityStatusType getBesStatus() {
        return besStatus;
    }


    /**
     * Sets the besStatus value for this JobInformationType.
     * 
     * @param besStatus
     */
    public void setBesStatus(org.ggf.bes.factory.ActivityStatusType besStatus) {
        this.besStatus = besStatus;
    }


    /**
     * Gets the jobName value for this JobInformationType.
     * 
     * @return jobName
     */
    public java.lang.String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this JobInformationType.
     * 
     * @param jobName
     */
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobInformationType)) return false;
        JobInformationType other = (JobInformationType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.priority == other.getPriority() &&
            ((this.submitTime==null && other.getSubmitTime()==null) || 
             (this.submitTime!=null &&
              this.submitTime.equals(other.getSubmitTime()))) &&
            ((this.startTime==null && other.getStartTime()==null) || 
             (this.startTime!=null &&
              this.startTime.equals(other.getStartTime()))) &&
            ((this.finishTime==null && other.getFinishTime()==null) || 
             (this.finishTime!=null &&
              this.finishTime.equals(other.getFinishTime()))) &&
            ((this.attempts==null && other.getAttempts()==null) || 
             (this.attempts!=null &&
              this.attempts.equals(other.getAttempts()))) &&
            ((this.scheduledOn==null && other.getScheduledOn()==null) || 
             (this.scheduledOn!=null &&
              this.scheduledOn.equals(other.getScheduledOn()))) &&
            ((this.besStatus==null && other.getBesStatus()==null) || 
             (this.besStatus!=null &&
              this.besStatus.equals(other.getBesStatus()))) &&
            ((this.jobName==null && other.getJobName()==null) || 
             (this.jobName!=null &&
              this.jobName.equals(other.getJobName())));
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
        _hashCode += getPriority();
        if (getSubmitTime() != null) {
            _hashCode += getSubmitTime().hashCode();
        }
        if (getStartTime() != null) {
            _hashCode += getStartTime().hashCode();
        }
        if (getFinishTime() != null) {
            _hashCode += getFinishTime().hashCode();
        }
        if (getAttempts() != null) {
            _hashCode += getAttempts().hashCode();
        }
        if (getScheduledOn() != null) {
            _hashCode += getScheduledOn().hashCode();
        }
        if (getBesStatus() != null) {
            _hashCode += getBesStatus().hashCode();
        }
        if (getJobName() != null) {
            _hashCode += getJobName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(JobInformationType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "JobInformationType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("jobName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "job-name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("priority");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "priority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "byte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("submitTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "submit-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "start-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("finishTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "finish-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attempts");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "attempts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scheduledOn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "scheduled-on"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("besStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "bes-status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityStatusType"));
        elemField.setMinOccurs(0);
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
