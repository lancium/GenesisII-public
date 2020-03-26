/**
 * FactoryResourceAttributesDocumentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.bes.factory;

public class FactoryResourceAttributesDocumentType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.bes.factory.BasicResourceAttributesDocumentType basicResourceAttributesDocument;

    private boolean isAcceptingNewActivities;

    private java.lang.String commonName;

    private java.lang.String longDescription;

    private long totalNumberOfActivities;

    private org.ws.addressing.EndpointReferenceType[] activityReference;

    private long totalNumberOfContainedResources;

    private java.lang.Object[] containedResource;

    private org.apache.axis.types.URI[] namingProfile;

    private org.apache.axis.types.URI[] BESExtension;

    private org.apache.axis.types.URI localResourceManagerType;

    private org.apache.axis.message.MessageElement [] _any;

    public FactoryResourceAttributesDocumentType() {
    }

    public FactoryResourceAttributesDocumentType(
           org.ggf.bes.factory.BasicResourceAttributesDocumentType basicResourceAttributesDocument,
           boolean isAcceptingNewActivities,
           java.lang.String commonName,
           java.lang.String longDescription,
           long totalNumberOfActivities,
           org.ws.addressing.EndpointReferenceType[] activityReference,
           long totalNumberOfContainedResources,
           java.lang.Object[] containedResource,
           org.apache.axis.types.URI[] namingProfile,
           org.apache.axis.types.URI[] BESExtension,
           org.apache.axis.types.URI localResourceManagerType,
           org.apache.axis.message.MessageElement [] _any) {
           this.basicResourceAttributesDocument = basicResourceAttributesDocument;
           this.isAcceptingNewActivities = isAcceptingNewActivities;
           this.commonName = commonName;
           this.longDescription = longDescription;
           this.totalNumberOfActivities = totalNumberOfActivities;
           this.activityReference = activityReference;
           this.totalNumberOfContainedResources = totalNumberOfContainedResources;
           this.containedResource = containedResource;
           this.namingProfile = namingProfile;
           this.BESExtension = BESExtension;
           this.localResourceManagerType = localResourceManagerType;
           this._any = _any;
    }


    /**
     * Gets the basicResourceAttributesDocument value for this FactoryResourceAttributesDocumentType.
     * 
     * @return basicResourceAttributesDocument
     */
    public org.ggf.bes.factory.BasicResourceAttributesDocumentType getBasicResourceAttributesDocument() {
        return basicResourceAttributesDocument;
    }


    /**
     * Sets the basicResourceAttributesDocument value for this FactoryResourceAttributesDocumentType.
     * 
     * @param basicResourceAttributesDocument
     */
    public void setBasicResourceAttributesDocument(org.ggf.bes.factory.BasicResourceAttributesDocumentType basicResourceAttributesDocument) {
        this.basicResourceAttributesDocument = basicResourceAttributesDocument;
    }


    /**
     * Gets the isAcceptingNewActivities value for this FactoryResourceAttributesDocumentType.
     * 
     * @return isAcceptingNewActivities
     */
    public boolean isIsAcceptingNewActivities() {
        return isAcceptingNewActivities;
    }


    /**
     * Sets the isAcceptingNewActivities value for this FactoryResourceAttributesDocumentType.
     * 
     * @param isAcceptingNewActivities
     */
    public void setIsAcceptingNewActivities(boolean isAcceptingNewActivities) {
        this.isAcceptingNewActivities = isAcceptingNewActivities;
    }


    /**
     * Gets the commonName value for this FactoryResourceAttributesDocumentType.
     * 
     * @return commonName
     */
    public java.lang.String getCommonName() {
        return commonName;
    }


    /**
     * Sets the commonName value for this FactoryResourceAttributesDocumentType.
     * 
     * @param commonName
     */
    public void setCommonName(java.lang.String commonName) {
        this.commonName = commonName;
    }


    /**
     * Gets the longDescription value for this FactoryResourceAttributesDocumentType.
     * 
     * @return longDescription
     */
    public java.lang.String getLongDescription() {
        return longDescription;
    }


    /**
     * Sets the longDescription value for this FactoryResourceAttributesDocumentType.
     * 
     * @param longDescription
     */
    public void setLongDescription(java.lang.String longDescription) {
        this.longDescription = longDescription;
    }


    /**
     * Gets the totalNumberOfActivities value for this FactoryResourceAttributesDocumentType.
     * 
     * @return totalNumberOfActivities
     */
    public long getTotalNumberOfActivities() {
        return totalNumberOfActivities;
    }


    /**
     * Sets the totalNumberOfActivities value for this FactoryResourceAttributesDocumentType.
     * 
     * @param totalNumberOfActivities
     */
    public void setTotalNumberOfActivities(long totalNumberOfActivities) {
        this.totalNumberOfActivities = totalNumberOfActivities;
    }


    /**
     * Gets the activityReference value for this FactoryResourceAttributesDocumentType.
     * 
     * @return activityReference
     */
    public org.ws.addressing.EndpointReferenceType[] getActivityReference() {
        return activityReference;
    }


    /**
     * Sets the activityReference value for this FactoryResourceAttributesDocumentType.
     * 
     * @param activityReference
     */
    public void setActivityReference(org.ws.addressing.EndpointReferenceType[] activityReference) {
        this.activityReference = activityReference;
    }

    public org.ws.addressing.EndpointReferenceType getActivityReference(int i) {
        return this.activityReference[i];
    }

    public void setActivityReference(int i, org.ws.addressing.EndpointReferenceType _value) {
        this.activityReference[i] = _value;
    }


    /**
     * Gets the totalNumberOfContainedResources value for this FactoryResourceAttributesDocumentType.
     * 
     * @return totalNumberOfContainedResources
     */
    public long getTotalNumberOfContainedResources() {
        return totalNumberOfContainedResources;
    }


    /**
     * Sets the totalNumberOfContainedResources value for this FactoryResourceAttributesDocumentType.
     * 
     * @param totalNumberOfContainedResources
     */
    public void setTotalNumberOfContainedResources(long totalNumberOfContainedResources) {
        this.totalNumberOfContainedResources = totalNumberOfContainedResources;
    }


    /**
     * Gets the containedResource value for this FactoryResourceAttributesDocumentType.
     * 
     * @return containedResource
     */
    public java.lang.Object[] getContainedResource() {
        return containedResource;
    }


    /**
     * Sets the containedResource value for this FactoryResourceAttributesDocumentType.
     * 
     * @param containedResource
     */
    public void setContainedResource(java.lang.Object[] containedResource) {
        this.containedResource = containedResource;
    }

    public java.lang.Object getContainedResource(int i) {
        return this.containedResource[i];
    }

    public void setContainedResource(int i, java.lang.Object _value) {
        this.containedResource[i] = _value;
    }


    /**
     * Gets the namingProfile value for this FactoryResourceAttributesDocumentType.
     * 
     * @return namingProfile
     */
    public org.apache.axis.types.URI[] getNamingProfile() {
        return namingProfile;
    }


    /**
     * Sets the namingProfile value for this FactoryResourceAttributesDocumentType.
     * 
     * @param namingProfile
     */
    public void setNamingProfile(org.apache.axis.types.URI[] namingProfile) {
        this.namingProfile = namingProfile;
    }

    public org.apache.axis.types.URI getNamingProfile(int i) {
        return this.namingProfile[i];
    }

    public void setNamingProfile(int i, org.apache.axis.types.URI _value) {
        this.namingProfile[i] = _value;
    }


    /**
     * Gets the BESExtension value for this FactoryResourceAttributesDocumentType.
     * 
     * @return BESExtension
     */
    public org.apache.axis.types.URI[] getBESExtension() {
        return BESExtension;
    }


    /**
     * Sets the BESExtension value for this FactoryResourceAttributesDocumentType.
     * 
     * @param BESExtension
     */
    public void setBESExtension(org.apache.axis.types.URI[] BESExtension) {
        this.BESExtension = BESExtension;
    }

    public org.apache.axis.types.URI getBESExtension(int i) {
        return this.BESExtension[i];
    }

    public void setBESExtension(int i, org.apache.axis.types.URI _value) {
        this.BESExtension[i] = _value;
    }


    /**
     * Gets the localResourceManagerType value for this FactoryResourceAttributesDocumentType.
     * 
     * @return localResourceManagerType
     */
    public org.apache.axis.types.URI getLocalResourceManagerType() {
        return localResourceManagerType;
    }


    /**
     * Sets the localResourceManagerType value for this FactoryResourceAttributesDocumentType.
     * 
     * @param localResourceManagerType
     */
    public void setLocalResourceManagerType(org.apache.axis.types.URI localResourceManagerType) {
        this.localResourceManagerType = localResourceManagerType;
    }


    /**
     * Gets the _any value for this FactoryResourceAttributesDocumentType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this FactoryResourceAttributesDocumentType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FactoryResourceAttributesDocumentType)) return false;
        FactoryResourceAttributesDocumentType other = (FactoryResourceAttributesDocumentType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.basicResourceAttributesDocument==null && other.getBasicResourceAttributesDocument()==null) || 
             (this.basicResourceAttributesDocument!=null &&
              this.basicResourceAttributesDocument.equals(other.getBasicResourceAttributesDocument()))) &&
            this.isAcceptingNewActivities == other.isIsAcceptingNewActivities() &&
            ((this.commonName==null && other.getCommonName()==null) || 
             (this.commonName!=null &&
              this.commonName.equals(other.getCommonName()))) &&
            ((this.longDescription==null && other.getLongDescription()==null) || 
             (this.longDescription!=null &&
              this.longDescription.equals(other.getLongDescription()))) &&
            this.totalNumberOfActivities == other.getTotalNumberOfActivities() &&
            ((this.activityReference==null && other.getActivityReference()==null) || 
             (this.activityReference!=null &&
              java.util.Arrays.equals(this.activityReference, other.getActivityReference()))) &&
            this.totalNumberOfContainedResources == other.getTotalNumberOfContainedResources() &&
            ((this.containedResource==null && other.getContainedResource()==null) || 
             (this.containedResource!=null &&
              java.util.Arrays.equals(this.containedResource, other.getContainedResource()))) &&
            ((this.namingProfile==null && other.getNamingProfile()==null) || 
             (this.namingProfile!=null &&
              java.util.Arrays.equals(this.namingProfile, other.getNamingProfile()))) &&
            ((this.BESExtension==null && other.getBESExtension()==null) || 
             (this.BESExtension!=null &&
              java.util.Arrays.equals(this.BESExtension, other.getBESExtension()))) &&
            ((this.localResourceManagerType==null && other.getLocalResourceManagerType()==null) || 
             (this.localResourceManagerType!=null &&
              this.localResourceManagerType.equals(other.getLocalResourceManagerType()))) &&
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
        if (getBasicResourceAttributesDocument() != null) {
            _hashCode += getBasicResourceAttributesDocument().hashCode();
        }
        _hashCode += (isIsAcceptingNewActivities() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getCommonName() != null) {
            _hashCode += getCommonName().hashCode();
        }
        if (getLongDescription() != null) {
            _hashCode += getLongDescription().hashCode();
        }
        _hashCode += new Long(getTotalNumberOfActivities()).hashCode();
        if (getActivityReference() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getActivityReference());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getActivityReference(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += new Long(getTotalNumberOfContainedResources()).hashCode();
        if (getContainedResource() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getContainedResource());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getContainedResource(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getNamingProfile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNamingProfile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNamingProfile(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getBESExtension() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBESExtension());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBESExtension(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLocalResourceManagerType() != null) {
            _hashCode += getLocalResourceManagerType().hashCode();
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
        new org.apache.axis.description.TypeDesc(FactoryResourceAttributesDocumentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "FactoryResourceAttributesDocumentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("basicResourceAttributesDocument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "BasicResourceAttributesDocument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "BasicResourceAttributesDocumentType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isAcceptingNewActivities");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "IsAcceptingNewActivities"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("commonName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "CommonName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("longDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "LongDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalNumberOfActivities");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "TotalNumberOfActivities"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("activityReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ActivityReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalNumberOfContainedResources");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "TotalNumberOfContainedResources"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containedResource");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "ContainedResource"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("namingProfile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "NamingProfile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("BESExtension");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "BESExtension"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("localResourceManagerType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "LocalResourceManagerType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
