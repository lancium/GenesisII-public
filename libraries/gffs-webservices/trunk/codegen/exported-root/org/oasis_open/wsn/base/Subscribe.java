/**
 * Subscribe.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class Subscribe  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType consumerReference;

    private org.oasis_open.wsn.base.FilterType filter;

    private org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType initialTerminationTime;

    private org.oasis_open.wsn.base.SubscriptionPolicyType subscriptionPolicy;

    private org.apache.axis.message.MessageElement [] _any;

    public Subscribe() {
    }

    public Subscribe(
           org.ws.addressing.EndpointReferenceType consumerReference,
           org.oasis_open.wsn.base.FilterType filter,
           org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType initialTerminationTime,
           org.oasis_open.wsn.base.SubscriptionPolicyType subscriptionPolicy,
           org.apache.axis.message.MessageElement [] _any) {
           this.consumerReference = consumerReference;
           this.filter = filter;
           this.initialTerminationTime = initialTerminationTime;
           this.subscriptionPolicy = subscriptionPolicy;
           this._any = _any;
    }


    /**
     * Gets the consumerReference value for this Subscribe.
     * 
     * @return consumerReference
     */
    public org.ws.addressing.EndpointReferenceType getConsumerReference() {
        return consumerReference;
    }


    /**
     * Sets the consumerReference value for this Subscribe.
     * 
     * @param consumerReference
     */
    public void setConsumerReference(org.ws.addressing.EndpointReferenceType consumerReference) {
        this.consumerReference = consumerReference;
    }


    /**
     * Gets the filter value for this Subscribe.
     * 
     * @return filter
     */
    public org.oasis_open.wsn.base.FilterType getFilter() {
        return filter;
    }


    /**
     * Sets the filter value for this Subscribe.
     * 
     * @param filter
     */
    public void setFilter(org.oasis_open.wsn.base.FilterType filter) {
        this.filter = filter;
    }


    /**
     * Gets the initialTerminationTime value for this Subscribe.
     * 
     * @return initialTerminationTime
     */
    public org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType getInitialTerminationTime() {
        return initialTerminationTime;
    }


    /**
     * Sets the initialTerminationTime value for this Subscribe.
     * 
     * @param initialTerminationTime
     */
    public void setInitialTerminationTime(org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType initialTerminationTime) {
        this.initialTerminationTime = initialTerminationTime;
    }


    /**
     * Gets the subscriptionPolicy value for this Subscribe.
     * 
     * @return subscriptionPolicy
     */
    public org.oasis_open.wsn.base.SubscriptionPolicyType getSubscriptionPolicy() {
        return subscriptionPolicy;
    }


    /**
     * Sets the subscriptionPolicy value for this Subscribe.
     * 
     * @param subscriptionPolicy
     */
    public void setSubscriptionPolicy(org.oasis_open.wsn.base.SubscriptionPolicyType subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }


    /**
     * Gets the _any value for this Subscribe.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this Subscribe.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Subscribe)) return false;
        Subscribe other = (Subscribe) obj;
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
            ((this.initialTerminationTime==null && other.getInitialTerminationTime()==null) || 
             (this.initialTerminationTime!=null &&
              this.initialTerminationTime.equals(other.getInitialTerminationTime()))) &&
            ((this.subscriptionPolicy==null && other.getSubscriptionPolicy()==null) || 
             (this.subscriptionPolicy!=null &&
              this.subscriptionPolicy.equals(other.getSubscriptionPolicy()))) &&
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
        if (getConsumerReference() != null) {
            _hashCode += getConsumerReference().hashCode();
        }
        if (getFilter() != null) {
            _hashCode += getFilter().hashCode();
        }
        if (getInitialTerminationTime() != null) {
            _hashCode += getInitialTerminationTime().hashCode();
        }
        if (getSubscriptionPolicy() != null) {
            _hashCode += getSubscriptionPolicy().hashCode();
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
        new org.apache.axis.description.TypeDesc(Subscribe.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Subscribe"));
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
        elemField.setFieldName("initialTerminationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InitialTerminationTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "AbsoluteOrRelativeTimeType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptionPolicy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscriptionPolicy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscriptionPolicyType"));
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
