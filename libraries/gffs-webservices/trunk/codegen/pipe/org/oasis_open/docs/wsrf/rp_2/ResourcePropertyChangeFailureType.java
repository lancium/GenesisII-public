/**
 * ResourcePropertyChangeFailureType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsrf.rp_2;

public class ResourcePropertyChangeFailureType  implements java.io.Serializable {
    private org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeCurrentValue currentValue;

    private org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeRequestedValue requestedValue;

    private boolean restored;  // attribute

    public ResourcePropertyChangeFailureType() {
    }

    public ResourcePropertyChangeFailureType(
           org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeCurrentValue currentValue,
           org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeRequestedValue requestedValue,
           boolean restored) {
           this.currentValue = currentValue;
           this.requestedValue = requestedValue;
           this.restored = restored;
    }


    /**
     * Gets the currentValue value for this ResourcePropertyChangeFailureType.
     * 
     * @return currentValue
     */
    public org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeCurrentValue getCurrentValue() {
        return currentValue;
    }


    /**
     * Sets the currentValue value for this ResourcePropertyChangeFailureType.
     * 
     * @param currentValue
     */
    public void setCurrentValue(org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeCurrentValue currentValue) {
        this.currentValue = currentValue;
    }


    /**
     * Gets the requestedValue value for this ResourcePropertyChangeFailureType.
     * 
     * @return requestedValue
     */
    public org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeRequestedValue getRequestedValue() {
        return requestedValue;
    }


    /**
     * Sets the requestedValue value for this ResourcePropertyChangeFailureType.
     * 
     * @param requestedValue
     */
    public void setRequestedValue(org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeRequestedValue requestedValue) {
        this.requestedValue = requestedValue;
    }


    /**
     * Gets the restored value for this ResourcePropertyChangeFailureType.
     * 
     * @return restored
     */
    public boolean isRestored() {
        return restored;
    }


    /**
     * Sets the restored value for this ResourcePropertyChangeFailureType.
     * 
     * @param restored
     */
    public void setRestored(boolean restored) {
        this.restored = restored;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResourcePropertyChangeFailureType)) return false;
        ResourcePropertyChangeFailureType other = (ResourcePropertyChangeFailureType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.currentValue==null && other.getCurrentValue()==null) || 
             (this.currentValue!=null &&
              this.currentValue.equals(other.getCurrentValue()))) &&
            ((this.requestedValue==null && other.getRequestedValue()==null) || 
             (this.requestedValue!=null &&
              this.requestedValue.equals(other.getRequestedValue()))) &&
            this.restored == other.isRestored();
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
        if (getCurrentValue() != null) {
            _hashCode += getCurrentValue().hashCode();
        }
        if (getRequestedValue() != null) {
            _hashCode += getRequestedValue().hashCode();
        }
        _hashCode += (isRestored() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResourcePropertyChangeFailureType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourcePropertyChangeFailureType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("restored");
        attrField.setXmlName(new javax.xml.namespace.QName("", "Restored"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "CurrentValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyChangeFailureType>CurrentValue"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestedValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "RequestedValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyChangeFailureType>RequestedValue"));
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
