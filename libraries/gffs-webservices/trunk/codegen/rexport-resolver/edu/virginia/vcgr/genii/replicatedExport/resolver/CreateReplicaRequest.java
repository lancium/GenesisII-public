/**
 * CreateReplicaRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class CreateReplicaRequest  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType primary_EPR;

    private org.ws.addressing.EndpointReferenceType dataStream_EPR;

    private java.lang.String export_type;

    private java.lang.String replica_name;

    public CreateReplicaRequest() {
    }

    public CreateReplicaRequest(
           org.ws.addressing.EndpointReferenceType primary_EPR,
           org.ws.addressing.EndpointReferenceType dataStream_EPR,
           java.lang.String export_type,
           java.lang.String replica_name) {
           this.primary_EPR = primary_EPR;
           this.dataStream_EPR = dataStream_EPR;
           this.export_type = export_type;
           this.replica_name = replica_name;
    }


    /**
     * Gets the primary_EPR value for this CreateReplicaRequest.
     * 
     * @return primary_EPR
     */
    public org.ws.addressing.EndpointReferenceType getPrimary_EPR() {
        return primary_EPR;
    }


    /**
     * Sets the primary_EPR value for this CreateReplicaRequest.
     * 
     * @param primary_EPR
     */
    public void setPrimary_EPR(org.ws.addressing.EndpointReferenceType primary_EPR) {
        this.primary_EPR = primary_EPR;
    }


    /**
     * Gets the dataStream_EPR value for this CreateReplicaRequest.
     * 
     * @return dataStream_EPR
     */
    public org.ws.addressing.EndpointReferenceType getDataStream_EPR() {
        return dataStream_EPR;
    }


    /**
     * Sets the dataStream_EPR value for this CreateReplicaRequest.
     * 
     * @param dataStream_EPR
     */
    public void setDataStream_EPR(org.ws.addressing.EndpointReferenceType dataStream_EPR) {
        this.dataStream_EPR = dataStream_EPR;
    }


    /**
     * Gets the export_type value for this CreateReplicaRequest.
     * 
     * @return export_type
     */
    public java.lang.String getExport_type() {
        return export_type;
    }


    /**
     * Sets the export_type value for this CreateReplicaRequest.
     * 
     * @param export_type
     */
    public void setExport_type(java.lang.String export_type) {
        this.export_type = export_type;
    }


    /**
     * Gets the replica_name value for this CreateReplicaRequest.
     * 
     * @return replica_name
     */
    public java.lang.String getReplica_name() {
        return replica_name;
    }


    /**
     * Sets the replica_name value for this CreateReplicaRequest.
     * 
     * @param replica_name
     */
    public void setReplica_name(java.lang.String replica_name) {
        this.replica_name = replica_name;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateReplicaRequest)) return false;
        CreateReplicaRequest other = (CreateReplicaRequest) obj;
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
              this.primary_EPR.equals(other.getPrimary_EPR()))) &&
            ((this.dataStream_EPR==null && other.getDataStream_EPR()==null) || 
             (this.dataStream_EPR!=null &&
              this.dataStream_EPR.equals(other.getDataStream_EPR()))) &&
            ((this.export_type==null && other.getExport_type()==null) || 
             (this.export_type!=null &&
              this.export_type.equals(other.getExport_type()))) &&
            ((this.replica_name==null && other.getReplica_name()==null) || 
             (this.replica_name!=null &&
              this.replica_name.equals(other.getReplica_name())));
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
        if (getDataStream_EPR() != null) {
            _hashCode += getDataStream_EPR().hashCode();
        }
        if (getExport_type() != null) {
            _hashCode += getExport_type().hashCode();
        }
        if (getReplica_name() != null) {
            _hashCode += getReplica_name().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateReplicaRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "CreateReplicaRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("primary_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "primary_EPR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataStream_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "dataStream_EPR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("export_type");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "export_type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replica_name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "replica_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
