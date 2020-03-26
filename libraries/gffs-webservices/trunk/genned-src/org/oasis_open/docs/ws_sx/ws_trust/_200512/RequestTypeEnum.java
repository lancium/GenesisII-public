/**
 * RequestTypeEnum.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_trust._200512;

public class RequestTypeEnum implements java.io.Serializable {
    private org.apache.axis.types.URI _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected RequestTypeEnum(org.apache.axis.types.URI value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final org.apache.axis.types.URI _value1;
    static {
    	try {
            _value1 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final org.apache.axis.types.URI _value2;
    static {
    	try {
            _value2 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Renew");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final org.apache.axis.types.URI _value3;
    static {
    	try {
            _value3 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Cancel");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final org.apache.axis.types.URI _value4;
    static {
    	try {
            _value4 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/STSCancel");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final org.apache.axis.types.URI _value5;
    static {
    	try {
            _value5 = new org.apache.axis.types.URI("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate");
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
        }
    }

    public static final RequestTypeEnum value1 = new RequestTypeEnum(_value1);
    public static final RequestTypeEnum value2 = new RequestTypeEnum(_value2);
    public static final RequestTypeEnum value3 = new RequestTypeEnum(_value3);
    public static final RequestTypeEnum value4 = new RequestTypeEnum(_value4);
    public static final RequestTypeEnum value5 = new RequestTypeEnum(_value5);
    public org.apache.axis.types.URI getValue() { return _value_;}
    public static RequestTypeEnum fromValue(org.apache.axis.types.URI value)
          throws java.lang.IllegalArgumentException {
        RequestTypeEnum enumeration = (RequestTypeEnum)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static RequestTypeEnum fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        try {
            return fromValue(new org.apache.axis.types.URI(value));
        } catch (Exception e) {
            throw new java.lang.IllegalArgumentException();
        }
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_.toString();}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RequestTypeEnum.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RequestTypeEnum"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
