/**
 * CreateDeploymentDocumentResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc;

public class CreateDeploymentDocumentResponseType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType newDeploymentDocument;

    public CreateDeploymentDocumentResponseType() {
    }

    public CreateDeploymentDocumentResponseType(
           org.ws.addressing.EndpointReferenceType newDeploymentDocument) {
           this.newDeploymentDocument = newDeploymentDocument;
    }


    /**
     * Gets the newDeploymentDocument value for this CreateDeploymentDocumentResponseType.
     * 
     * @return newDeploymentDocument
     */
    public org.ws.addressing.EndpointReferenceType getNewDeploymentDocument() {
        return newDeploymentDocument;
    }


    /**
     * Sets the newDeploymentDocument value for this CreateDeploymentDocumentResponseType.
     * 
     * @param newDeploymentDocument
     */
    public void setNewDeploymentDocument(org.ws.addressing.EndpointReferenceType newDeploymentDocument) {
        this.newDeploymentDocument = newDeploymentDocument;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateDeploymentDocumentResponseType)) return false;
        CreateDeploymentDocumentResponseType other = (CreateDeploymentDocumentResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.newDeploymentDocument==null && other.getNewDeploymentDocument()==null) || 
             (this.newDeploymentDocument!=null &&
              this.newDeploymentDocument.equals(other.getNewDeploymentDocument())));
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
        if (getNewDeploymentDocument() != null) {
            _hashCode += getNewDeploymentDocument().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateDeploymentDocumentResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "CreateDeploymentDocumentResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newDeploymentDocument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "NewDeploymentDocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
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
