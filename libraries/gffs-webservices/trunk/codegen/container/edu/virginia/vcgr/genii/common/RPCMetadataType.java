/**
 * RPCMetadataType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common;

public class RPCMetadataType  implements java.io.Serializable {
    private java.lang.String methodName;

    private java.lang.String issueDate;

    private java.lang.String returnDate;

    private org.ws.addressing.EndpointReferenceType targetEPR;

    private java.lang.String requestMessage;

    private java.lang.String responseMessage;

    public RPCMetadataType() {
    }

    public RPCMetadataType(
           java.lang.String methodName,
           java.lang.String issueDate,
           java.lang.String returnDate,
           org.ws.addressing.EndpointReferenceType targetEPR,
           java.lang.String requestMessage,
           java.lang.String responseMessage) {
           this.methodName = methodName;
           this.issueDate = issueDate;
           this.returnDate = returnDate;
           this.targetEPR = targetEPR;
           this.requestMessage = requestMessage;
           this.responseMessage = responseMessage;
    }


    /**
     * Gets the methodName value for this RPCMetadataType.
     * 
     * @return methodName
     */
    public java.lang.String getMethodName() {
        return methodName;
    }


    /**
     * Sets the methodName value for this RPCMetadataType.
     * 
     * @param methodName
     */
    public void setMethodName(java.lang.String methodName) {
        this.methodName = methodName;
    }


    /**
     * Gets the issueDate value for this RPCMetadataType.
     * 
     * @return issueDate
     */
    public java.lang.String getIssueDate() {
        return issueDate;
    }


    /**
     * Sets the issueDate value for this RPCMetadataType.
     * 
     * @param issueDate
     */
    public void setIssueDate(java.lang.String issueDate) {
        this.issueDate = issueDate;
    }


    /**
     * Gets the returnDate value for this RPCMetadataType.
     * 
     * @return returnDate
     */
    public java.lang.String getReturnDate() {
        return returnDate;
    }


    /**
     * Sets the returnDate value for this RPCMetadataType.
     * 
     * @param returnDate
     */
    public void setReturnDate(java.lang.String returnDate) {
        this.returnDate = returnDate;
    }


    /**
     * Gets the targetEPR value for this RPCMetadataType.
     * 
     * @return targetEPR
     */
    public org.ws.addressing.EndpointReferenceType getTargetEPR() {
        return targetEPR;
    }


    /**
     * Sets the targetEPR value for this RPCMetadataType.
     * 
     * @param targetEPR
     */
    public void setTargetEPR(org.ws.addressing.EndpointReferenceType targetEPR) {
        this.targetEPR = targetEPR;
    }


    /**
     * Gets the requestMessage value for this RPCMetadataType.
     * 
     * @return requestMessage
     */
    public java.lang.String getRequestMessage() {
        return requestMessage;
    }


    /**
     * Sets the requestMessage value for this RPCMetadataType.
     * 
     * @param requestMessage
     */
    public void setRequestMessage(java.lang.String requestMessage) {
        this.requestMessage = requestMessage;
    }


    /**
     * Gets the responseMessage value for this RPCMetadataType.
     * 
     * @return responseMessage
     */
    public java.lang.String getResponseMessage() {
        return responseMessage;
    }


    /**
     * Sets the responseMessage value for this RPCMetadataType.
     * 
     * @param responseMessage
     */
    public void setResponseMessage(java.lang.String responseMessage) {
        this.responseMessage = responseMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RPCMetadataType)) return false;
        RPCMetadataType other = (RPCMetadataType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.methodName==null && other.getMethodName()==null) || 
             (this.methodName!=null &&
              this.methodName.equals(other.getMethodName()))) &&
            ((this.issueDate==null && other.getIssueDate()==null) || 
             (this.issueDate!=null &&
              this.issueDate.equals(other.getIssueDate()))) &&
            ((this.returnDate==null && other.getReturnDate()==null) || 
             (this.returnDate!=null &&
              this.returnDate.equals(other.getReturnDate()))) &&
            ((this.targetEPR==null && other.getTargetEPR()==null) || 
             (this.targetEPR!=null &&
              this.targetEPR.equals(other.getTargetEPR()))) &&
            ((this.requestMessage==null && other.getRequestMessage()==null) || 
             (this.requestMessage!=null &&
              this.requestMessage.equals(other.getRequestMessage()))) &&
            ((this.responseMessage==null && other.getResponseMessage()==null) || 
             (this.responseMessage!=null &&
              this.responseMessage.equals(other.getResponseMessage())));
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
        if (getMethodName() != null) {
            _hashCode += getMethodName().hashCode();
        }
        if (getIssueDate() != null) {
            _hashCode += getIssueDate().hashCode();
        }
        if (getReturnDate() != null) {
            _hashCode += getReturnDate().hashCode();
        }
        if (getTargetEPR() != null) {
            _hashCode += getTargetEPR().hashCode();
        }
        if (getRequestMessage() != null) {
            _hashCode += getRequestMessage().hashCode();
        }
        if (getResponseMessage() != null) {
            _hashCode += getResponseMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RPCMetadataType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCMetadataType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("methodName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "methodName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("issueDate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "issueDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("returnDate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "returnDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("targetEPR");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "targetEPR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "requestMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responseMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "responseMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
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
