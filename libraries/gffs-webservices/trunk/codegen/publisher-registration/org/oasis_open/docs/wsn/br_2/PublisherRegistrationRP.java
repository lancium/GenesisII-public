/**
 * PublisherRegistrationRP.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsn.br_2;

public class PublisherRegistrationRP  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType publisherReference;

    private org.oasis_open.wsn.base.TopicExpressionType[] topic;

    private boolean demand;

    private java.util.Calendar creationTime;

    public PublisherRegistrationRP() {
    }

    public PublisherRegistrationRP(
           org.ws.addressing.EndpointReferenceType publisherReference,
           org.oasis_open.wsn.base.TopicExpressionType[] topic,
           boolean demand,
           java.util.Calendar creationTime) {
           this.publisherReference = publisherReference;
           this.topic = topic;
           this.demand = demand;
           this.creationTime = creationTime;
    }


    /**
     * Gets the publisherReference value for this PublisherRegistrationRP.
     * 
     * @return publisherReference
     */
    public org.ws.addressing.EndpointReferenceType getPublisherReference() {
        return publisherReference;
    }


    /**
     * Sets the publisherReference value for this PublisherRegistrationRP.
     * 
     * @param publisherReference
     */
    public void setPublisherReference(org.ws.addressing.EndpointReferenceType publisherReference) {
        this.publisherReference = publisherReference;
    }


    /**
     * Gets the topic value for this PublisherRegistrationRP.
     * 
     * @return topic
     */
    public org.oasis_open.wsn.base.TopicExpressionType[] getTopic() {
        return topic;
    }


    /**
     * Sets the topic value for this PublisherRegistrationRP.
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
     * Gets the demand value for this PublisherRegistrationRP.
     * 
     * @return demand
     */
    public boolean isDemand() {
        return demand;
    }


    /**
     * Sets the demand value for this PublisherRegistrationRP.
     * 
     * @param demand
     */
    public void setDemand(boolean demand) {
        this.demand = demand;
    }


    /**
     * Gets the creationTime value for this PublisherRegistrationRP.
     * 
     * @return creationTime
     */
    public java.util.Calendar getCreationTime() {
        return creationTime;
    }


    /**
     * Sets the creationTime value for this PublisherRegistrationRP.
     * 
     * @param creationTime
     */
    public void setCreationTime(java.util.Calendar creationTime) {
        this.creationTime = creationTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PublisherRegistrationRP)) return false;
        PublisherRegistrationRP other = (PublisherRegistrationRP) obj;
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
            this.demand == other.isDemand() &&
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
        _hashCode += (isDemand() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getCreationTime() != null) {
            _hashCode += getCreationTime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PublisherRegistrationRP.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", ">PublisherRegistrationRP"));
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
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creationTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/br-2", "CreationTime"));
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
