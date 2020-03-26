/**
 * DeploymentExistsFaultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc;

public class DeploymentExistsFaultType  extends org.oasis_open.wsrf.basefaults.BaseFaultType  implements java.io.Serializable {
    private java.lang.String name;

    private edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType existingDeployment;

    public DeploymentExistsFaultType() {
    }

    public DeploymentExistsFaultType(
           org.apache.axis.message.MessageElement [] _any,
           java.util.Calendar timestamp,
           org.ws.addressing.EndpointReferenceType originator,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode errorCode,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription[] description,
           org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause faultCause,
           java.lang.String name,
           edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType existingDeployment) {
        super(
            _any,
            timestamp,
            originator,
            errorCode,
            description,
            faultCause);
        this.name = name;
        this.existingDeployment = existingDeployment;
    }


    /**
     * Gets the name value for this DeploymentExistsFaultType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this DeploymentExistsFaultType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the existingDeployment value for this DeploymentExistsFaultType.
     * 
     * @return existingDeployment
     */
    public edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType getExistingDeployment() {
        return existingDeployment;
    }


    /**
     * Sets the existingDeployment value for this DeploymentExistsFaultType.
     * 
     * @param existingDeployment
     */
    public void setExistingDeployment(edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType existingDeployment) {
        this.existingDeployment = existingDeployment;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeploymentExistsFaultType)) return false;
        DeploymentExistsFaultType other = (DeploymentExistsFaultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.existingDeployment==null && other.getExistingDeployment()==null) || 
             (this.existingDeployment!=null &&
              this.existingDeployment.equals(other.getExistingDeployment())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getExistingDeployment() != null) {
            _hashCode += getExistingDeployment().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DeploymentExistsFaultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "DeploymentExistsFaultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("existingDeployment");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "ExistingDeployment"));
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


    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
