/**
 * CreateReplicaResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class CreateReplicaResponse  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType replica_EPR;

    public CreateReplicaResponse() {
    }

    public CreateReplicaResponse(
           org.ws.addressing.EndpointReferenceType replica_EPR) {
           this.replica_EPR = replica_EPR;
    }


    /**
     * Gets the replica_EPR value for this CreateReplicaResponse.
     * 
     * @return replica_EPR
     */
    public org.ws.addressing.EndpointReferenceType getReplica_EPR() {
        return replica_EPR;
    }


    /**
     * Sets the replica_EPR value for this CreateReplicaResponse.
     * 
     * @param replica_EPR
     */
    public void setReplica_EPR(org.ws.addressing.EndpointReferenceType replica_EPR) {
        this.replica_EPR = replica_EPR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateReplicaResponse)) return false;
        CreateReplicaResponse other = (CreateReplicaResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.replica_EPR==null && other.getReplica_EPR()==null) || 
             (this.replica_EPR!=null &&
              this.replica_EPR.equals(other.getReplica_EPR())));
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
        if (getReplica_EPR() != null) {
            _hashCode += getReplica_EPR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateReplicaResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "CreateReplicaResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replica_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "replica_EPR"));
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
