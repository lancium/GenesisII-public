/**
 * DataStaging_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class DataStaging_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private java.lang.String fileName;

    private org.apache.axis.types.NCName filesystemName;

    private org.ggf.jsdl.CreationFlagEnumeration creationFlag;

    private java.lang.Boolean deleteOnTermination;

    private java.lang.Boolean handleAsArchive;

    private java.lang.Boolean alwaysStageOut;

    private org.ggf.jsdl.SourceTarget_Type source;

    private org.ggf.jsdl.SourceTarget_Type target;

    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.NCName name;  // attribute

    public DataStaging_Type() {
    }

    public DataStaging_Type(
           java.lang.String fileName,
           org.apache.axis.types.NCName filesystemName,
           org.ggf.jsdl.CreationFlagEnumeration creationFlag,
           java.lang.Boolean deleteOnTermination,
           java.lang.Boolean handleAsArchive,
           java.lang.Boolean alwaysStageOut,
           org.ggf.jsdl.SourceTarget_Type source,
           org.ggf.jsdl.SourceTarget_Type target,
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.NCName name) {
           this.fileName = fileName;
           this.filesystemName = filesystemName;
           this.creationFlag = creationFlag;
           this.deleteOnTermination = deleteOnTermination;
           this.handleAsArchive = handleAsArchive;
           this.alwaysStageOut = alwaysStageOut;
           this.source = source;
           this.target = target;
           this._any = _any;
           this.name = name;
    }


    /**
     * Gets the fileName value for this DataStaging_Type.
     * 
     * @return fileName
     */
    public java.lang.String getFileName() {
        return fileName;
    }


    /**
     * Sets the fileName value for this DataStaging_Type.
     * 
     * @param fileName
     */
    public void setFileName(java.lang.String fileName) {
        this.fileName = fileName;
    }


    /**
     * Gets the filesystemName value for this DataStaging_Type.
     * 
     * @return filesystemName
     */
    public org.apache.axis.types.NCName getFilesystemName() {
        return filesystemName;
    }


    /**
     * Sets the filesystemName value for this DataStaging_Type.
     * 
     * @param filesystemName
     */
    public void setFilesystemName(org.apache.axis.types.NCName filesystemName) {
        this.filesystemName = filesystemName;
    }


    /**
     * Gets the creationFlag value for this DataStaging_Type.
     * 
     * @return creationFlag
     */
    public org.ggf.jsdl.CreationFlagEnumeration getCreationFlag() {
        return creationFlag;
    }


    /**
     * Sets the creationFlag value for this DataStaging_Type.
     * 
     * @param creationFlag
     */
    public void setCreationFlag(org.ggf.jsdl.CreationFlagEnumeration creationFlag) {
        this.creationFlag = creationFlag;
    }


    /**
     * Gets the deleteOnTermination value for this DataStaging_Type.
     * 
     * @return deleteOnTermination
     */
    public java.lang.Boolean getDeleteOnTermination() {
        return deleteOnTermination;
    }


    /**
     * Sets the deleteOnTermination value for this DataStaging_Type.
     * 
     * @param deleteOnTermination
     */
    public void setDeleteOnTermination(java.lang.Boolean deleteOnTermination) {
        this.deleteOnTermination = deleteOnTermination;
    }


    /**
     * Gets the handleAsArchive value for this DataStaging_Type.
     * 
     * @return handleAsArchive
     */
    public java.lang.Boolean getHandleAsArchive() {
        return handleAsArchive;
    }


    /**
     * Sets the handleAsArchive value for this DataStaging_Type.
     * 
     * @param handleAsArchive
     */
    public void setHandleAsArchive(java.lang.Boolean handleAsArchive) {
        this.handleAsArchive = handleAsArchive;
    }


    /**
     * Gets the alwaysStageOut value for this DataStaging_Type.
     * 
     * @return alwaysStageOut
     */
    public java.lang.Boolean getAlwaysStageOut() {
        return alwaysStageOut;
    }


    /**
     * Sets the alwaysStageOut value for this DataStaging_Type.
     * 
     * @param alwaysStageOut
     */
    public void setAlwaysStageOut(java.lang.Boolean alwaysStageOut) {
        this.alwaysStageOut = alwaysStageOut;
    }


    /**
     * Gets the source value for this DataStaging_Type.
     * 
     * @return source
     */
    public org.ggf.jsdl.SourceTarget_Type getSource() {
        return source;
    }


    /**
     * Sets the source value for this DataStaging_Type.
     * 
     * @param source
     */
    public void setSource(org.ggf.jsdl.SourceTarget_Type source) {
        this.source = source;
    }


    /**
     * Gets the target value for this DataStaging_Type.
     * 
     * @return target
     */
    public org.ggf.jsdl.SourceTarget_Type getTarget() {
        return target;
    }


    /**
     * Sets the target value for this DataStaging_Type.
     * 
     * @param target
     */
    public void setTarget(org.ggf.jsdl.SourceTarget_Type target) {
        this.target = target;
    }


    /**
     * Gets the _any value for this DataStaging_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this DataStaging_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the name value for this DataStaging_Type.
     * 
     * @return name
     */
    public org.apache.axis.types.NCName getName() {
        return name;
    }


    /**
     * Sets the name value for this DataStaging_Type.
     * 
     * @param name
     */
    public void setName(org.apache.axis.types.NCName name) {
        this.name = name;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DataStaging_Type)) return false;
        DataStaging_Type other = (DataStaging_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.fileName==null && other.getFileName()==null) || 
             (this.fileName!=null &&
              this.fileName.equals(other.getFileName()))) &&
            ((this.filesystemName==null && other.getFilesystemName()==null) || 
             (this.filesystemName!=null &&
              this.filesystemName.equals(other.getFilesystemName()))) &&
            ((this.creationFlag==null && other.getCreationFlag()==null) || 
             (this.creationFlag!=null &&
              this.creationFlag.equals(other.getCreationFlag()))) &&
            ((this.deleteOnTermination==null && other.getDeleteOnTermination()==null) || 
             (this.deleteOnTermination!=null &&
              this.deleteOnTermination.equals(other.getDeleteOnTermination()))) &&
            ((this.handleAsArchive==null && other.getHandleAsArchive()==null) || 
             (this.handleAsArchive!=null &&
              this.handleAsArchive.equals(other.getHandleAsArchive()))) &&
            ((this.alwaysStageOut==null && other.getAlwaysStageOut()==null) || 
             (this.alwaysStageOut!=null &&
              this.alwaysStageOut.equals(other.getAlwaysStageOut()))) &&
            ((this.source==null && other.getSource()==null) || 
             (this.source!=null &&
              this.source.equals(other.getSource()))) &&
            ((this.target==null && other.getTarget()==null) || 
             (this.target!=null &&
              this.target.equals(other.getTarget()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName())));
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
        if (getFileName() != null) {
            _hashCode += getFileName().hashCode();
        }
        if (getFilesystemName() != null) {
            _hashCode += getFilesystemName().hashCode();
        }
        if (getCreationFlag() != null) {
            _hashCode += getCreationFlag().hashCode();
        }
        if (getDeleteOnTermination() != null) {
            _hashCode += getDeleteOnTermination().hashCode();
        }
        if (getHandleAsArchive() != null) {
            _hashCode += getHandleAsArchive().hashCode();
        }
        if (getAlwaysStageOut() != null) {
            _hashCode += getAlwaysStageOut().hashCode();
        }
        if (getSource() != null) {
            _hashCode += getSource().hashCode();
        }
        if (getTarget() != null) {
            _hashCode += getTarget().hashCode();
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DataStaging_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "DataStaging_Type"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "NCName"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filesystemName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FilesystemName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "NCName"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creationFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CreationFlag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CreationFlagEnumeration"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deleteOnTermination");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "DeleteOnTermination"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("handleAsArchive");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "HandleAsArchive"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alwaysStageOut");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "AlwaysStageOut"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("source");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Source"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "SourceTarget_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("target");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Target"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "SourceTarget_Type"));
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
