/**
 * CreateResolverRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class CreateResolverRequestType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType target_EPR;

    private org.apache.axis.message.MessageElement [] _any;

    public CreateResolverRequestType() {
    }

    public CreateResolverRequestType(
           org.ws.addressing.EndpointReferenceType target_EPR,
           org.apache.axis.message.MessageElement [] _any) {
           this.target_EPR = target_EPR;
           this._any = _any;
    }


    /**
     * Gets the target_EPR value for this CreateResolverRequestType.
     * 
     * @return target_EPR
     */
    public org.ws.addressing.EndpointReferenceType getTarget_EPR() {
        return target_EPR;
    }


    /**
     * Sets the target_EPR value for this CreateResolverRequestType.
     * 
     * @param target_EPR
     */
    public void setTarget_EPR(org.ws.addressing.EndpointReferenceType target_EPR) {
        this.target_EPR = target_EPR;
    }


    /**
     * Gets the _any value for this CreateResolverRequestType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this CreateResolverRequestType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateResolverRequestType)) return false;
        CreateResolverRequestType other = (CreateResolverRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.target_EPR==null && other.getTarget_EPR()==null) || 
             (this.target_EPR!=null &&
              this.target_EPR.equals(other.getTarget_EPR()))) &&
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
        if (getTarget_EPR() != null) {
            _hashCode += getTarget_EPR().hashCode();
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
        new org.apache.axis.description.TypeDesc(CreateResolverRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "CreateResolverRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("target_EPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "target_EPR"));
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
