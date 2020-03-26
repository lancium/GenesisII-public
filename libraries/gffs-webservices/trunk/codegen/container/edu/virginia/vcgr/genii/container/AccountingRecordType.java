/**
 * AccountingRecordType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.container;

public class AccountingRecordType  implements java.io.Serializable {
    private long recordId;

    private java.lang.String besEpi;

    private java.lang.String arch;

    private java.lang.String os;

    private java.lang.String besMachineName;

    private java.lang.String[] commandLineValue;

    private int exitCode;

    private long userTime;

    private long kernelTime;

    private long wallclockTime;

    private long maximumRss;

    private byte[] credentials;

    private java.util.Calendar recordaddtime;

    public AccountingRecordType() {
    }

    public AccountingRecordType(
           long recordId,
           java.lang.String besEpi,
           java.lang.String arch,
           java.lang.String os,
           java.lang.String besMachineName,
           java.lang.String[] commandLineValue,
           int exitCode,
           long userTime,
           long kernelTime,
           long wallclockTime,
           long maximumRss,
           byte[] credentials,
           java.util.Calendar recordaddtime) {
           this.recordId = recordId;
           this.besEpi = besEpi;
           this.arch = arch;
           this.os = os;
           this.besMachineName = besMachineName;
           this.commandLineValue = commandLineValue;
           this.exitCode = exitCode;
           this.userTime = userTime;
           this.kernelTime = kernelTime;
           this.wallclockTime = wallclockTime;
           this.maximumRss = maximumRss;
           this.credentials = credentials;
           this.recordaddtime = recordaddtime;
    }


    /**
     * Gets the recordId value for this AccountingRecordType.
     * 
     * @return recordId
     */
    public long getRecordId() {
        return recordId;
    }


    /**
     * Sets the recordId value for this AccountingRecordType.
     * 
     * @param recordId
     */
    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }


    /**
     * Gets the besEpi value for this AccountingRecordType.
     * 
     * @return besEpi
     */
    public java.lang.String getBesEpi() {
        return besEpi;
    }


    /**
     * Sets the besEpi value for this AccountingRecordType.
     * 
     * @param besEpi
     */
    public void setBesEpi(java.lang.String besEpi) {
        this.besEpi = besEpi;
    }


    /**
     * Gets the arch value for this AccountingRecordType.
     * 
     * @return arch
     */
    public java.lang.String getArch() {
        return arch;
    }


    /**
     * Sets the arch value for this AccountingRecordType.
     * 
     * @param arch
     */
    public void setArch(java.lang.String arch) {
        this.arch = arch;
    }


    /**
     * Gets the os value for this AccountingRecordType.
     * 
     * @return os
     */
    public java.lang.String getOs() {
        return os;
    }


    /**
     * Sets the os value for this AccountingRecordType.
     * 
     * @param os
     */
    public void setOs(java.lang.String os) {
        this.os = os;
    }


    /**
     * Gets the besMachineName value for this AccountingRecordType.
     * 
     * @return besMachineName
     */
    public java.lang.String getBesMachineName() {
        return besMachineName;
    }


    /**
     * Sets the besMachineName value for this AccountingRecordType.
     * 
     * @param besMachineName
     */
    public void setBesMachineName(java.lang.String besMachineName) {
        this.besMachineName = besMachineName;
    }


    /**
     * Gets the commandLineValue value for this AccountingRecordType.
     * 
     * @return commandLineValue
     */
    public java.lang.String[] getCommandLineValue() {
        return commandLineValue;
    }


    /**
     * Sets the commandLineValue value for this AccountingRecordType.
     * 
     * @param commandLineValue
     */
    public void setCommandLineValue(java.lang.String[] commandLineValue) {
        this.commandLineValue = commandLineValue;
    }

    public java.lang.String getCommandLineValue(int i) {
        return this.commandLineValue[i];
    }

    public void setCommandLineValue(int i, java.lang.String _value) {
        this.commandLineValue[i] = _value;
    }


    /**
     * Gets the exitCode value for this AccountingRecordType.
     * 
     * @return exitCode
     */
    public int getExitCode() {
        return exitCode;
    }


    /**
     * Sets the exitCode value for this AccountingRecordType.
     * 
     * @param exitCode
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }


    /**
     * Gets the userTime value for this AccountingRecordType.
     * 
     * @return userTime
     */
    public long getUserTime() {
        return userTime;
    }


    /**
     * Sets the userTime value for this AccountingRecordType.
     * 
     * @param userTime
     */
    public void setUserTime(long userTime) {
        this.userTime = userTime;
    }


    /**
     * Gets the kernelTime value for this AccountingRecordType.
     * 
     * @return kernelTime
     */
    public long getKernelTime() {
        return kernelTime;
    }


    /**
     * Sets the kernelTime value for this AccountingRecordType.
     * 
     * @param kernelTime
     */
    public void setKernelTime(long kernelTime) {
        this.kernelTime = kernelTime;
    }


    /**
     * Gets the wallclockTime value for this AccountingRecordType.
     * 
     * @return wallclockTime
     */
    public long getWallclockTime() {
        return wallclockTime;
    }


    /**
     * Sets the wallclockTime value for this AccountingRecordType.
     * 
     * @param wallclockTime
     */
    public void setWallclockTime(long wallclockTime) {
        this.wallclockTime = wallclockTime;
    }


    /**
     * Gets the maximumRss value for this AccountingRecordType.
     * 
     * @return maximumRss
     */
    public long getMaximumRss() {
        return maximumRss;
    }


    /**
     * Sets the maximumRss value for this AccountingRecordType.
     * 
     * @param maximumRss
     */
    public void setMaximumRss(long maximumRss) {
        this.maximumRss = maximumRss;
    }


    /**
     * Gets the credentials value for this AccountingRecordType.
     * 
     * @return credentials
     */
    public byte[] getCredentials() {
        return credentials;
    }


    /**
     * Sets the credentials value for this AccountingRecordType.
     * 
     * @param credentials
     */
    public void setCredentials(byte[] credentials) {
        this.credentials = credentials;
    }


    /**
     * Gets the recordaddtime value for this AccountingRecordType.
     * 
     * @return recordaddtime
     */
    public java.util.Calendar getRecordaddtime() {
        return recordaddtime;
    }


    /**
     * Sets the recordaddtime value for this AccountingRecordType.
     * 
     * @param recordaddtime
     */
    public void setRecordaddtime(java.util.Calendar recordaddtime) {
        this.recordaddtime = recordaddtime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AccountingRecordType)) return false;
        AccountingRecordType other = (AccountingRecordType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.recordId == other.getRecordId() &&
            ((this.besEpi==null && other.getBesEpi()==null) || 
             (this.besEpi!=null &&
              this.besEpi.equals(other.getBesEpi()))) &&
            ((this.arch==null && other.getArch()==null) || 
             (this.arch!=null &&
              this.arch.equals(other.getArch()))) &&
            ((this.os==null && other.getOs()==null) || 
             (this.os!=null &&
              this.os.equals(other.getOs()))) &&
            ((this.besMachineName==null && other.getBesMachineName()==null) || 
             (this.besMachineName!=null &&
              this.besMachineName.equals(other.getBesMachineName()))) &&
            ((this.commandLineValue==null && other.getCommandLineValue()==null) || 
             (this.commandLineValue!=null &&
              java.util.Arrays.equals(this.commandLineValue, other.getCommandLineValue()))) &&
            this.exitCode == other.getExitCode() &&
            this.userTime == other.getUserTime() &&
            this.kernelTime == other.getKernelTime() &&
            this.wallclockTime == other.getWallclockTime() &&
            this.maximumRss == other.getMaximumRss() &&
            ((this.credentials==null && other.getCredentials()==null) || 
             (this.credentials!=null &&
              java.util.Arrays.equals(this.credentials, other.getCredentials()))) &&
            ((this.recordaddtime==null && other.getRecordaddtime()==null) || 
             (this.recordaddtime!=null &&
              this.recordaddtime.equals(other.getRecordaddtime())));
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
        _hashCode += new Long(getRecordId()).hashCode();
        if (getBesEpi() != null) {
            _hashCode += getBesEpi().hashCode();
        }
        if (getArch() != null) {
            _hashCode += getArch().hashCode();
        }
        if (getOs() != null) {
            _hashCode += getOs().hashCode();
        }
        if (getBesMachineName() != null) {
            _hashCode += getBesMachineName().hashCode();
        }
        if (getCommandLineValue() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCommandLineValue());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCommandLineValue(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getExitCode();
        _hashCode += new Long(getUserTime()).hashCode();
        _hashCode += new Long(getKernelTime()).hashCode();
        _hashCode += new Long(getWallclockTime()).hashCode();
        _hashCode += new Long(getMaximumRss()).hashCode();
        if (getCredentials() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCredentials());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCredentials(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRecordaddtime() != null) {
            _hashCode += getRecordaddtime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AccountingRecordType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "AccountingRecordType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recordId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "record-id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("besEpi");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "bes-epi"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("arch");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "arch"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("os");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "os"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("besMachineName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "bes-machine-name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commandLineValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "command-line-value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exitCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "exit-code"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "user-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("kernelTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "kernel-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("wallclockTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "wallclock-time"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("maximumRss");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "maximum-rss"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("credentials");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "credentials"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recordaddtime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "recordaddtime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
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
