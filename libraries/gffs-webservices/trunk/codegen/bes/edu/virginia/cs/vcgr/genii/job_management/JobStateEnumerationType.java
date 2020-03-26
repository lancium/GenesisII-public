/**
 * JobStateEnumerationType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class JobStateEnumerationType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected JobStateEnumerationType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _QUEUED = "QUEUED";
    public static final java.lang.String _REQUEUED = "REQUEUED";
    public static final java.lang.String _STARTING = "STARTING";
    public static final java.lang.String _RUNNING = "RUNNING";
    public static final java.lang.String _FINISHED = "FINISHED";
    public static final java.lang.String _ERROR = "ERROR";
    public static final JobStateEnumerationType QUEUED = new JobStateEnumerationType(_QUEUED);
    public static final JobStateEnumerationType REQUEUED = new JobStateEnumerationType(_REQUEUED);
    public static final JobStateEnumerationType STARTING = new JobStateEnumerationType(_STARTING);
    public static final JobStateEnumerationType RUNNING = new JobStateEnumerationType(_RUNNING);
    public static final JobStateEnumerationType FINISHED = new JobStateEnumerationType(_FINISHED);
    public static final JobStateEnumerationType ERROR = new JobStateEnumerationType(_ERROR);
    public java.lang.String getValue() { return _value_;}
    public static JobStateEnumerationType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        JobStateEnumerationType enumeration = (JobStateEnumerationType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static JobStateEnumerationType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(JobStateEnumerationType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "JobStateEnumerationType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
