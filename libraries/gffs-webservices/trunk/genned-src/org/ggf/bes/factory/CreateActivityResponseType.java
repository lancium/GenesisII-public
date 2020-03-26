/**
 * CreateActivityResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class CreateActivityResponseType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType activityIdentifier;

    private org.ggf.bes.factory.ActivityDocumentType activityDocument;

    private org.apache.axis.message.MessageElement [] _any;

    public CreateActivityResponseType() {
    }

    public CreateActivityResponseType(
           org.ws.addressing.EndpointReferenceType activityIdentifier,
           org.ggf.bes.factory.ActivityDocumentType activityDocument,
           org.apache.axis.message.MessageElement [] _any) {
           this.activityIdentifier = activityIdentifier;
           this.activityDocument = activityDocument;
           this._any = _any;
    }


    /**
     * Gets the activityIdentifier value for this CreateActivityResponseType.
     * 
     * @return activityIdentifier
     */
    public org.ws.addressing.EndpointReferenceType getActivityIdentifier() {
        return activityIdentifier;
    }


    /**
     * Sets the activityIdentifier value for this CreateActivityResponseType.
     * 
     * @param activityIdentifier
     */
    public void setActivityIdentifier(org.ws.addressing.EndpointReferenceType activityIdentifier) {
        this.activityIdentifier = activityIdentifier;
    }


    /**
     * Gets the activityDocument value for this CreateActivityResponseType.
     * 
     * @return activityDocument
     */
    public org.ggf.bes.factory.ActivityDocumentType getActivityDocument() {
        return activityDocument;
    }


    /**
     * Sets the activityDocument value for this CreateActivityResponseType.
     * 
     * @param activityDocument
     */
    public void setActivityDocument(org.ggf.bes.factory.ActivityDocumentType activityDocument) {
        this.activityDocument = activityDocument;
    }


    /**
     * Gets the _any value for this CreateActivityResponseType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this CreateActivityResponseType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateActivityResponseType)) return false;
        CreateActivityResponseType other = (CreateActivityResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.activityIdentifier==null && other.getActivityIdentifier()==null) || 
             (this.activityIdentifier!=null &&
              this.activityIdentifier.equals(other.getActivityIdentifier()))) &&
            ((this.activityDocument==null && other.getActivityDocument()==null) || 
             (this.activityDocument!=null &&
              this.activityDocument.equals(other.getActivityDocument()))) &&
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
        if (getActivityIdentifier() != null) {
            _hashCode += getActivityIdentifier().hashCode();
        }
        if (getActivityDocument() != null) {
            _hashCode += getActivityDocument().hashCode();
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
        new org.apache.axis.description.TypeDesc(CreateActivityResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CreateActivityResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityDocument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityDocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityDocumentType"));
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
