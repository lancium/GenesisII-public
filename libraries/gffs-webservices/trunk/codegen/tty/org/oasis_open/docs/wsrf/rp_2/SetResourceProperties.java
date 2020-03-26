/**
 * SetResourceProperties.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.wsrf.rp_2;

public class SetResourceProperties  implements java.io.Serializable {
    private org.oasis_open.docs.wsrf.rp_2.InsertType insert;

    private org.oasis_open.docs.wsrf.rp_2.UpdateType update;

    private org.oasis_open.docs.wsrf.rp_2.DeleteType delete;

    public SetResourceProperties() {
    }

    public SetResourceProperties(
           org.oasis_open.docs.wsrf.rp_2.InsertType insert,
           org.oasis_open.docs.wsrf.rp_2.UpdateType update,
           org.oasis_open.docs.wsrf.rp_2.DeleteType delete) {
           this.insert = insert;
           this.update = update;
           this.delete = delete;
    }


    /**
     * Gets the insert value for this SetResourceProperties.
     * 
     * @return insert
     */
    public org.oasis_open.docs.wsrf.rp_2.InsertType getInsert() {
        return insert;
    }


    /**
     * Sets the insert value for this SetResourceProperties.
     * 
     * @param insert
     */
    public void setInsert(org.oasis_open.docs.wsrf.rp_2.InsertType insert) {
        this.insert = insert;
    }


    /**
     * Gets the update value for this SetResourceProperties.
     * 
     * @return update
     */
    public org.oasis_open.docs.wsrf.rp_2.UpdateType getUpdate() {
        return update;
    }


    /**
     * Sets the update value for this SetResourceProperties.
     * 
     * @param update
     */
    public void setUpdate(org.oasis_open.docs.wsrf.rp_2.UpdateType update) {
        this.update = update;
    }


    /**
     * Gets the delete value for this SetResourceProperties.
     * 
     * @return delete
     */
    public org.oasis_open.docs.wsrf.rp_2.DeleteType getDelete() {
        return delete;
    }


    /**
     * Sets the delete value for this SetResourceProperties.
     * 
     * @param delete
     */
    public void setDelete(org.oasis_open.docs.wsrf.rp_2.DeleteType delete) {
        this.delete = delete;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SetResourceProperties)) return false;
        SetResourceProperties other = (SetResourceProperties) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.insert==null && other.getInsert()==null) || 
             (this.insert!=null &&
              this.insert.equals(other.getInsert()))) &&
            ((this.update==null && other.getUpdate()==null) || 
             (this.update!=null &&
              this.update.equals(other.getUpdate()))) &&
            ((this.delete==null && other.getDelete()==null) || 
             (this.delete!=null &&
              this.delete.equals(other.getDelete())));
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
        if (getInsert() != null) {
            _hashCode += getInsert().hashCode();
        }
        if (getUpdate() != null) {
            _hashCode += getUpdate().hashCode();
        }
        if (getDelete() != null) {
            _hashCode += getDelete().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SetResourceProperties.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">SetResourceProperties"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("insert");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "Insert"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("update");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "Update"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("delete");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "Delete"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteType"));
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
