/**
 * RegisterPublisher.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsn.br_2;

public class RegisterPublisher  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.EndpointReferenceType publisherReference;

    private org.oasis_open.wsn.base.TopicExpressionType[] topic;

    private java.lang.Boolean demand;

    private java.util.Calendar initialTerminationTime;

    private org.apache.axis.message.MessageElement [] _any;

    public RegisterPublisher() {
    }

    public RegisterPublisher(
           org.ws.addressing.EndpointReferenceType publisherReference,
           org.oasis_open.wsn.base.TopicExpressionType[] topic,
           java.lang.Boolean demand,
           java.util.Calendar initialTerminationTime,
           org.apache.axis.message.MessageElement [] _any) {
           this.publisherReference = publisherReference;
           this.topic = topic;
           this.demand = demand;
           this.initialTerminationTime = initialTerminationTime;
           this._any = _any;
    }


    /**
     * Gets the publisherReference value for this RegisterPublisher.
     * 
     * @return publisherReference
     */
    public org.ws.addressing.EndpointReferenceType getPublisherReference() {
        return publisherReference;
    }


    /**
     * Sets the publisherReference value for this RegisterPublisher.
     * 
     * @param publisherReference
     */
    public void setPublisherReference(org.ws.addressing.EndpointReferenceType publisherReference) {
        this.publisherReference = publisherReference;
    }


    /**
     * Gets the topic value for this RegisterPublisher.
     * 
     * @return topic
     */
    public org.oasis_open.wsn.base.TopicExpressionType[] getTopic() {
        return topic;
    }


    /**
     * Sets the topic value for this RegisterPublisher.
     * 
     * @param topic
     */
    public void setTopic(org.oasis_open.wsn.base.TopicExpressionType[] topic) {
        this.topic = topic;
    }

    public org.oasis_open.wsn.base.TopicExpressionType getTopic(int i) {
        return this.topic[i];
    }

    public void setTopic(int i, org.oasis_open.wsn.base.TopicExpressionType _value) {
        this.topic[i] = _value;
    }


    /**
     * Gets the demand value for this RegisterPublisher.
     * 
     * @return demand
     */
    public java.lang.Boolean getDemand() {
        return demand;
    }


    /**
     * Sets the demand value for this RegisterPublisher.
     * 
     * @param demand
     */
    public void setDemand(java.lang.Boolean demand) {
        this.demand = demand;
    }


    /**
     * Gets the initialTerminationTime value for this RegisterPublisher.
     * 
     * @return initialTerminationTime
     */
    public java.util.Calendar getInitialTerminationTime() {
        return initialTerminationTime;
    }


    /**
     * Sets the initialTerminationTime value for this RegisterPublisher.
     * 
     * @param initialTerminationTime
     */
    public void setInitialTerminationTime(java.util.Calendar initialTerminationTime) {
        this.initialTerminationTime = initialTerminationTime;
    }


    /**
     * Gets the _any value for this RegisterPublisher.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this RegisterPublisher.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RegisterPublisher)) return false;
        RegisterPublisher other = (RegisterPublisher) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.publisherReference==null && other.getPublisherReference()==null) || 
             (this.publisherReference!=null &&
              this.publisherReference.equals(other.getPublisherReference()))) &&
            ((this.topic==null && other.getTopic()==null) || 
             (this.topic!=null &&
              java.util.Arrays.equals(this.topic, other.getTopic()))) &&
            ((this.demand==null && other.getDemand()==null) || 
             (this.demand!=null &&
              this.demand.equals(other.getDemand()))) &&
            ((this.initialTerminationTime==null && other.getInitialTerminationTime()==null) || 
             (this.initialTerminationTime!=null &&
              this.initialTerminationTime.equals(other.getInitialTerminationTime()))) &&
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
        if (getPublisherReference() != null) {
            _hashCode += getPublisherReference().hashCode();
        }
        if (getTopic() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTopic());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTopic(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDemand() != null) {
            _hashCode += getDemand().hashCode();
        }
        if (getInitialTerminationTime() != null) {
            _hashCode += getInitialTerminationTime().hashCode();
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
        new org.apache.axis.description.TypeDesc(RegisterPublisher.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", ">RegisterPublisher"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("publisherReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "PublisherReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("topic");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "Topic"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("demand");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "Demand"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("initialTerminationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "InitialTerminationTime"));
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
