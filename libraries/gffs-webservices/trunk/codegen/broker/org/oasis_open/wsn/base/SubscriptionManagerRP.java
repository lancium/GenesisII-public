/**
 * SubscriptionManagerRP.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class SubscriptionManagerRP  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType consumerReference;

    private org.oasis_open.wsn.base.FilterType filter;

    private org.oasis_open.wsn.base.SubscriptionPolicyType subscriptionPolicy;

    private java.util.Calendar creationTime;

    public SubscriptionManagerRP() {
    }

    public SubscriptionManagerRP(
           org.ws.addressing.EndpointReferenceType consumerReference,
           org.oasis_open.wsn.base.FilterType filter,
           org.oasis_open.wsn.base.SubscriptionPolicyType subscriptionPolicy,
           java.util.Calendar creationTime) {
           this.consumerReference = consumerReference;
           this.filter = filter;
           this.subscriptionPolicy = subscriptionPolicy;
           this.creationTime = creationTime;
    }


    /**
     * Gets the consumerReference value for this SubscriptionManagerRP.
     * 
     * @return consumerReference
     */
    public org.ws.addressing.EndpointReferenceType getConsumerReference() {
        return consumerReference;
    }


    /**
     * Sets the consumerReference value for this SubscriptionManagerRP.
     * 
     * @param consumerReference
     */
    public void setConsumerReference(org.ws.addressing.EndpointReferenceType consumerReference) {
        this.consumerReference = consumerReference;
    }


    /**
     * Gets the filter value for this SubscriptionManagerRP.
     * 
     * @return filter
     */
    public org.oasis_open.wsn.base.FilterType getFilter() {
        return filter;
    }


    /**
     * Sets the filter value for this SubscriptionManagerRP.
     * 
     * @param filter
     */
    public void setFilter(org.oasis_open.wsn.base.FilterType filter) {
        this.filter = filter;
    }


    /**
     * Gets the subscriptionPolicy value for this SubscriptionManagerRP.
     * 
     * @return subscriptionPolicy
     */
    public org.oasis_open.wsn.base.SubscriptionPolicyType getSubscriptionPolicy() {
        return subscriptionPolicy;
    }


    /**
     * Sets the subscriptionPolicy value for this SubscriptionManagerRP.
     * 
     * @param subscriptionPolicy
     */
    public void setSubscriptionPolicy(org.oasis_open.wsn.base.SubscriptionPolicyType subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }


    /**
     * Gets the creationTime value for this SubscriptionManagerRP.
     * 
     * @return creationTime
     */
    public java.util.Calendar getCreationTime() {
        return creationTime;
    }


    /**
     * Sets the creationTime value for this SubscriptionManagerRP.
     * 
     * @param creationTime
     */
    public void setCreationTime(java.util.Calendar creationTime) {
        this.creationTime = creationTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscriptionManagerRP)) return false;
        SubscriptionManagerRP other = (SubscriptionManagerRP) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.consumerReference==null && other.getConsumerReference()==null) || 
             (this.consumerReference!=null &&
              this.consumerReference.equals(other.getConsumerReference()))) &&
            ((this.filter==null && other.getFilter()==null) || 
             (this.filter!=null &&
              this.filter.equals(other.getFilter()))) &&
            ((this.subscriptionPolicy==null && other.getSubscriptionPolicy()==null) || 
             (this.subscriptionPolicy!=null &&
              this.subscriptionPolicy.equals(other.getSubscriptionPolicy()))) &&
            ((this.creationTime==null && other.getCreationTime()==null) || 
             (this.creationTime!=null &&
              this.creationTime.equals(other.getCreationTime())));
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
        if (getConsumerReference() != null) {
            _hashCode += getConsumerReference().hashCode();
        }
        if (getFilter() != null) {
            _hashCode += getFilter().hashCode();
        }
        if (getSubscriptionPolicy() != null) {
            _hashCode += getSubscriptionPolicy().hashCode();
        }
        if (getCreationTime() != null) {
            _hashCode += getCreationTime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscriptionManagerRP.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">SubscriptionManagerRP"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consumerReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "ConsumerReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "Filter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "FilterType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptionPolicy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscriptionPolicy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscriptionPolicyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "CreationTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
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
