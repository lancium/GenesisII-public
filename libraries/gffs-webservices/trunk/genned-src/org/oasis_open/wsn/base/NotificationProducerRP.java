/**
 * NotificationProducerRP.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.wsn.base;

public class NotificationProducerRP  implements java.io.Serializable {
    private org.oasis_open.wsn.base.TopicExpressionType[] topicExpression;

    private java.lang.Boolean fixedTopicSet;

    private org.apache.axis.types.URI[] topicExpressionDialect;

    private org.oasis_open.docs.wsn.t_1.TopicSetType topicSet;

    public NotificationProducerRP() {
    }

    public NotificationProducerRP(
           org.oasis_open.wsn.base.TopicExpressionType[] topicExpression,
           java.lang.Boolean fixedTopicSet,
           org.apache.axis.types.URI[] topicExpressionDialect,
           org.oasis_open.docs.wsn.t_1.TopicSetType topicSet) {
           this.topicExpression = topicExpression;
           this.fixedTopicSet = fixedTopicSet;
           this.topicExpressionDialect = topicExpressionDialect;
           this.topicSet = topicSet;
    }


    /**
     * Gets the topicExpression value for this NotificationProducerRP.
     * 
     * @return topicExpression
     */
    public org.oasis_open.wsn.base.TopicExpressionType[] getTopicExpression() {
        return topicExpression;
    }


    /**
     * Sets the topicExpression value for this NotificationProducerRP.
     * 
     * @param topicExpression
     */
    public void setTopicExpression(org.oasis_open.wsn.base.TopicExpressionType[] topicExpression) {
        this.topicExpression = topicExpression;
    }

    public org.oasis_open.wsn.base.TopicExpressionType getTopicExpression(int i) {
        return this.topicExpression[i];
    }

    public void setTopicExpression(int i, org.oasis_open.wsn.base.TopicExpressionType _value) {
        this.topicExpression[i] = _value;
    }


    /**
     * Gets the fixedTopicSet value for this NotificationProducerRP.
     * 
     * @return fixedTopicSet
     */
    public java.lang.Boolean getFixedTopicSet() {
        return fixedTopicSet;
    }


    /**
     * Sets the fixedTopicSet value for this NotificationProducerRP.
     * 
     * @param fixedTopicSet
     */
    public void setFixedTopicSet(java.lang.Boolean fixedTopicSet) {
        this.fixedTopicSet = fixedTopicSet;
    }


    /**
     * Gets the topicExpressionDialect value for this NotificationProducerRP.
     * 
     * @return topicExpressionDialect
     */
    public org.apache.axis.types.URI[] getTopicExpressionDialect() {
        return topicExpressionDialect;
    }


    /**
     * Sets the topicExpressionDialect value for this NotificationProducerRP.
     * 
     * @param topicExpressionDialect
     */
    public void setTopicExpressionDialect(org.apache.axis.types.URI[] topicExpressionDialect) {
        this.topicExpressionDialect = topicExpressionDialect;
    }

    public org.apache.axis.types.URI getTopicExpressionDialect(int i) {
        return this.topicExpressionDialect[i];
    }

    public void setTopicExpressionDialect(int i, org.apache.axis.types.URI _value) {
        this.topicExpressionDialect[i] = _value;
    }


    /**
     * Gets the topicSet value for this NotificationProducerRP.
     * 
     * @return topicSet
     */
    public org.oasis_open.docs.wsn.t_1.TopicSetType getTopicSet() {
        return topicSet;
    }


    /**
     * Sets the topicSet value for this NotificationProducerRP.
     * 
     * @param topicSet
     */
    public void setTopicSet(org.oasis_open.docs.wsn.t_1.TopicSetType topicSet) {
        this.topicSet = topicSet;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NotificationProducerRP)) return false;
        NotificationProducerRP other = (NotificationProducerRP) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.topicExpression==null && other.getTopicExpression()==null) || 
             (this.topicExpression!=null &&
              java.util.Arrays.equals(this.topicExpression, other.getTopicExpression()))) &&
            ((this.fixedTopicSet==null && other.getFixedTopicSet()==null) || 
             (this.fixedTopicSet!=null &&
              this.fixedTopicSet.equals(other.getFixedTopicSet()))) &&
            ((this.topicExpressionDialect==null && other.getTopicExpressionDialect()==null) || 
             (this.topicExpressionDialect!=null &&
              java.util.Arrays.equals(this.topicExpressionDialect, other.getTopicExpressionDialect()))) &&
            ((this.topicSet==null && other.getTopicSet()==null) || 
             (this.topicSet!=null &&
              this.topicSet.equals(other.getTopicSet())));
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
        if (getTopicExpression() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTopicExpression());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTopicExpression(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFixedTopicSet() != null) {
            _hashCode += getFixedTopicSet().hashCode();
        }
        if (getTopicExpressionDialect() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTopicExpressionDialect());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTopicExpressionDialect(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTopicSet() != null) {
            _hashCode += getTopicSet().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NotificationProducerRP.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">NotificationProducerRP"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("topicExpression");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpression"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fixedTopicSet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "FixedTopicSet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("topicExpressionDialect");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionDialect"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("topicSet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "TopicSetType"));
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
