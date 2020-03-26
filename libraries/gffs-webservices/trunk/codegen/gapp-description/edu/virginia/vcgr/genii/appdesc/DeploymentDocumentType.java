/**
 * DeploymentDocumentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.appdesc;

public class DeploymentDocumentType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType[] platformDescription;

    private org.apache.axis.message.MessageElement [] _any;

    public DeploymentDocumentType() {
    }

    public DeploymentDocumentType(
           edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType[] platformDescription,
           org.apache.axis.message.MessageElement [] _any) {
           this.platformDescription = platformDescription;
           this._any = _any;
    }


    /**
     * Gets the platformDescription value for this DeploymentDocumentType.
     * 
     * @return platformDescription
     */
    public edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType[] getPlatformDescription() {
        return platformDescription;
    }


    /**
     * Sets the platformDescription value for this DeploymentDocumentType.
     * 
     * @param platformDescription
     */
    public void setPlatformDescription(edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType[] platformDescription) {
        this.platformDescription = platformDescription;
    }

    public edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType getPlatformDescription(int i) {
        return this.platformDescription[i];
    }

    public void setPlatformDescription(int i, edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType _value) {
        this.platformDescription[i] = _value;
    }


    /**
     * Gets the _any value for this DeploymentDocumentType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this DeploymentDocumentType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeploymentDocumentType)) return false;
        DeploymentDocumentType other = (DeploymentDocumentType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.platformDescription==null && other.getPlatformDescription()==null) || 
             (this.platformDescription!=null &&
              java.util.Arrays.equals(this.platformDescription, other.getPlatformDescription()))) &&
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
        if (getPlatformDescription() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPlatformDescription());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPlatformDescription(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
        new org.apache.axis.description.TypeDesc(DeploymentDocumentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "DeploymentDocumentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("platformDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "PlatformDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/application-description", "PlatformDescriptionType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
