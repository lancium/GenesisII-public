/**
 * CreateIteratorRequestType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.iterator;

public class CreateIteratorRequestType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.apache.axis.types.UnsignedInt initialBatchSize;

    private org.apache.axis.message.MessageElement [] _any;

    public CreateIteratorRequestType() {
    }

    public CreateIteratorRequestType(
           org.apache.axis.types.UnsignedInt initialBatchSize,
           org.apache.axis.message.MessageElement [] _any) {
           this.initialBatchSize = initialBatchSize;
           this._any = _any;
    }


    /**
     * Gets the initialBatchSize value for this CreateIteratorRequestType.
     * 
     * @return initialBatchSize
     */
    public org.apache.axis.types.UnsignedInt getInitialBatchSize() {
        return initialBatchSize;
    }


    /**
     * Sets the initialBatchSize value for this CreateIteratorRequestType.
     * 
     * @param initialBatchSize
     */
    public void setInitialBatchSize(org.apache.axis.types.UnsignedInt initialBatchSize) {
        this.initialBatchSize = initialBatchSize;
    }


    /**
     * Gets the _any value for this CreateIteratorRequestType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this CreateIteratorRequestType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateIteratorRequestType)) return false;
        CreateIteratorRequestType other = (CreateIteratorRequestType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.initialBatchSize==null && other.getInitialBatchSize()==null) || 
             (this.initialBatchSize!=null &&
              this.initialBatchSize.equals(other.getInitialBatchSize()))) &&
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
        if (getInitialBatchSize() != null) {
            _hashCode += getInitialBatchSize().hashCode();
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
        new org.apache.axis.description.TypeDesc(CreateIteratorRequestType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "CreateIteratorRequestType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("initialBatchSize");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "initial-batch-size"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"));
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
