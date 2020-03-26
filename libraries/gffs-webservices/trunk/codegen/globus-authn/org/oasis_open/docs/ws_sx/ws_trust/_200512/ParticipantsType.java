/**
 * ParticipantsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_trust._200512;

public class ParticipantsType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType primary;

    private org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType[] participant;

    private org.apache.axis.message.MessageElement [] _any;

    public ParticipantsType() {
    }

    public ParticipantsType(
           org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType primary,
           org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType[] participant,
           org.apache.axis.message.MessageElement [] _any) {
           this.primary = primary;
           this.participant = participant;
           this._any = _any;
    }


    /**
     * Gets the primary value for this ParticipantsType.
     * 
     * @return primary
     */
    public org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType getPrimary() {
        return primary;
    }


    /**
     * Sets the primary value for this ParticipantsType.
     * 
     * @param primary
     */
    public void setPrimary(org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType primary) {
        this.primary = primary;
    }


    /**
     * Gets the participant value for this ParticipantsType.
     * 
     * @return participant
     */
    public org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType[] getParticipant() {
        return participant;
    }


    /**
     * Sets the participant value for this ParticipantsType.
     * 
     * @param participant
     */
    public void setParticipant(org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType[] participant) {
        this.participant = participant;
    }

    public org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType getParticipant(int i) {
        return this.participant[i];
    }

    public void setParticipant(int i, org.oasis_open.docs.ws_sx.ws_trust._200512.ParticipantType _value) {
        this.participant[i] = _value;
    }


    /**
     * Gets the _any value for this ParticipantsType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this ParticipantsType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParticipantsType)) return false;
        ParticipantsType other = (ParticipantsType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.primary==null && other.getPrimary()==null) || 
             (this.primary!=null &&
              this.primary.equals(other.getPrimary()))) &&
            ((this.participant==null && other.getParticipant()==null) || 
             (this.participant!=null &&
              java.util.Arrays.equals(this.participant, other.getParticipant()))) &&
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
        if (getPrimary() != null) {
            _hashCode += getPrimary().hashCode();
        }
        if (getParticipant() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParticipant());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParticipant(), i);
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
        new org.apache.axis.description.TypeDesc(ParticipantsType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "ParticipantsType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("primary");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "Primary"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "ParticipantType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("participant");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "Participant"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "ParticipantType"));
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
