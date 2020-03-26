/**
 * RelativeNamedSourceType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc.bin;

public class RelativeNamedSourceType  extends edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType  implements java.io.Serializable {
    private java.lang.Boolean relativeToComponent;  // attribute

    private java.lang.String componentName;  // attribute

    private java.lang.Boolean relativeToCwd;  // attribute

    public RelativeNamedSourceType() {
    }

    public RelativeNamedSourceType(
           org.apache.axis.message.MessageElement [] _any,
           java.lang.String name,
           java.lang.Boolean relativeToComponent,
           java.lang.String componentName,
           java.lang.Boolean relativeToCwd) {
        super(
            _any,
            name);
        this.relativeToComponent = relativeToComponent;
        this.componentName = componentName;
        this.relativeToCwd = relativeToCwd;
    }


    /**
     * Gets the relativeToComponent value for this RelativeNamedSourceType.
     * 
     * @return relativeToComponent
     */
    public java.lang.Boolean getRelativeToComponent() {
        return relativeToComponent;
    }


    /**
     * Sets the relativeToComponent value for this RelativeNamedSourceType.
     * 
     * @param relativeToComponent
     */
    public void setRelativeToComponent(java.lang.Boolean relativeToComponent) {
        this.relativeToComponent = relativeToComponent;
    }


    /**
     * Gets the componentName value for this RelativeNamedSourceType.
     * 
     * @return componentName
     */
    public java.lang.String getComponentName() {
        return componentName;
    }


    /**
     * Sets the componentName value for this RelativeNamedSourceType.
     * 
     * @param componentName
     */
    public void setComponentName(java.lang.String componentName) {
        this.componentName = componentName;
    }


    /**
     * Gets the relativeToCwd value for this RelativeNamedSourceType.
     * 
     * @return relativeToCwd
     */
    public java.lang.Boolean getRelativeToCwd() {
        return relativeToCwd;
    }


    /**
     * Sets the relativeToCwd value for this RelativeNamedSourceType.
     * 
     * @param relativeToCwd
     */
    public void setRelativeToCwd(java.lang.Boolean relativeToCwd) {
        this.relativeToCwd = relativeToCwd;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RelativeNamedSourceType)) return false;
        RelativeNamedSourceType other = (RelativeNamedSourceType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.relativeToComponent==null && other.getRelativeToComponent()==null) || 
             (this.relativeToComponent!=null &&
              this.relativeToComponent.equals(other.getRelativeToComponent()))) &&
            ((this.componentName==null && other.getComponentName()==null) || 
             (this.componentName!=null &&
              this.componentName.equals(other.getComponentName()))) &&
            ((this.relativeToCwd==null && other.getRelativeToCwd()==null) || 
             (this.relativeToCwd!=null &&
              this.relativeToCwd.equals(other.getRelativeToCwd())));
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
        if (getRelativeToComponent() != null) {
            _hashCode += getRelativeToComponent().hashCode();
        }
        if (getComponentName() != null) {
            _hashCode += getComponentName().hashCode();
        }
        if (getRelativeToCwd() != null) {
            _hashCode += getRelativeToCwd().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RelativeNamedSourceType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description/bin", "RelativeNamedSourceType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("relativeToComponent");
        attrField.setXmlName(new javax.xml.namespace.QName("", "relative-to-component"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("componentName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "component-name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("relativeToCwd");
        attrField.setXmlName(new javax.xml.namespace.QName("", "relative-to-cwd"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
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
