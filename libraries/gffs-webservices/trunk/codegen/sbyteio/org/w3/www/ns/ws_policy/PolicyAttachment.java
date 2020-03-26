/**
 * PolicyAttachment.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.w3.www.ns.ws_policy;

public class PolicyAttachment  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.w3.www.ns.ws_policy.AppliesTo appliesTo;

    private org.w3.www.ns.ws_policy.Policy policy;

    private org.w3.www.ns.ws_policy.PolicyReference policyReference;

    private org.apache.axis.message.MessageElement [] _any;

    public PolicyAttachment() {
    }

    public PolicyAttachment(
           org.w3.www.ns.ws_policy.AppliesTo appliesTo,
           org.w3.www.ns.ws_policy.Policy policy,
           org.w3.www.ns.ws_policy.PolicyReference policyReference,
           org.apache.axis.message.MessageElement [] _any) {
           this.appliesTo = appliesTo;
           this.policy = policy;
           this.policyReference = policyReference;
           this._any = _any;
    }


    /**
     * Gets the appliesTo value for this PolicyAttachment.
     * 
     * @return appliesTo
     */
    public org.w3.www.ns.ws_policy.AppliesTo getAppliesTo() {
        return appliesTo;
    }


    /**
     * Sets the appliesTo value for this PolicyAttachment.
     * 
     * @param appliesTo
     */
    public void setAppliesTo(org.w3.www.ns.ws_policy.AppliesTo appliesTo) {
        this.appliesTo = appliesTo;
    }


    /**
     * Gets the policy value for this PolicyAttachment.
     * 
     * @return policy
     */
    public org.w3.www.ns.ws_policy.Policy getPolicy() {
        return policy;
    }


    /**
     * Sets the policy value for this PolicyAttachment.
     * 
     * @param policy
     */
    public void setPolicy(org.w3.www.ns.ws_policy.Policy policy) {
        this.policy = policy;
    }


    /**
     * Gets the policyReference value for this PolicyAttachment.
     * 
     * @return policyReference
     */
    public org.w3.www.ns.ws_policy.PolicyReference getPolicyReference() {
        return policyReference;
    }


    /**
     * Sets the policyReference value for this PolicyAttachment.
     * 
     * @param policyReference
     */
    public void setPolicyReference(org.w3.www.ns.ws_policy.PolicyReference policyReference) {
        this.policyReference = policyReference;
    }


    /**
     * Gets the _any value for this PolicyAttachment.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this PolicyAttachment.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PolicyAttachment)) return false;
        PolicyAttachment other = (PolicyAttachment) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.appliesTo==null && other.getAppliesTo()==null) || 
             (this.appliesTo!=null &&
              this.appliesTo.equals(other.getAppliesTo()))) &&
            ((this.policy==null && other.getPolicy()==null) || 
             (this.policy!=null &&
              this.policy.equals(other.getPolicy()))) &&
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
        if (getAppliesTo() != null) {
            _hashCode += getAppliesTo().hashCode();
        }
        if (getPolicy() != null) {
            _hashCode += getPolicy().hashCode();
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
        new org.apache.axis.description.TypeDesc(PolicyAttachment.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">PolicyAttachment"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("appliesTo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "AppliesTo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">AppliesTo"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("policy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "Policy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">Policy"));
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
