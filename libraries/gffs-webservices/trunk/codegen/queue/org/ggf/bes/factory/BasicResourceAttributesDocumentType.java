/**
 * BasicResourceAttributesDocumentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class BasicResourceAttributesDocumentType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private java.lang.String resourceName;

    private org.ggf.jsdl.OperatingSystem_Type operatingSystem;

    private org.ggf.jsdl.CPUArchitecture_Type CPUArchitecture;

    private org.ggf.jsdl.GPUArchitecture_Type GPUArchitecture;

    private java.lang.Double GPUCountPerNode;

    private java.lang.Double GPUMemoryPerNode;

    private java.lang.Double CPUCount;

    private java.lang.Double CPUSpeed;

    private java.lang.Double physicalMemory;

    private java.lang.Double virtualMemory;

    private org.apache.axis.message.MessageElement [] _any;

    public BasicResourceAttributesDocumentType() {
    }

    public BasicResourceAttributesDocumentType(
           java.lang.String resourceName,
           org.ggf.jsdl.OperatingSystem_Type operatingSystem,
           org.ggf.jsdl.CPUArchitecture_Type CPUArchitecture,
           org.ggf.jsdl.GPUArchitecture_Type GPUArchitecture,
           java.lang.Double GPUCountPerNode,
           java.lang.Double GPUMemoryPerNode,
           java.lang.Double CPUCount,
           java.lang.Double CPUSpeed,
           java.lang.Double physicalMemory,
           java.lang.Double virtualMemory,
           org.apache.axis.message.MessageElement [] _any) {
           this.resourceName = resourceName;
           this.operatingSystem = operatingSystem;
           this.CPUArchitecture = CPUArchitecture;
           this.GPUArchitecture = GPUArchitecture;
           this.GPUCountPerNode = GPUCountPerNode;
           this.GPUMemoryPerNode = GPUMemoryPerNode;
           this.CPUCount = CPUCount;
           this.CPUSpeed = CPUSpeed;
           this.physicalMemory = physicalMemory;
           this.virtualMemory = virtualMemory;
           this._any = _any;
    }


    /**
     * Gets the resourceName value for this BasicResourceAttributesDocumentType.
     * 
     * @return resourceName
     */
    public java.lang.String getResourceName() {
        return resourceName;
    }


    /**
     * Sets the resourceName value for this BasicResourceAttributesDocumentType.
     * 
     * @param resourceName
     */
    public void setResourceName(java.lang.String resourceName) {
        this.resourceName = resourceName;
    }


    /**
     * Gets the operatingSystem value for this BasicResourceAttributesDocumentType.
     * 
     * @return operatingSystem
     */
    public org.ggf.jsdl.OperatingSystem_Type getOperatingSystem() {
        return operatingSystem;
    }


    /**
     * Sets the operatingSystem value for this BasicResourceAttributesDocumentType.
     * 
     * @param operatingSystem
     */
    public void setOperatingSystem(org.ggf.jsdl.OperatingSystem_Type operatingSystem) {
        this.operatingSystem = operatingSystem;
    }


    /**
     * Gets the CPUArchitecture value for this BasicResourceAttributesDocumentType.
     * 
     * @return CPUArchitecture
     */
    public org.ggf.jsdl.CPUArchitecture_Type getCPUArchitecture() {
        return CPUArchitecture;
    }


    /**
     * Sets the CPUArchitecture value for this BasicResourceAttributesDocumentType.
     * 
     * @param CPUArchitecture
     */
    public void setCPUArchitecture(org.ggf.jsdl.CPUArchitecture_Type CPUArchitecture) {
        this.CPUArchitecture = CPUArchitecture;
    }


    /**
     * Gets the GPUArchitecture value for this BasicResourceAttributesDocumentType.
     * 
     * @return GPUArchitecture
     */
    public org.ggf.jsdl.GPUArchitecture_Type getGPUArchitecture() {
        return GPUArchitecture;
    }


    /**
     * Sets the GPUArchitecture value for this BasicResourceAttributesDocumentType.
     * 
     * @param GPUArchitecture
     */
    public void setGPUArchitecture(org.ggf.jsdl.GPUArchitecture_Type GPUArchitecture) {
        this.GPUArchitecture = GPUArchitecture;
    }


    /**
     * Gets the GPUCountPerNode value for this BasicResourceAttributesDocumentType.
     * 
     * @return GPUCountPerNode
     */
    public java.lang.Double getGPUCountPerNode() {
        return GPUCountPerNode;
    }


    /**
     * Sets the GPUCountPerNode value for this BasicResourceAttributesDocumentType.
     * 
     * @param GPUCountPerNode
     */
    public void setGPUCountPerNode(java.lang.Double GPUCountPerNode) {
        this.GPUCountPerNode = GPUCountPerNode;
    }


    /**
     * Gets the GPUMemoryPerNode value for this BasicResourceAttributesDocumentType.
     * 
     * @return GPUMemoryPerNode
     */
    public java.lang.Double getGPUMemoryPerNode() {
        return GPUMemoryPerNode;
    }


    /**
     * Sets the GPUMemoryPerNode value for this BasicResourceAttributesDocumentType.
     * 
     * @param GPUMemoryPerNode
     */
    public void setGPUMemoryPerNode(java.lang.Double GPUMemoryPerNode) {
        this.GPUMemoryPerNode = GPUMemoryPerNode;
    }


    /**
     * Gets the CPUCount value for this BasicResourceAttributesDocumentType.
     * 
     * @return CPUCount
     */
    public java.lang.Double getCPUCount() {
        return CPUCount;
    }


    /**
     * Sets the CPUCount value for this BasicResourceAttributesDocumentType.
     * 
     * @param CPUCount
     */
    public void setCPUCount(java.lang.Double CPUCount) {
        this.CPUCount = CPUCount;
    }


    /**
     * Gets the CPUSpeed value for this BasicResourceAttributesDocumentType.
     * 
     * @return CPUSpeed
     */
    public java.lang.Double getCPUSpeed() {
        return CPUSpeed;
    }


    /**
     * Sets the CPUSpeed value for this BasicResourceAttributesDocumentType.
     * 
     * @param CPUSpeed
     */
    public void setCPUSpeed(java.lang.Double CPUSpeed) {
        this.CPUSpeed = CPUSpeed;
    }


    /**
     * Gets the physicalMemory value for this BasicResourceAttributesDocumentType.
     * 
     * @return physicalMemory
     */
    public java.lang.Double getPhysicalMemory() {
        return physicalMemory;
    }


    /**
     * Sets the physicalMemory value for this BasicResourceAttributesDocumentType.
     * 
     * @param physicalMemory
     */
    public void setPhysicalMemory(java.lang.Double physicalMemory) {
        this.physicalMemory = physicalMemory;
    }


    /**
     * Gets the virtualMemory value for this BasicResourceAttributesDocumentType.
     * 
     * @return virtualMemory
     */
    public java.lang.Double getVirtualMemory() {
        return virtualMemory;
    }


    /**
     * Sets the virtualMemory value for this BasicResourceAttributesDocumentType.
     * 
     * @param virtualMemory
     */
    public void setVirtualMemory(java.lang.Double virtualMemory) {
        this.virtualMemory = virtualMemory;
    }


    /**
     * Gets the _any value for this BasicResourceAttributesDocumentType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this BasicResourceAttributesDocumentType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BasicResourceAttributesDocumentType)) return false;
        BasicResourceAttributesDocumentType other = (BasicResourceAttributesDocumentType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.resourceName==null && other.getResourceName()==null) || 
             (this.resourceName!=null &&
              this.resourceName.equals(other.getResourceName()))) &&
            ((this.operatingSystem==null && other.getOperatingSystem()==null) || 
             (this.operatingSystem!=null &&
              this.operatingSystem.equals(other.getOperatingSystem()))) &&
            ((this.CPUArchitecture==null && other.getCPUArchitecture()==null) || 
             (this.CPUArchitecture!=null &&
              this.CPUArchitecture.equals(other.getCPUArchitecture()))) &&
            ((this.GPUArchitecture==null && other.getGPUArchitecture()==null) || 
             (this.GPUArchitecture!=null &&
              this.GPUArchitecture.equals(other.getGPUArchitecture()))) &&
            ((this.GPUCountPerNode==null && other.getGPUCountPerNode()==null) || 
             (this.GPUCountPerNode!=null &&
              this.GPUCountPerNode.equals(other.getGPUCountPerNode()))) &&
            ((this.GPUMemoryPerNode==null && other.getGPUMemoryPerNode()==null) || 
             (this.GPUMemoryPerNode!=null &&
              this.GPUMemoryPerNode.equals(other.getGPUMemoryPerNode()))) &&
            ((this.CPUCount==null && other.getCPUCount()==null) || 
             (this.CPUCount!=null &&
              this.CPUCount.equals(other.getCPUCount()))) &&
            ((this.CPUSpeed==null && other.getCPUSpeed()==null) || 
             (this.CPUSpeed!=null &&
              this.CPUSpeed.equals(other.getCPUSpeed()))) &&
            ((this.physicalMemory==null && other.getPhysicalMemory()==null) || 
             (this.physicalMemory!=null &&
              this.physicalMemory.equals(other.getPhysicalMemory()))) &&
            ((this.virtualMemory==null && other.getVirtualMemory()==null) || 
             (this.virtualMemory!=null &&
              this.virtualMemory.equals(other.getVirtualMemory()))) &&
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
        if (getResourceName() != null) {
            _hashCode += getResourceName().hashCode();
        }
        if (getOperatingSystem() != null) {
            _hashCode += getOperatingSystem().hashCode();
        }
        if (getCPUArchitecture() != null) {
            _hashCode += getCPUArchitecture().hashCode();
        }
        if (getGPUArchitecture() != null) {
            _hashCode += getGPUArchitecture().hashCode();
        }
        if (getGPUCountPerNode() != null) {
            _hashCode += getGPUCountPerNode().hashCode();
        }
        if (getGPUMemoryPerNode() != null) {
            _hashCode += getGPUMemoryPerNode().hashCode();
        }
        if (getCPUCount() != null) {
            _hashCode += getCPUCount().hashCode();
        }
        if (getCPUSpeed() != null) {
            _hashCode += getCPUSpeed().hashCode();
        }
        if (getPhysicalMemory() != null) {
            _hashCode += getPhysicalMemory().hashCode();
        }
        if (getVirtualMemory() != null) {
            _hashCode += getVirtualMemory().hashCode();
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
        new org.apache.axis.description.TypeDesc(BasicResourceAttributesDocumentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "BasicResourceAttributesDocumentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resourceName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ResourceName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatingSystem");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "OperatingSystem"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystem_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUArchitecture");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CPUArchitecture"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitecture_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUArchitecture");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GPUArchitecture"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitecture_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUCountPerNode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GPUCountPerNode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUMemoryPerNode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GPUMemoryPerNode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CPUCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUSpeed");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CPUSpeed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("physicalMemory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "PhysicalMemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("virtualMemory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "VirtualMemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
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
