/**
 * PopulateDirRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport;

public class PopulateDirRequestType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType replica_EPR;

    private java.lang.String replica_name;

    private java.lang.String replica_type;

    public PopulateDirRequestType() {
    }

    public PopulateDirRequestType(
           org.ws.addressing.EndpointReferenceType replica_EPR,
           java.lang.String replica_name,
           java.lang.String replica_type) {
           this.replica_EPR = replica_EPR;
           this.replica_name = replica_name;
           this.replica_type = replica_type;
    }


    /**
     * Gets the replica_EPR value for this PopulateDirRequestType.
     * 
     * @return replica_EPR
     */
    public org.ws.addressing.EndpointReferenceType getReplica_EPR() {
        return replica_EPR;
    }


    /**
     * Sets the replica_EPR value for this PopulateDirRequestType.
     * 
     * @param replica_EPR
     */
    public void setReplica_EPR(org.ws.addressing.EndpointReferenceType replica_EPR) {
        this.replica_EPR = replica_EPR;
    }


    /**
     * Gets the replica_name value for this PopulateDirRequestType.
     * 
     * @return replica_name
     */
    public java.lang.String getReplica_name() {
        return replica_name;
    }


    /**
     * Sets the replica_name value for this PopulateDirRequestType.
     * 
     * @param replica_name
     */
    public void setReplica_name(java.lang.String replica_name) {
        this.replica_name = replica_name;
    }


    /**
     * Gets the replica_type value for this PopulateDirRequestType.
     * 
     * @return replica_type
     */
    public java.lang.String getReplica_type() {
        return replica_type;
    }


    /**
     * Sets the replica_type value for this PopulateDirRequestType.
     * 
     * @param replica_type
     */
    public void setReplica_type(java.lang.String replica_type) {
        this.replica_type = replica_type;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PopulateDirRequestType)) return false;
        PopulateDirRequestType other = (PopulateDirRequestType) obj;
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
              this.replica_EPR.equals(other.getReplica_EPR()))) &&
            ((this.replica_name==null && other.getReplica_name()==null) || 
             (this.replica_name!=null &&
              this.replica_name.equals(other.getReplica_name()))) &&
            ((this.replica_type==null && other.getReplica_type()==null) || 
             (this.replica_type!=null &&
              this.replica_type.equals(other.getReplica_type())));
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
        if (getReplica_name() != null) {
            _hashCode += getReplica_name().hashCode();
        }
        if (getReplica_type() != null) {
            _hashCode += getReplica_type().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PopulateDirRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/rexport-dir", "PopulateDirRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replica_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/rexport-dir", "replica_EPR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replica_name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/rexport-dir", "replica_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replica_type");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/rexport-dir", "replica_type"));
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
