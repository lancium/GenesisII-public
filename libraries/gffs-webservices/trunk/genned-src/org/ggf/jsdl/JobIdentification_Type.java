/**
 * JobIdentification_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class JobIdentification_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private java.lang.String jobName;

    private java.lang.String description;

    private java.lang.String[] jobAnnotation;

    private java.lang.String[] jobProject;

    private org.apache.axis.message.MessageElement [] _any;

    public JobIdentification_Type() {
    }

    public JobIdentification_Type(
           java.lang.String jobName,
           java.lang.String description,
           java.lang.String[] jobAnnotation,
           java.lang.String[] jobProject,
           org.apache.axis.message.MessageElement [] _any) {
           this.jobName = jobName;
           this.description = description;
           this.jobAnnotation = jobAnnotation;
           this.jobProject = jobProject;
           this._any = _any;
    }


    /**
     * Gets the jobName value for this JobIdentification_Type.
     * 
     * @return jobName
     */
    public java.lang.String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this JobIdentification_Type.
     * 
     * @param jobName
     */
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }


    /**
     * Gets the description value for this JobIdentification_Type.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this JobIdentification_Type.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the jobAnnotation value for this JobIdentification_Type.
     * 
     * @return jobAnnotation
     */
    public java.lang.String[] getJobAnnotation() {
        return jobAnnotation;
    }


    /**
     * Sets the jobAnnotation value for this JobIdentification_Type.
     * 
     * @param jobAnnotation
     */
    public void setJobAnnotation(java.lang.String[] jobAnnotation) {
        this.jobAnnotation = jobAnnotation;
    }

    public java.lang.String getJobAnnotation(int i) {
        return this.jobAnnotation[i];
    }

    public void setJobAnnotation(int i, java.lang.String _value) {
        this.jobAnnotation[i] = _value;
    }


    /**
     * Gets the jobProject value for this JobIdentification_Type.
     * 
     * @return jobProject
     */
    public java.lang.String[] getJobProject() {
        return jobProject;
    }


    /**
     * Sets the jobProject value for this JobIdentification_Type.
     * 
     * @param jobProject
     */
    public void setJobProject(java.lang.String[] jobProject) {
        this.jobProject = jobProject;
    }

    public java.lang.String getJobProject(int i) {
        return this.jobProject[i];
    }

    public void setJobProject(int i, java.lang.String _value) {
        this.jobProject[i] = _value;
    }


    /**
     * Gets the _any value for this JobIdentification_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this JobIdentification_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobIdentification_Type)) return false;
        JobIdentification_Type other = (JobIdentification_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.jobName==null && other.getJobName()==null) || 
             (this.jobName!=null &&
              this.jobName.equals(other.getJobName()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.jobAnnotation==null && other.getJobAnnotation()==null) || 
             (this.jobAnnotation!=null &&
              java.util.Arrays.equals(this.jobAnnotation, other.getJobAnnotation()))) &&
            ((this.jobProject==null && other.getJobProject()==null) || 
             (this.jobProject!=null &&
              java.util.Arrays.equals(this.jobProject, other.getJobProject()))) &&
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
        if (getJobName() != null) {
            _hashCode += getJobName().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getJobAnnotation() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getJobAnnotation());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getJobAnnotation(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getJobProject() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getJobProject());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getJobProject(), i);
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
        new org.apache.axis.description.TypeDesc(JobIdentification_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobIdentification_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobAnnotation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobAnnotation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobProject");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobProject"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
