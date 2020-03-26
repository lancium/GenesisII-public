/**
 * JobErrorPacket.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.cs.vcgr.genii.job_management;

public class JobErrorPacket  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedShort attempt;

    private java.lang.String[] faultText;

    public JobErrorPacket() {
    }

    public JobErrorPacket(
           org.apache.axis.types.UnsignedShort attempt,
           java.lang.String[] faultText) {
           this.attempt = attempt;
           this.faultText = faultText;
    }


    /**
     * Gets the attempt value for this JobErrorPacket.
     * 
     * @return attempt
     */
    public org.apache.axis.types.UnsignedShort getAttempt() {
        return attempt;
    }


    /**
     * Sets the attempt value for this JobErrorPacket.
     * 
     * @param attempt
     */
    public void setAttempt(org.apache.axis.types.UnsignedShort attempt) {
        this.attempt = attempt;
    }


    /**
     * Gets the faultText value for this JobErrorPacket.
     * 
     * @return faultText
     */
    public java.lang.String[] getFaultText() {
        return faultText;
    }


    /**
     * Sets the faultText value for this JobErrorPacket.
     * 
     * @param faultText
     */
    public void setFaultText(java.lang.String[] faultText) {
        this.faultText = faultText;
    }

    public java.lang.String getFaultText(int i) {
        return this.faultText[i];
    }

    public void setFaultText(int i, java.lang.String _value) {
        this.faultText[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof JobErrorPacket)) return false;
        JobErrorPacket other = (JobErrorPacket) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attempt==null && other.getAttempt()==null) || 
             (this.attempt!=null &&
              this.attempt.equals(other.getAttempt()))) &&
            ((this.faultText==null && other.getFaultText()==null) || 
             (this.faultText!=null &&
              java.util.Arrays.equals(this.faultText, other.getFaultText())));
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
        if (getAttempt() != null) {
            _hashCode += getAttempt().hashCode();
        }
        if (getFaultText() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFaultText());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFaultText(), i);
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
        new org.apache.axis.description.TypeDesc(JobErrorPacket.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "JobErrorPacket"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attempt");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "attempt"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("faultText");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/job-management", "fault-text"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
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
