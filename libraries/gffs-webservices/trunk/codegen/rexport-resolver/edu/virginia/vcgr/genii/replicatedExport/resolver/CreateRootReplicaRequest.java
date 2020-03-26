/**
 * CreateRootReplicaRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class CreateRootReplicaRequest  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType primary_EPR;

    public CreateRootReplicaRequest() {
    }

    public CreateRootReplicaRequest(
           org.ws.addressing.EndpointReferenceType primary_EPR) {
           this.primary_EPR = primary_EPR;
    }


    /**
     * Gets the primary_EPR value for this CreateRootReplicaRequest.
     * 
     * @return primary_EPR
     */
    public org.ws.addressing.EndpointReferenceType getPrimary_EPR() {
        return primary_EPR;
    }


    /**
     * Sets the primary_EPR value for this CreateRootReplicaRequest.
     * 
     * @param primary_EPR
     */
    public void setPrimary_EPR(org.ws.addressing.EndpointReferenceType primary_EPR) {
        this.primary_EPR = primary_EPR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateRootReplicaRequest)) return false;
        CreateRootReplicaRequest other = (CreateRootReplicaRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.primary_EPR==null && other.getPrimary_EPR()==null) || 
             (this.primary_EPR!=null &&
              this.primary_EPR.equals(other.getPrimary_EPR())));
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
        if (getPrimary_EPR() != null) {
            _hashCode += getPrimary_EPR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateRootReplicaRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "CreateRootReplicaRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("primary_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "primary_EPR"));
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
