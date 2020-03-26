/**
 * ConfigureRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class ConfigureRequestType  implements java.io.Serializable {
    private java.lang.String queueResource;

    private org.apache.axis.types.UnsignedInt numSlots;

    private org.apache.axis.types.UnsignedInt numCores;

    public ConfigureRequestType() {
    }

    public ConfigureRequestType(
           java.lang.String queueResource,
           org.apache.axis.types.UnsignedInt numSlots,
           org.apache.axis.types.UnsignedInt numCores) {
           this.queueResource = queueResource;
           this.numSlots = numSlots;
           this.numCores = numCores;
    }


    /**
     * Gets the queueResource value for this ConfigureRequestType.
     * 
     * @return queueResource
     */
    public java.lang.String getQueueResource() {
        return queueResource;
    }


    /**
     * Sets the queueResource value for this ConfigureRequestType.
     * 
     * @param queueResource
     */
    public void setQueueResource(java.lang.String queueResource) {
        this.queueResource = queueResource;
    }


    /**
     * Gets the numSlots value for this ConfigureRequestType.
     * 
     * @return numSlots
     */
    public org.apache.axis.types.UnsignedInt getNumSlots() {
        return numSlots;
    }


    /**
     * Sets the numSlots value for this ConfigureRequestType.
     * 
     * @param numSlots
     */
    public void setNumSlots(org.apache.axis.types.UnsignedInt numSlots) {
        this.numSlots = numSlots;
    }


    /**
     * Gets the numCores value for this ConfigureRequestType.
     * 
     * @return numCores
     */
    public org.apache.axis.types.UnsignedInt getNumCores() {
        return numCores;
    }


    /**
     * Sets the numCores value for this ConfigureRequestType.
     * 
     * @param numCores
     */
    public void setNumCores(org.apache.axis.types.UnsignedInt numCores) {
        this.numCores = numCores;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ConfigureRequestType)) return false;
        ConfigureRequestType other = (ConfigureRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.queueResource==null && other.getQueueResource()==null) || 
             (this.queueResource!=null &&
              this.queueResource.equals(other.getQueueResource()))) &&
            ((this.numSlots==null && other.getNumSlots()==null) || 
             (this.numSlots!=null &&
              this.numSlots.equals(other.getNumSlots()))) &&
            ((this.numCores==null && other.getNumCores()==null) || 
             (this.numCores!=null &&
              this.numCores.equals(other.getNumCores())));
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
        if (getQueueResource() != null) {
            _hashCode += getQueueResource().hashCode();
        }
        if (getNumSlots() != null) {
            _hashCode += getNumSlots().hashCode();
        }
        if (getNumCores() != null) {
            _hashCode += getNumCores().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ConfigureRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "ConfigureRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("queueResource");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "queue-resource"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numSlots");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "num-slots"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numCores");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "num-cores"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
