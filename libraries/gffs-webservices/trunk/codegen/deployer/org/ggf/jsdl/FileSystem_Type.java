/**
 * FileSystem_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class FileSystem_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.FileSystemTypeEnumeration fileSystemType;

    private java.lang.String description;

    private java.lang.String mountPoint;

    private java.lang.String mountSource;

    private org.ggf.jsdl.RangeValue_Type diskSpace;

    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.NCName name;  // attribute

    private java.lang.String uniqueId;  // attribute

    public FileSystem_Type() {
    }

    public FileSystem_Type(
           org.ggf.jsdl.FileSystemTypeEnumeration fileSystemType,
           java.lang.String description,
           java.lang.String mountPoint,
           java.lang.String mountSource,
           org.ggf.jsdl.RangeValue_Type diskSpace,
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.NCName name,
           java.lang.String uniqueId) {
           this.fileSystemType = fileSystemType;
           this.description = description;
           this.mountPoint = mountPoint;
           this.mountSource = mountSource;
           this.diskSpace = diskSpace;
           this._any = _any;
           this.name = name;
           this.uniqueId = uniqueId;
    }


    /**
     * Gets the fileSystemType value for this FileSystem_Type.
     * 
     * @return fileSystemType
     */
    public org.ggf.jsdl.FileSystemTypeEnumeration getFileSystemType() {
        return fileSystemType;
    }


    /**
     * Sets the fileSystemType value for this FileSystem_Type.
     * 
     * @param fileSystemType
     */
    public void setFileSystemType(org.ggf.jsdl.FileSystemTypeEnumeration fileSystemType) {
        this.fileSystemType = fileSystemType;
    }


    /**
     * Gets the description value for this FileSystem_Type.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this FileSystem_Type.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the mountPoint value for this FileSystem_Type.
     * 
     * @return mountPoint
     */
    public java.lang.String getMountPoint() {
        return mountPoint;
    }


    /**
     * Sets the mountPoint value for this FileSystem_Type.
     * 
     * @param mountPoint
     */
    public void setMountPoint(java.lang.String mountPoint) {
        this.mountPoint = mountPoint;
    }


    /**
     * Gets the mountSource value for this FileSystem_Type.
     * 
     * @return mountSource
     */
    public java.lang.String getMountSource() {
        return mountSource;
    }


    /**
     * Sets the mountSource value for this FileSystem_Type.
     * 
     * @param mountSource
     */
    public void setMountSource(java.lang.String mountSource) {
        this.mountSource = mountSource;
    }


    /**
     * Gets the diskSpace value for this FileSystem_Type.
     * 
     * @return diskSpace
     */
    public org.ggf.jsdl.RangeValue_Type getDiskSpace() {
        return diskSpace;
    }


    /**
     * Sets the diskSpace value for this FileSystem_Type.
     * 
     * @param diskSpace
     */
    public void setDiskSpace(org.ggf.jsdl.RangeValue_Type diskSpace) {
        this.diskSpace = diskSpace;
    }


    /**
     * Gets the _any value for this FileSystem_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this FileSystem_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the name value for this FileSystem_Type.
     * 
     * @return name
     */
    public org.apache.axis.types.NCName getName() {
        return name;
    }


    /**
     * Sets the name value for this FileSystem_Type.
     * 
     * @param name
     */
    public void setName(org.apache.axis.types.NCName name) {
        this.name = name;
    }


    /**
     * Gets the uniqueId value for this FileSystem_Type.
     * 
     * @return uniqueId
     */
    public java.lang.String getUniqueId() {
        return uniqueId;
    }


    /**
     * Sets the uniqueId value for this FileSystem_Type.
     * 
     * @param uniqueId
     */
    public void setUniqueId(java.lang.String uniqueId) {
        this.uniqueId = uniqueId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FileSystem_Type)) return false;
        FileSystem_Type other = (FileSystem_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.fileSystemType==null && other.getFileSystemType()==null) || 
             (this.fileSystemType!=null &&
              this.fileSystemType.equals(other.getFileSystemType()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.mountPoint==null && other.getMountPoint()==null) || 
             (this.mountPoint!=null &&
              this.mountPoint.equals(other.getMountPoint()))) &&
            ((this.mountSource==null && other.getMountSource()==null) || 
             (this.mountSource!=null &&
              this.mountSource.equals(other.getMountSource()))) &&
            ((this.diskSpace==null && other.getDiskSpace()==null) || 
             (this.diskSpace!=null &&
              this.diskSpace.equals(other.getDiskSpace()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.uniqueId==null && other.getUniqueId()==null) || 
             (this.uniqueId!=null &&
              this.uniqueId.equals(other.getUniqueId())));
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
        if (getFileSystemType() != null) {
            _hashCode += getFileSystemType().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getMountPoint() != null) {
            _hashCode += getMountPoint().hashCode();
        }
        if (getMountSource() != null) {
            _hashCode += getMountSource().hashCode();
        }
        if (getDiskSpace() != null) {
            _hashCode += getDiskSpace().hashCode();
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
        if (getUniqueId() != null) {
            _hashCode += getUniqueId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FileSystem_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileSystem_Type"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "NCName"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("uniqueId");
        attrField.setXmlName(new javax.xml.namespace.QName("", "unique-id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileSystemType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileSystemType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileSystemTypeEnumeration"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mountPoint");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "MountPoint"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mountSource");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "MountSource"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("diskSpace");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "DiskSpace"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
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
