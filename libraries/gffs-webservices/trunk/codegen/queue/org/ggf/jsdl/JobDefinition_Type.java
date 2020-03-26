/**
 * JobDefinition_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class JobDefinition_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.Common_Type common;

    private org.ggf.jsdl.JobDescription_Type[] jobDescription;

    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.Id id;  // attribute

    public JobDefinition_Type() {
    }

    public JobDefinition_Type(
           org.ggf.jsdl.Common_Type common,
           org.ggf.jsdl.JobDescription_Type[] jobDescription,
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.Id id) {
           this.common = common;
           this.jobDescription = jobDescription;
           this._any = _any;
           this.id = id;
    }


    /**
     * Gets the common value for this JobDefinition_Type.
     * 
     * @return common
     */
    public org.ggf.jsdl.Common_Type getCommon() {
        return common;
    }


    /**
     * Sets the common value for this JobDefinition_Type.
     * 
     * @param common
     */
    public void setCommon(org.ggf.jsdl.Common_Type common) {
        this.common = common;
    }


    /**
     * Gets the jobDescription value for this JobDefinition_Type.
     * 
     * @return jobDescription
     */
    public org.ggf.jsdl.JobDescription_Type[] getJobDescription() {
        return jobDescription;
    }


    /**
     * Sets the jobDescription value for this JobDefinition_Type.
     * 
     * @param jobDescription
     */
    public void setJobDescription(org.ggf.jsdl.JobDescription_Type[] jobDescription) {
        this.jobDescription = jobDescription;
    }

    public org.ggf.jsdl.JobDescription_Type getJobDescription(int i) {
        return this.jobDescription[i];
    }

    public void setJobDescription(int i, org.ggf.jsdl.JobDescription_Type _value) {
        this.jobDescription[i] = _value;
    }


    /**
     * Gets the _any value for this JobDefinition_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this JobDefinition_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the id value for this JobDefinition_Type.
     * 
     * @return id
     */
    public org.apache.axis.types.Id getId() {
        return id;
    }


    /**
     * Sets the id value for this JobDefinition_Type.
     * 
     * @param id
     */
    public void setId(org.apache.axis.types.Id id) {
        this.id = id;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobDefinition_Type)) return false;
        JobDefinition_Type other = (JobDefinition_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.common==null && other.getCommon()==null) || 
             (this.common!=null &&
              this.common.equals(other.getCommon()))) &&
            ((this.jobDescription==null && other.getJobDescription()==null) || 
             (this.jobDescription!=null &&
              java.util.Arrays.equals(this.jobDescription, other.getJobDescription()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId())));
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
        if (getCommon() != null) {
            _hashCode += getCommon().hashCode();
        }
        if (getJobDescription() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getJobDescription());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getJobDescription(), i);
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(JobDefinition_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDefinition_Type"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "ID"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("common");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Common"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Common_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobDescription_Type"));
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
