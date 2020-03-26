/**
 * RelatesToType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ws.addressing;

public class RelatesToType  implements java.io.Serializable, org.apache.axis.encoding.SimpleType {
    private org.apache.axis.types.URI _value;

    private org.ws.addressing.RelationshipTypeOpenEnum relationshipType;  // attribute

    public RelatesToType() {
    }

    // Simple Types must have a String constructor
    public RelatesToType(org.apache.axis.types.URI _value) {
        this._value = _value;
    }
    public RelatesToType(java.lang.String _value) {
        try {
            this._value = new org.apache.axis.types.URI(_value);
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
       }
    }

    // Simple Types must have a toString for serializing the value
    public java.lang.String toString() {
        return _value == null ? null : _value.toString();
    }


    /**
     * Gets the _value value for this RelatesToType.
     * 
     * @return _value
     */
    public org.apache.axis.types.URI get_value() {
        return _value;
    }


    /**
     * Sets the _value value for this RelatesToType.
     * 
     * @param _value
     */
    public void set_value(org.apache.axis.types.URI _value) {
        this._value = _value;
    }


    /**
     * Gets the relationshipType value for this RelatesToType.
     * 
     * @return relationshipType
     */
    public org.ws.addressing.RelationshipTypeOpenEnum getRelationshipType() {
        return relationshipType;
    }


    /**
     * Sets the relationshipType value for this RelatesToType.
     * 
     * @param relationshipType
     */
    public void setRelationshipType(org.ws.addressing.RelationshipTypeOpenEnum relationshipType) {
        this.relationshipType = relationshipType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RelatesToType)) return false;
        RelatesToType other = (RelatesToType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._value==null && other.get_value()==null) || 
             (this._value!=null &&
              this._value.equals(other.get_value()))) &&
            ((this.relationshipType==null && other.getRelationshipType()==null) || 
             (this.relationshipType!=null &&
              this.relationshipType.equals(other.getRelationshipType())));
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
        if (get_value() != null) {
            _hashCode += get_value().hashCode();
        }
        if (getRelationshipType() != null) {
            _hashCode += getRelationshipType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RelatesToType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "RelatesToType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("relationshipType");
        attrField.setXmlName(new javax.xml.namespace.QName("", "RelationshipType"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "RelationshipTypeOpenEnum"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_value");
        elemField.setXmlName(new javax.xml.namespace.QName("", "_value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
          new  org.apache.axis.encoding.ser.SimpleSerializer(
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
          new  org.apache.axis.encoding.ser.SimpleDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
