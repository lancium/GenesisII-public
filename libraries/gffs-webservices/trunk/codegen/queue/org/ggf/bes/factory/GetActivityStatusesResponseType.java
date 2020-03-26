/**
 * GetActivityStatusesResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class GetActivityStatusesResponseType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.bes.factory.GetActivityStatusResponseType[] response;

    private org.apache.axis.message.MessageElement [] _any;

    public GetActivityStatusesResponseType() {
    }

    public GetActivityStatusesResponseType(
           org.ggf.bes.factory.GetActivityStatusResponseType[] response,
           org.apache.axis.message.MessageElement [] _any) {
           this.response = response;
           this._any = _any;
    }


    /**
     * Gets the response value for this GetActivityStatusesResponseType.
     * 
     * @return response
     */
    public org.ggf.bes.factory.GetActivityStatusResponseType[] getResponse() {
        return response;
    }


    /**
     * Sets the response value for this GetActivityStatusesResponseType.
     * 
     * @param response
     */
    public void setResponse(org.ggf.bes.factory.GetActivityStatusResponseType[] response) {
        this.response = response;
    }

    public org.ggf.bes.factory.GetActivityStatusResponseType getResponse(int i) {
        return this.response[i];
    }

    public void setResponse(int i, org.ggf.bes.factory.GetActivityStatusResponseType _value) {
        this.response[i] = _value;
    }


    /**
     * Gets the _any value for this GetActivityStatusesResponseType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this GetActivityStatusesResponseType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetActivityStatusesResponseType)) return false;
        GetActivityStatusesResponseType other = (GetActivityStatusesResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.response==null && other.getResponse()==null) || 
             (this.response!=null &&
              java.util.Arrays.equals(this.response, other.getResponse()))) &&
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
        if (getResponse() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getResponse());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getResponse(), i);
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
        new org.apache.axis.description.TypeDesc(GetActivityStatusesResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GetActivityStatusesResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("response");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "Response"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GetActivityStatusResponseType"));
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
