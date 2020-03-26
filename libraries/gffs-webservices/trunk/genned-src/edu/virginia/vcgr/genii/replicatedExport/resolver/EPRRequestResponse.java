/**
 * EPRRequestResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class EPRRequestResponse  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType resolverServiceEPR;

    public EPRRequestResponse() {
    }

    public EPRRequestResponse(
           org.ws.addressing.EndpointReferenceType resolverServiceEPR) {
           this.resolverServiceEPR = resolverServiceEPR;
    }


    /**
     * Gets the resolverServiceEPR value for this EPRRequestResponse.
     * 
     * @return resolverServiceEPR
     */
    public org.ws.addressing.EndpointReferenceType getResolverServiceEPR() {
        return resolverServiceEPR;
    }


    /**
     * Sets the resolverServiceEPR value for this EPRRequestResponse.
     * 
     * @param resolverServiceEPR
     */
    public void setResolverServiceEPR(org.ws.addressing.EndpointReferenceType resolverServiceEPR) {
        this.resolverServiceEPR = resolverServiceEPR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EPRRequestResponse)) return false;
        EPRRequestResponse other = (EPRRequestResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.resolverServiceEPR==null && other.getResolverServiceEPR()==null) || 
             (this.resolverServiceEPR!=null &&
              this.resolverServiceEPR.equals(other.getResolverServiceEPR())));
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
        if (getResolverServiceEPR() != null) {
            _hashCode += getResolverServiceEPR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(EPRRequestResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "EPRRequestResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resolverServiceEPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "resolverServiceEPR"));
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
