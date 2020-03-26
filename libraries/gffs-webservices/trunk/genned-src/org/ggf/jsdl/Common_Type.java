/**
 * Common_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class Common_Type  implements java.io.Serializable {
    private org.ggf.jsdl.JobIdentification_Type jobIdentification;

    private org.ggf.jsdl.Application_Type application;

    private org.ggf.jsdl.Resources_Type resources;

    private org.ggf.jsdl.DataStaging_Type[] dataStaging;

    public Common_Type() {
    }

    public Common_Type(
           org.ggf.jsdl.JobIdentification_Type jobIdentification,
           org.ggf.jsdl.Application_Type application,
           org.ggf.jsdl.Resources_Type resources,
           org.ggf.jsdl.DataStaging_Type[] dataStaging) {
           this.jobIdentification = jobIdentification;
           this.application = application;
           this.resources = resources;
           this.dataStaging = dataStaging;
    }


    /**
     * Gets the jobIdentification value for this Common_Type.
     * 
     * @return jobIdentification
     */
    public org.ggf.jsdl.JobIdentification_Type getJobIdentification() {
        return jobIdentification;
    }


    /**
     * Sets the jobIdentification value for this Common_Type.
     * 
     * @param jobIdentification
     */
    public void setJobIdentification(org.ggf.jsdl.JobIdentification_Type jobIdentification) {
        this.jobIdentification = jobIdentification;
    }


    /**
     * Gets the application value for this Common_Type.
     * 
     * @return application
     */
    public org.ggf.jsdl.Application_Type getApplication() {
        return application;
    }


    /**
     * Sets the application value for this Common_Type.
     * 
     * @param application
     */
    public void setApplication(org.ggf.jsdl.Application_Type application) {
        this.application = application;
    }


    /**
     * Gets the resources value for this Common_Type.
     * 
     * @return resources
     */
    public org.ggf.jsdl.Resources_Type getResources() {
        return resources;
    }


    /**
     * Sets the resources value for this Common_Type.
     * 
     * @param resources
     */
    public void setResources(org.ggf.jsdl.Resources_Type resources) {
        this.resources = resources;
    }


    /**
     * Gets the dataStaging value for this Common_Type.
     * 
     * @return dataStaging
     */
    public org.ggf.jsdl.DataStaging_Type[] getDataStaging() {
        return dataStaging;
    }


    /**
     * Sets the dataStaging value for this Common_Type.
     * 
     * @param dataStaging
     */
    public void setDataStaging(org.ggf.jsdl.DataStaging_Type[] dataStaging) {
        this.dataStaging = dataStaging;
    }

    public org.ggf.jsdl.DataStaging_Type getDataStaging(int i) {
        return this.dataStaging[i];
    }

    public void setDataStaging(int i, org.ggf.jsdl.DataStaging_Type _value) {
        this.dataStaging[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Common_Type)) return false;
        Common_Type other = (Common_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.jobIdentification==null && other.getJobIdentification()==null) || 
             (this.jobIdentification!=null &&
              this.jobIdentification.equals(other.getJobIdentification()))) &&
            ((this.application==null && other.getApplication()==null) || 
             (this.application!=null &&
              this.application.equals(other.getApplication()))) &&
            ((this.resources==null && other.getResources()==null) || 
             (this.resources!=null &&
              this.resources.equals(other.getResources()))) &&
            ((this.dataStaging==null && other.getDataStaging()==null) || 
             (this.dataStaging!=null &&
              java.util.Arrays.equals(this.dataStaging, other.getDataStaging())));
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
        if (getJobIdentification() != null) {
            _hashCode += getJobIdentification().hashCode();
        }
        if (getApplication() != null) {
            _hashCode += getApplication().hashCode();
        }
        if (getResources() != null) {
            _hashCode += getResources().hashCode();
        }
        if (getDataStaging() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDataStaging());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDataStaging(), i);
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
        new org.apache.axis.description.TypeDesc(Common_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Common_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobIdentification");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobIdentification"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "JobIdentification_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("application");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Application"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Application_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resources");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Resources"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Resources_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataStaging");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "DataStaging"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "DataStaging_Type"));
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
