/**
 * UpdateResourceProperties.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsrf.rp_2;

public class UpdateResourceProperties  implements java.io.Serializable {
    private org.oasis_open.docs.wsrf.rp_2.UpdateType update;

    public UpdateResourceProperties() {
    }

    public UpdateResourceProperties(
           org.oasis_open.docs.wsrf.rp_2.UpdateType update) {
           this.update = update;
    }


    /**
     * Gets the update value for this UpdateResourceProperties.
     * 
     * @return update
     */
    public org.oasis_open.docs.wsrf.rp_2.UpdateType getUpdate() {
        return update;
    }


    /**
     * Sets the update value for this UpdateResourceProperties.
     * 
     * @param update
     */
    public void setUpdate(org.oasis_open.docs.wsrf.rp_2.UpdateType update) {
        this.update = update;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateResourceProperties)) return false;
        UpdateResourceProperties other = (UpdateResourceProperties) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.update==null && other.getUpdate()==null) || 
             (this.update!=null &&
              this.update.equals(other.getUpdate())));
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
        if (getUpdate() != null) {
            _hashCode += getUpdate().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateResourceProperties.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">UpdateResourceProperties"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("update");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "Update"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateType"));
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
