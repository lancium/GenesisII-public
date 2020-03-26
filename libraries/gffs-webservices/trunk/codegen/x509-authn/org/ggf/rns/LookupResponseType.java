/**
 * LookupResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class LookupResponseType  implements java.io.Serializable {
    private org.ggf.rns.RNSEntryResponseType[] entryResponse;

    private org.ws.addressing.EndpointReferenceType iterator;

    public LookupResponseType() {
    }

    public LookupResponseType(
           org.ggf.rns.RNSEntryResponseType[] entryResponse,
           org.ws.addressing.EndpointReferenceType iterator) {
           this.entryResponse = entryResponse;
           this.iterator = iterator;
    }


    /**
     * Gets the entryResponse value for this LookupResponseType.
     * 
     * @return entryResponse
     */
    public org.ggf.rns.RNSEntryResponseType[] getEntryResponse() {
        return entryResponse;
    }


    /**
     * Sets the entryResponse value for this LookupResponseType.
     * 
     * @param entryResponse
     */
    public void setEntryResponse(org.ggf.rns.RNSEntryResponseType[] entryResponse) {
        this.entryResponse = entryResponse;
    }

    public org.ggf.rns.RNSEntryResponseType getEntryResponse(int i) {
        return this.entryResponse[i];
    }

    public void setEntryResponse(int i, org.ggf.rns.RNSEntryResponseType _value) {
        this.entryResponse[i] = _value;
    }


    /**
     * Gets the iterator value for this LookupResponseType.
     * 
     * @return iterator
     */
    public org.ws.addressing.EndpointReferenceType getIterator() {
        return iterator;
    }


    /**
     * Sets the iterator value for this LookupResponseType.
     * 
     * @param iterator
     */
    public void setIterator(org.ws.addressing.EndpointReferenceType iterator) {
        this.iterator = iterator;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LookupResponseType)) return false;
        LookupResponseType other = (LookupResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.entryResponse==null && other.getEntryResponse()==null) || 
             (this.entryResponse!=null &&
              java.util.Arrays.equals(this.entryResponse, other.getEntryResponse()))) &&
            ((this.iterator==null && other.getIterator()==null) || 
             (this.iterator!=null &&
              this.iterator.equals(other.getIterator())));
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
        if (getEntryResponse() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEntryResponse());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEntryResponse(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getIterator() != null) {
            _hashCode += getIterator().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LookupResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupResponseType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entryResponse");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iterator");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "iterator"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
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
