/**
 * ResourcePropertyValueChangeNotificationType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsrf.rp_2;

public class ResourcePropertyValueChangeNotificationType  implements java.io.Serializable {
    private org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeOldValues oldValues;

    private org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeNewValues newValues;

    public ResourcePropertyValueChangeNotificationType() {
    }

    public ResourcePropertyValueChangeNotificationType(
           org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeOldValues oldValues,
           org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeNewValues newValues) {
           this.oldValues = oldValues;
           this.newValues = newValues;
    }


    /**
     * Gets the oldValues value for this ResourcePropertyValueChangeNotificationType.
     * 
     * @return oldValues
     */
    public org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeOldValues getOldValues() {
        return oldValues;
    }


    /**
     * Sets the oldValues value for this ResourcePropertyValueChangeNotificationType.
     * 
     * @param oldValues
     */
    public void setOldValues(org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeOldValues oldValues) {
        this.oldValues = oldValues;
    }


    /**
     * Gets the newValues value for this ResourcePropertyValueChangeNotificationType.
     * 
     * @return newValues
     */
    public org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeNewValues getNewValues() {
        return newValues;
    }


    /**
     * Sets the newValues value for this ResourcePropertyValueChangeNotificationType.
     * 
     * @param newValues
     */
    public void setNewValues(org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeNewValues newValues) {
        this.newValues = newValues;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResourcePropertyValueChangeNotificationType)) return false;
        ResourcePropertyValueChangeNotificationType other = (ResourcePropertyValueChangeNotificationType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.oldValues==null && other.getOldValues()==null) || 
             (this.oldValues!=null &&
              this.oldValues.equals(other.getOldValues()))) &&
            ((this.newValues==null && other.getNewValues()==null) || 
             (this.newValues!=null &&
              this.newValues.equals(other.getNewValues())));
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
        if (getOldValues() != null) {
            _hashCode += getOldValues().hashCode();
        }
        if (getNewValues() != null) {
            _hashCode += getNewValues().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResourcePropertyValueChangeNotificationType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourcePropertyValueChangeNotificationType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("oldValues");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "OldValues"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyValueChangeNotificationType>OldValues"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newValues");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "NewValues"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyValueChangeNotificationType>NewValues"));
        elemField.setNillable(true);
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
