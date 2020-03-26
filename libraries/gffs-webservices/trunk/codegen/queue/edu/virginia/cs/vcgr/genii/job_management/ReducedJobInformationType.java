/**
 * ReducedJobInformationType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class ReducedJobInformationType  implements java.io.Serializable {
    private java.lang.String jobTicket;

    private byte[][] owner;

    private edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType jobStatus;

    public ReducedJobInformationType() {
    }

    public ReducedJobInformationType(
           java.lang.String jobTicket,
           byte[][] owner,
           edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType jobStatus) {
           this.jobTicket = jobTicket;
           this.owner = owner;
           this.jobStatus = jobStatus;
    }


    /**
     * Gets the jobTicket value for this ReducedJobInformationType.
     * 
     * @return jobTicket
     */
    public java.lang.String getJobTicket() {
        return jobTicket;
    }


    /**
     * Sets the jobTicket value for this ReducedJobInformationType.
     * 
     * @param jobTicket
     */
    public void setJobTicket(java.lang.String jobTicket) {
        this.jobTicket = jobTicket;
    }


    /**
     * Gets the owner value for this ReducedJobInformationType.
     * 
     * @return owner
     */
    public byte[][] getOwner() {
        return owner;
    }


    /**
     * Sets the owner value for this ReducedJobInformationType.
     * 
     * @param owner
     */
    public void setOwner(byte[][] owner) {
        this.owner = owner;
    }

    public byte[] getOwner(int i) {
        return this.owner[i];
    }

    public void setOwner(int i, byte[] _value) {
        this.owner[i] = _value;
    }


    /**
     * Gets the jobStatus value for this ReducedJobInformationType.
     * 
     * @return jobStatus
     */
    public edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType getJobStatus() {
        return jobStatus;
    }


    /**
     * Sets the jobStatus value for this ReducedJobInformationType.
     * 
     * @param jobStatus
     */
    public void setJobStatus(edu.virginia.cs.vcgr.genii.job_management.JobStateEnumerationType jobStatus) {
        this.jobStatus = jobStatus;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReducedJobInformationType)) return false;
        ReducedJobInformationType other = (ReducedJobInformationType) obj;
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
              this.jobTicket.equals(other.getJobTicket()))) &&
            ((this.owner==null && other.getOwner()==null) || 
             (this.owner!=null &&
              java.util.Arrays.equals(this.owner, other.getOwner()))) &&
            ((this.jobStatus==null && other.getJobStatus()==null) || 
             (this.jobStatus!=null &&
              this.jobStatus.equals(other.getJobStatus())));
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
        if (getOwner() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOwner());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOwner(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getJobStatus() != null) {
            _hashCode += getJobStatus().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReducedJobInformationType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "ReducedJobInformationType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobTicket");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "job-ticket"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("owner");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "owner"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "job-status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "JobStateEnumerationType"));
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
