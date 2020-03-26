/**
 * ServiceEPRRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class ServiceEPRRequest  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType callingResolverEPR;

    public ServiceEPRRequest() {
    }

    public ServiceEPRRequest(
           org.ws.addressing.EndpointReferenceType callingResolverEPR) {
           this.callingResolverEPR = callingResolverEPR;
    }


    /**
     * Gets the callingResolverEPR value for this ServiceEPRRequest.
     * 
     * @return callingResolverEPR
     */
    public org.ws.addressing.EndpointReferenceType getCallingResolverEPR() {
        return callingResolverEPR;
    }


    /**
     * Sets the callingResolverEPR value for this ServiceEPRRequest.
     * 
     * @param callingResolverEPR
     */
    public void setCallingResolverEPR(org.ws.addressing.EndpointReferenceType callingResolverEPR) {
        this.callingResolverEPR = callingResolverEPR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ServiceEPRRequest)) return false;
        ServiceEPRRequest other = (ServiceEPRRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.callingResolverEPR==null && other.getCallingResolverEPR()==null) || 
             (this.callingResolverEPR!=null &&
              this.callingResolverEPR.equals(other.getCallingResolverEPR())));
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
        if (getCallingResolverEPR() != null) {
            _hashCode += getCallingResolverEPR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServiceEPRRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "ServiceEPRRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("callingResolverEPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver", "CallingResolverEPR"));
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
