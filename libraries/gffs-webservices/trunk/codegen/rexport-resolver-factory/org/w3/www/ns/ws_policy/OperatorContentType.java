/**
 * OperatorContentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.w3.www.ns.ws_policy;

public class OperatorContentType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.w3.www.ns.ws_policy.Policy policy;

    private org.w3.www.ns.ws_policy.OperatorContentType all;

    private org.w3.www.ns.ws_policy.OperatorContentType exactlyOne;

    private org.w3.www.ns.ws_policy.PolicyReference policyReference;

    private org.apache.axis.message.MessageElement [] _any;

    public OperatorContentType() {
    }

    public OperatorContentType(
           org.w3.www.ns.ws_policy.Policy policy,
           org.w3.www.ns.ws_policy.OperatorContentType all,
           org.w3.www.ns.ws_policy.OperatorContentType exactlyOne,
           org.w3.www.ns.ws_policy.PolicyReference policyReference,
           org.apache.axis.message.MessageElement [] _any) {
           this.policy = policy;
           this.all = all;
           this.exactlyOne = exactlyOne;
           this.policyReference = policyReference;
           this._any = _any;
    }


    /**
     * Gets the policy value for this OperatorContentType.
     * 
     * @return policy
     */
    public org.w3.www.ns.ws_policy.Policy getPolicy() {
        return policy;
    }


    /**
     * Sets the policy value for this OperatorContentType.
     * 
     * @param policy
     */
    public void setPolicy(org.w3.www.ns.ws_policy.Policy policy) {
        this.policy = policy;
    }


    /**
     * Gets the all value for this OperatorContentType.
     * 
     * @return all
     */
    public org.w3.www.ns.ws_policy.OperatorContentType getAll() {
        return all;
    }


    /**
     * Sets the all value for this OperatorContentType.
     * 
     * @param all
     */
    public void setAll(org.w3.www.ns.ws_policy.OperatorContentType all) {
        this.all = all;
    }


    /**
     * Gets the exactlyOne value for this OperatorContentType.
     * 
     * @return exactlyOne
     */
    public org.w3.www.ns.ws_policy.OperatorContentType getExactlyOne() {
        return exactlyOne;
    }


    /**
     * Sets the exactlyOne value for this OperatorContentType.
     * 
     * @param exactlyOne
     */
    public void setExactlyOne(org.w3.www.ns.ws_policy.OperatorContentType exactlyOne) {
        this.exactlyOne = exactlyOne;
    }


    /**
     * Gets the policyReference value for this OperatorContentType.
     * 
     * @return policyReference
     */
    public org.w3.www.ns.ws_policy.PolicyReference getPolicyReference() {
        return policyReference;
    }


    /**
     * Sets the policyReference value for this OperatorContentType.
     * 
     * @param policyReference
     */
    public void setPolicyReference(org.w3.www.ns.ws_policy.PolicyReference policyReference) {
        this.policyReference = policyReference;
    }


    /**
     * Gets the _any value for this OperatorContentType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this OperatorContentType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OperatorContentType)) return false;
        OperatorContentType other = (OperatorContentType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.policy==null && other.getPolicy()==null) || 
             (this.policy!=null &&
              this.policy.equals(other.getPolicy()))) &&
            ((this.all==null && other.getAll()==null) || 
             (this.all!=null &&
              this.all.equals(other.getAll()))) &&
            ((this.exactlyOne==null && other.getExactlyOne()==null) || 
             (this.exactlyOne!=null &&
              this.exactlyOne.equals(other.getExactlyOne()))) &&
            ((this.policyReference==null && other.getPolicyReference()==null) || 
             (this.policyReference!=null &&
              this.policyReference.equals(other.getPolicyReference()))) &&
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
        if (getPolicy() != null) {
            _hashCode += getPolicy().hashCode();
        }
        if (getAll() != null) {
            _hashCode += getAll().hashCode();
        }
        if (getExactlyOne() != null) {
            _hashCode += getExactlyOne().hashCode();
        }
        if (getPolicyReference() != null) {
            _hashCode += getPolicyReference().hashCode();
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
        new org.apache.axis.description.TypeDesc(OperatorContentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "OperatorContentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("policy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "Policy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">Policy"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("all");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "All"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "OperatorContentType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exactlyOne");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "ExactlyOne"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "OperatorContentType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("policyReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "PolicyReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">PolicyReference"));
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
