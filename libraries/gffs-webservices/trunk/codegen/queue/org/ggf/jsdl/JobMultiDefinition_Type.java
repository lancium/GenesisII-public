/**
 * JobMultiDefinition_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class JobMultiDefinition_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.JobDefinition_Type[] jobDefinition;

    private org.apache.axis.message.MessageElement [] _any;

    public JobMultiDefinition_Type() {
    }

    public JobMultiDefinition_Type(
           org.ggf.jsdl.JobDefinition_Type[] jobDefinition,
           org.apache.axis.message.MessageElement [] _any) {
           this.jobDefinition = jobDefinition;
           this._any = _any;
    }


    /**
     * Gets the jobDefinition value for this JobMultiDefinition_Type.
     * 
     * @return jobDefinition
     */
    public org.ggf.jsdl.JobDefinition_Type[] getJobDefinition() {
        return jobDefinition;
    }


    /**
     * Sets the jobDefinition value for this JobMultiDefinition_Type.
     * 
     * @param jobDefinition
     */
    public void setJobDefinition(org.ggf.jsdl.JobDefinition_Type[] jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    public org.ggf.jsdl.JobDefinition_Type getJobDefinition(int i) {
        return this.jobDefinition[i];
    }

    public void setJobDefinition(int i, org.ggf.jsdl.JobDefinition_Type _value) {
        this.jobDefinition[i] = _value;
    }


    /**
     * Gets the _any value for this JobMultiDefinition_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this JobMultiDefinition_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobMultiDefinition_Type)) return false;
        JobMultiDefinition_Type other = (JobMultiDefinition_Type) obj;
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
              java.util.Arrays.equals(this.jobDefinition, other.getJobDefinition()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any())));
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
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getJobDefinition());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getJobDefinition(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (get_any() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(get_any());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(get_any(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(JobMultiDefinition_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobMultiDefinition_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobDefinition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDefinition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDefinition_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
