/**
 * CreateDeploymentDocumentRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc;

public class CreateDeploymentDocumentRequestType  implements java.io.Serializable {
    private java.lang.String name;

    private edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType deploymentDocument;

    public CreateDeploymentDocumentRequestType() {
    }

    public CreateDeploymentDocumentRequestType(
           java.lang.String name,
           edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType deploymentDocument) {
           this.name = name;
           this.deploymentDocument = deploymentDocument;
    }


    /**
     * Gets the name value for this CreateDeploymentDocumentRequestType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this CreateDeploymentDocumentRequestType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the deploymentDocument value for this CreateDeploymentDocumentRequestType.
     * 
     * @return deploymentDocument
     */
    public edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType getDeploymentDocument() {
        return deploymentDocument;
    }


    /**
     * Sets the deploymentDocument value for this CreateDeploymentDocumentRequestType.
     * 
     * @param deploymentDocument
     */
    public void setDeploymentDocument(edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType deploymentDocument) {
        this.deploymentDocument = deploymentDocument;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateDeploymentDocumentRequestType)) return false;
        CreateDeploymentDocumentRequestType other = (CreateDeploymentDocumentRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.deploymentDocument==null && other.getDeploymentDocument()==null) || 
             (this.deploymentDocument!=null &&
              this.deploymentDocument.equals(other.getDeploymentDocument())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDeploymentDocument() != null) {
            _hashCode += getDeploymentDocument().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateDeploymentDocumentRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "CreateDeploymentDocumentRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deploymentDocument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "DeploymentDocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "DeploymentDocumentType"));
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
