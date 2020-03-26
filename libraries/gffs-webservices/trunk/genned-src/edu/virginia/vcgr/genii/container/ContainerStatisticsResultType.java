/**
 * ContainerStatisticsResultType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.container;

public class ContainerStatisticsResultType  implements java.io.Serializable {
    private long containerStartTime;

    private byte[] dbStatisticsReport;

    private byte[] methodStatisticsReport;

    public ContainerStatisticsResultType() {
    }

    public ContainerStatisticsResultType(
           long containerStartTime,
           byte[] dbStatisticsReport,
           byte[] methodStatisticsReport) {
           this.containerStartTime = containerStartTime;
           this.dbStatisticsReport = dbStatisticsReport;
           this.methodStatisticsReport = methodStatisticsReport;
    }


    /**
     * Gets the containerStartTime value for this ContainerStatisticsResultType.
     * 
     * @return containerStartTime
     */
    public long getContainerStartTime() {
        return containerStartTime;
    }


    /**
     * Sets the containerStartTime value for this ContainerStatisticsResultType.
     * 
     * @param containerStartTime
     */
    public void setContainerStartTime(long containerStartTime) {
        this.containerStartTime = containerStartTime;
    }


    /**
     * Gets the dbStatisticsReport value for this ContainerStatisticsResultType.
     * 
     * @return dbStatisticsReport
     */
    public byte[] getDbStatisticsReport() {
        return dbStatisticsReport;
    }


    /**
     * Sets the dbStatisticsReport value for this ContainerStatisticsResultType.
     * 
     * @param dbStatisticsReport
     */
    public void setDbStatisticsReport(byte[] dbStatisticsReport) {
        this.dbStatisticsReport = dbStatisticsReport;
    }


    /**
     * Gets the methodStatisticsReport value for this ContainerStatisticsResultType.
     * 
     * @return methodStatisticsReport
     */
    public byte[] getMethodStatisticsReport() {
        return methodStatisticsReport;
    }


    /**
     * Sets the methodStatisticsReport value for this ContainerStatisticsResultType.
     * 
     * @param methodStatisticsReport
     */
    public void setMethodStatisticsReport(byte[] methodStatisticsReport) {
        this.methodStatisticsReport = methodStatisticsReport;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ContainerStatisticsResultType)) return false;
        ContainerStatisticsResultType other = (ContainerStatisticsResultType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.containerStartTime == other.getContainerStartTime() &&
            ((this.dbStatisticsReport==null && other.getDbStatisticsReport()==null) || 
             (this.dbStatisticsReport!=null &&
              java.util.Arrays.equals(this.dbStatisticsReport, other.getDbStatisticsReport()))) &&
            ((this.methodStatisticsReport==null && other.getMethodStatisticsReport()==null) || 
             (this.methodStatisticsReport!=null &&
              java.util.Arrays.equals(this.methodStatisticsReport, other.getMethodStatisticsReport())));
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
        _hashCode += new Long(getContainerStartTime()).hashCode();
        if (getDbStatisticsReport() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDbStatisticsReport());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDbStatisticsReport(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMethodStatisticsReport() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMethodStatisticsReport());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMethodStatisticsReport(), i);
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
        new org.apache.axis.description.TypeDesc(ContainerStatisticsResultType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "ContainerStatisticsResultType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containerStartTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "container-start-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dbStatisticsReport");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "db-statistics-report"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("methodStatisticsReport");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "method-statistics-report"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
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
