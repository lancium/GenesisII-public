/**
 * CreatePullPointResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class CreatePullPointResponse  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType pullPoint;

    private org.apache.axis.message.MessageElement [] _any;

    public CreatePullPointResponse() {
    }

    public CreatePullPointResponse(
           org.ws.addressing.EndpointReferenceType pullPoint,
           org.apache.axis.message.MessageElement [] _any) {
           this.pullPoint = pullPoint;
           this._any = _any;
    }


    /**
     * Gets the pullPoint value for this CreatePullPointResponse.
     * 
     * @return pullPoint
     */
    public org.ws.addressing.EndpointReferenceType getPullPoint() {
        return pullPoint;
    }


    /**
     * Sets the pullPoint value for this CreatePullPointResponse.
     * 
     * @param pullPoint
     */
    public void setPullPoint(org.ws.addressing.EndpointReferenceType pullPoint) {
        this.pullPoint = pullPoint;
    }


    /**
     * Gets the _any value for this CreatePullPointResponse.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this CreatePullPointResponse.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreatePullPointResponse)) return false;
        CreatePullPointResponse other = (CreatePullPointResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.pullPoint==null && other.getPullPoint()==null) || 
             (this.pullPoint!=null &&
              this.pullPoint.equals(other.getPullPoint()))) &&
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
        if (getPullPoint() != null) {
            _hashCode += getPullPoint().hashCode();
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
        new org.apache.axis.description.TypeDesc(CreatePullPointResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">CreatePullPointResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pullPoint");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "PullPoint"));
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
