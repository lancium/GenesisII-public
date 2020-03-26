/**
 * IteratorInitializationType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.iterator;

public class IteratorInitializationType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType iteratorEndpoint;

    private edu.virginia.vcgr.genii.iterator.IterableElementType[] batchElement;

    public IteratorInitializationType() {
    }

    public IteratorInitializationType(
           org.ws.addressing.EndpointReferenceType iteratorEndpoint,
           edu.virginia.vcgr.genii.iterator.IterableElementType[] batchElement) {
           this.iteratorEndpoint = iteratorEndpoint;
           this.batchElement = batchElement;
    }


    /**
     * Gets the iteratorEndpoint value for this IteratorInitializationType.
     * 
     * @return iteratorEndpoint
     */
    public org.ws.addressing.EndpointReferenceType getIteratorEndpoint() {
        return iteratorEndpoint;
    }


    /**
     * Sets the iteratorEndpoint value for this IteratorInitializationType.
     * 
     * @param iteratorEndpoint
     */
    public void setIteratorEndpoint(org.ws.addressing.EndpointReferenceType iteratorEndpoint) {
        this.iteratorEndpoint = iteratorEndpoint;
    }


    /**
     * Gets the batchElement value for this IteratorInitializationType.
     * 
     * @return batchElement
     */
    public edu.virginia.vcgr.genii.iterator.IterableElementType[] getBatchElement() {
        return batchElement;
    }


    /**
     * Sets the batchElement value for this IteratorInitializationType.
     * 
     * @param batchElement
     */
    public void setBatchElement(edu.virginia.vcgr.genii.iterator.IterableElementType[] batchElement) {
        this.batchElement = batchElement;
    }

    public edu.virginia.vcgr.genii.iterator.IterableElementType getBatchElement(int i) {
        return this.batchElement[i];
    }

    public void setBatchElement(int i, edu.virginia.vcgr.genii.iterator.IterableElementType _value) {
        this.batchElement[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IteratorInitializationType)) return false;
        IteratorInitializationType other = (IteratorInitializationType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.iteratorEndpoint==null && other.getIteratorEndpoint()==null) || 
             (this.iteratorEndpoint!=null &&
              this.iteratorEndpoint.equals(other.getIteratorEndpoint()))) &&
            ((this.batchElement==null && other.getBatchElement()==null) || 
             (this.batchElement!=null &&
              java.util.Arrays.equals(this.batchElement, other.getBatchElement())));
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
        if (getIteratorEndpoint() != null) {
            _hashCode += getIteratorEndpoint().hashCode();
        }
        if (getBatchElement() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBatchElement());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBatchElement(), i);
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
        new org.apache.axis.description.TypeDesc(IteratorInitializationType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "IteratorInitializationType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iteratorEndpoint");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "iterator-endpoint"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchElement");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "batch-element"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterableElementType"));
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
