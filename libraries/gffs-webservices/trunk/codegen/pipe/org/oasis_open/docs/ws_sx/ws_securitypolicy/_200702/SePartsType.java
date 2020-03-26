/**
 * SePartsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_securitypolicy._200702;

public class SePartsType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType body;

    private org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType[] header;

    private org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType attachments;

    private org.apache.axis.message.MessageElement [] _any;

    public SePartsType() {
    }

    public SePartsType(
           org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType body,
           org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType[] header,
           org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType attachments,
           org.apache.axis.message.MessageElement [] _any) {
           this.body = body;
           this.header = header;
           this.attachments = attachments;
           this._any = _any;
    }


    /**
     * Gets the body value for this SePartsType.
     * 
     * @return body
     */
    public org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType getBody() {
        return body;
    }


    /**
     * Sets the body value for this SePartsType.
     * 
     * @param body
     */
    public void setBody(org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType body) {
        this.body = body;
    }


    /**
     * Gets the header value for this SePartsType.
     * 
     * @return header
     */
    public org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType[] getHeader() {
        return header;
    }


    /**
     * Sets the header value for this SePartsType.
     * 
     * @param header
     */
    public void setHeader(org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType[] header) {
        this.header = header;
    }

    public org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType getHeader(int i) {
        return this.header[i];
    }

    public void setHeader(int i, org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType _value) {
        this.header[i] = _value;
    }


    /**
     * Gets the attachments value for this SePartsType.
     * 
     * @return attachments
     */
    public org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType getAttachments() {
        return attachments;
    }


    /**
     * Sets the attachments value for this SePartsType.
     * 
     * @param attachments
     */
    public void setAttachments(org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType attachments) {
        this.attachments = attachments;
    }


    /**
     * Gets the _any value for this SePartsType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this SePartsType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SePartsType)) return false;
        SePartsType other = (SePartsType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.body==null && other.getBody()==null) || 
             (this.body!=null &&
              this.body.equals(other.getBody()))) &&
            ((this.header==null && other.getHeader()==null) || 
             (this.header!=null &&
              java.util.Arrays.equals(this.header, other.getHeader()))) &&
            ((this.attachments==null && other.getAttachments()==null) || 
             (this.attachments!=null &&
              this.attachments.equals(other.getAttachments()))) &&
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
        if (getBody() != null) {
            _hashCode += getBody().hashCode();
        }
        if (getHeader() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHeader());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHeader(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAttachments() != null) {
            _hashCode += getAttachments().hashCode();
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
        new org.apache.axis.description.TypeDesc(SePartsType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "SePartsType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("body");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "Body"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "EmptyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("header");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "Header"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "HeaderType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attachments");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "Attachments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "EmptyType"));
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
