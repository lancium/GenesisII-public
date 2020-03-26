/**
 * PlatformDescriptionType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc;

public class PlatformDescriptionType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.CPUArchitecture_Type[] CPUArchitecture;

    private org.ggf.jsdl.GPUArchitecture_Type[] GPUArchitecture;

    private org.ggf.jsdl.OperatingSystem_Type[] operatingSystem;

    private org.apache.axis.message.MessageElement [] _any;

    public PlatformDescriptionType() {
    }

    public PlatformDescriptionType(
           org.ggf.jsdl.CPUArchitecture_Type[] CPUArchitecture,
           org.ggf.jsdl.GPUArchitecture_Type[] GPUArchitecture,
           org.ggf.jsdl.OperatingSystem_Type[] operatingSystem,
           org.apache.axis.message.MessageElement [] _any) {
           this.CPUArchitecture = CPUArchitecture;
           this.GPUArchitecture = GPUArchitecture;
           this.operatingSystem = operatingSystem;
           this._any = _any;
    }


    /**
     * Gets the CPUArchitecture value for this PlatformDescriptionType.
     * 
     * @return CPUArchitecture
     */
    public org.ggf.jsdl.CPUArchitecture_Type[] getCPUArchitecture() {
        return CPUArchitecture;
    }


    /**
     * Sets the CPUArchitecture value for this PlatformDescriptionType.
     * 
     * @param CPUArchitecture
     */
    public void setCPUArchitecture(org.ggf.jsdl.CPUArchitecture_Type[] CPUArchitecture) {
        this.CPUArchitecture = CPUArchitecture;
    }

    public org.ggf.jsdl.CPUArchitecture_Type getCPUArchitecture(int i) {
        return this.CPUArchitecture[i];
    }

    public void setCPUArchitecture(int i, org.ggf.jsdl.CPUArchitecture_Type _value) {
        this.CPUArchitecture[i] = _value;
    }


    /**
     * Gets the GPUArchitecture value for this PlatformDescriptionType.
     * 
     * @return GPUArchitecture
     */
    public org.ggf.jsdl.GPUArchitecture_Type[] getGPUArchitecture() {
        return GPUArchitecture;
    }


    /**
     * Sets the GPUArchitecture value for this PlatformDescriptionType.
     * 
     * @param GPUArchitecture
     */
    public void setGPUArchitecture(org.ggf.jsdl.GPUArchitecture_Type[] GPUArchitecture) {
        this.GPUArchitecture = GPUArchitecture;
    }

    public org.ggf.jsdl.GPUArchitecture_Type getGPUArchitecture(int i) {
        return this.GPUArchitecture[i];
    }

    public void setGPUArchitecture(int i, org.ggf.jsdl.GPUArchitecture_Type _value) {
        this.GPUArchitecture[i] = _value;
    }


    /**
     * Gets the operatingSystem value for this PlatformDescriptionType.
     * 
     * @return operatingSystem
     */
    public org.ggf.jsdl.OperatingSystem_Type[] getOperatingSystem() {
        return operatingSystem;
    }


    /**
     * Sets the operatingSystem value for this PlatformDescriptionType.
     * 
     * @param operatingSystem
     */
    public void setOperatingSystem(org.ggf.jsdl.OperatingSystem_Type[] operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public org.ggf.jsdl.OperatingSystem_Type getOperatingSystem(int i) {
        return this.operatingSystem[i];
    }

    public void setOperatingSystem(int i, org.ggf.jsdl.OperatingSystem_Type _value) {
        this.operatingSystem[i] = _value;
    }


    /**
     * Gets the _any value for this PlatformDescriptionType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this PlatformDescriptionType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PlatformDescriptionType)) return false;
        PlatformDescriptionType other = (PlatformDescriptionType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.CPUArchitecture==null && other.getCPUArchitecture()==null) || 
             (this.CPUArchitecture!=null &&
              java.util.Arrays.equals(this.CPUArchitecture, other.getCPUArchitecture()))) &&
            ((this.GPUArchitecture==null && other.getGPUArchitecture()==null) || 
             (this.GPUArchitecture!=null &&
              java.util.Arrays.equals(this.GPUArchitecture, other.getGPUArchitecture()))) &&
            ((this.operatingSystem==null && other.getOperatingSystem()==null) || 
             (this.operatingSystem!=null &&
              java.util.Arrays.equals(this.operatingSystem, other.getOperatingSystem()))) &&
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
        if (getCPUArchitecture() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCPUArchitecture());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCPUArchitecture(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getGPUArchitecture() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGPUArchitecture());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGPUArchitecture(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getOperatingSystem() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOperatingSystem());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOperatingSystem(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
        new org.apache.axis.description.TypeDesc(PlatformDescriptionType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "PlatformDescriptionType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUArchitecture");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitecture"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitecture_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUArchitecture");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitecture"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitecture_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatingSystem");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystem"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystem_Type"));
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
