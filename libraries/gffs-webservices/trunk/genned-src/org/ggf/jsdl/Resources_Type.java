/**
 * Resources_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl;

public class Resources_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private java.lang.String[] candidateHosts;

    private org.ggf.jsdl.FileSystem_Type[] fileSystem;

    private java.lang.Boolean exclusiveExecution;

    private org.ggf.jsdl.OperatingSystem_Type operatingSystem;

    private org.ggf.jsdl.CPUArchitecture_Type CPUArchitecture;

    private org.ggf.jsdl.GPUArchitecture_Type GPUArchitecture;

    private org.ggf.jsdl.RangeValue_Type GPUCountPerNode;

    private org.ggf.jsdl.RangeValue_Type GPUMemoryPerNode;

    private org.ggf.jsdl.RangeValue_Type individualCPUSpeed;

    private org.ggf.jsdl.RangeValue_Type individualCPUTime;

    private org.ggf.jsdl.RangeValue_Type individualCPUCount;

    private org.ggf.jsdl.RangeValue_Type individualNetworkBandwidth;

    private org.ggf.jsdl.RangeValue_Type individualPhysicalMemory;

    private org.ggf.jsdl.RangeValue_Type individualVirtualMemory;

    private org.ggf.jsdl.RangeValue_Type individualDiskSpace;

    private org.ggf.jsdl.RangeValue_Type totalCPUTime;

    private org.ggf.jsdl.RangeValue_Type totalCPUCount;

    private org.ggf.jsdl.RangeValue_Type totalPhysicalMemory;

    private org.ggf.jsdl.RangeValue_Type totalVirtualMemory;

    private org.ggf.jsdl.RangeValue_Type totalDiskSpace;

    private org.ggf.jsdl.RangeValue_Type totalResourceCount;

    private org.apache.axis.message.MessageElement [] _any;

    public Resources_Type() {
    }

    public Resources_Type(
           java.lang.String[] candidateHosts,
           org.ggf.jsdl.FileSystem_Type[] fileSystem,
           java.lang.Boolean exclusiveExecution,
           org.ggf.jsdl.OperatingSystem_Type operatingSystem,
           org.ggf.jsdl.CPUArchitecture_Type CPUArchitecture,
           org.ggf.jsdl.GPUArchitecture_Type GPUArchitecture,
           org.ggf.jsdl.RangeValue_Type GPUCountPerNode,
           org.ggf.jsdl.RangeValue_Type GPUMemoryPerNode,
           org.ggf.jsdl.RangeValue_Type individualCPUSpeed,
           org.ggf.jsdl.RangeValue_Type individualCPUTime,
           org.ggf.jsdl.RangeValue_Type individualCPUCount,
           org.ggf.jsdl.RangeValue_Type individualNetworkBandwidth,
           org.ggf.jsdl.RangeValue_Type individualPhysicalMemory,
           org.ggf.jsdl.RangeValue_Type individualVirtualMemory,
           org.ggf.jsdl.RangeValue_Type individualDiskSpace,
           org.ggf.jsdl.RangeValue_Type totalCPUTime,
           org.ggf.jsdl.RangeValue_Type totalCPUCount,
           org.ggf.jsdl.RangeValue_Type totalPhysicalMemory,
           org.ggf.jsdl.RangeValue_Type totalVirtualMemory,
           org.ggf.jsdl.RangeValue_Type totalDiskSpace,
           org.ggf.jsdl.RangeValue_Type totalResourceCount,
           org.apache.axis.message.MessageElement [] _any) {
           this.candidateHosts = candidateHosts;
           this.fileSystem = fileSystem;
           this.exclusiveExecution = exclusiveExecution;
           this.operatingSystem = operatingSystem;
           this.CPUArchitecture = CPUArchitecture;
           this.GPUArchitecture = GPUArchitecture;
           this.GPUCountPerNode = GPUCountPerNode;
           this.GPUMemoryPerNode = GPUMemoryPerNode;
           this.individualCPUSpeed = individualCPUSpeed;
           this.individualCPUTime = individualCPUTime;
           this.individualCPUCount = individualCPUCount;
           this.individualNetworkBandwidth = individualNetworkBandwidth;
           this.individualPhysicalMemory = individualPhysicalMemory;
           this.individualVirtualMemory = individualVirtualMemory;
           this.individualDiskSpace = individualDiskSpace;
           this.totalCPUTime = totalCPUTime;
           this.totalCPUCount = totalCPUCount;
           this.totalPhysicalMemory = totalPhysicalMemory;
           this.totalVirtualMemory = totalVirtualMemory;
           this.totalDiskSpace = totalDiskSpace;
           this.totalResourceCount = totalResourceCount;
           this._any = _any;
    }


    /**
     * Gets the candidateHosts value for this Resources_Type.
     * 
     * @return candidateHosts
     */
    public java.lang.String[] getCandidateHosts() {
        return candidateHosts;
    }


    /**
     * Sets the candidateHosts value for this Resources_Type.
     * 
     * @param candidateHosts
     */
    public void setCandidateHosts(java.lang.String[] candidateHosts) {
        this.candidateHosts = candidateHosts;
    }


    /**
     * Gets the fileSystem value for this Resources_Type.
     * 
     * @return fileSystem
     */
    public org.ggf.jsdl.FileSystem_Type[] getFileSystem() {
        return fileSystem;
    }


    /**
     * Sets the fileSystem value for this Resources_Type.
     * 
     * @param fileSystem
     */
    public void setFileSystem(org.ggf.jsdl.FileSystem_Type[] fileSystem) {
        this.fileSystem = fileSystem;
    }

    public org.ggf.jsdl.FileSystem_Type getFileSystem(int i) {
        return this.fileSystem[i];
    }

    public void setFileSystem(int i, org.ggf.jsdl.FileSystem_Type _value) {
        this.fileSystem[i] = _value;
    }


    /**
     * Gets the exclusiveExecution value for this Resources_Type.
     * 
     * @return exclusiveExecution
     */
    public java.lang.Boolean getExclusiveExecution() {
        return exclusiveExecution;
    }


    /**
     * Sets the exclusiveExecution value for this Resources_Type.
     * 
     * @param exclusiveExecution
     */
    public void setExclusiveExecution(java.lang.Boolean exclusiveExecution) {
        this.exclusiveExecution = exclusiveExecution;
    }


    /**
     * Gets the operatingSystem value for this Resources_Type.
     * 
     * @return operatingSystem
     */
    public org.ggf.jsdl.OperatingSystem_Type getOperatingSystem() {
        return operatingSystem;
    }


    /**
     * Sets the operatingSystem value for this Resources_Type.
     * 
     * @param operatingSystem
     */
    public void setOperatingSystem(org.ggf.jsdl.OperatingSystem_Type operatingSystem) {
        this.operatingSystem = operatingSystem;
    }


    /**
     * Gets the CPUArchitecture value for this Resources_Type.
     * 
     * @return CPUArchitecture
     */
    public org.ggf.jsdl.CPUArchitecture_Type getCPUArchitecture() {
        return CPUArchitecture;
    }


    /**
     * Sets the CPUArchitecture value for this Resources_Type.
     * 
     * @param CPUArchitecture
     */
    public void setCPUArchitecture(org.ggf.jsdl.CPUArchitecture_Type CPUArchitecture) {
        this.CPUArchitecture = CPUArchitecture;
    }


    /**
     * Gets the GPUArchitecture value for this Resources_Type.
     * 
     * @return GPUArchitecture
     */
    public org.ggf.jsdl.GPUArchitecture_Type getGPUArchitecture() {
        return GPUArchitecture;
    }


    /**
     * Sets the GPUArchitecture value for this Resources_Type.
     * 
     * @param GPUArchitecture
     */
    public void setGPUArchitecture(org.ggf.jsdl.GPUArchitecture_Type GPUArchitecture) {
        this.GPUArchitecture = GPUArchitecture;
    }


    /**
     * Gets the GPUCountPerNode value for this Resources_Type.
     * 
     * @return GPUCountPerNode
     */
    public org.ggf.jsdl.RangeValue_Type getGPUCountPerNode() {
        return GPUCountPerNode;
    }


    /**
     * Sets the GPUCountPerNode value for this Resources_Type.
     * 
     * @param GPUCountPerNode
     */
    public void setGPUCountPerNode(org.ggf.jsdl.RangeValue_Type GPUCountPerNode) {
        this.GPUCountPerNode = GPUCountPerNode;
    }


    /**
     * Gets the GPUMemoryPerNode value for this Resources_Type.
     * 
     * @return GPUMemoryPerNode
     */
    public org.ggf.jsdl.RangeValue_Type getGPUMemoryPerNode() {
        return GPUMemoryPerNode;
    }


    /**
     * Sets the GPUMemoryPerNode value for this Resources_Type.
     * 
     * @param GPUMemoryPerNode
     */
    public void setGPUMemoryPerNode(org.ggf.jsdl.RangeValue_Type GPUMemoryPerNode) {
        this.GPUMemoryPerNode = GPUMemoryPerNode;
    }


    /**
     * Gets the individualCPUSpeed value for this Resources_Type.
     * 
     * @return individualCPUSpeed
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualCPUSpeed() {
        return individualCPUSpeed;
    }


    /**
     * Sets the individualCPUSpeed value for this Resources_Type.
     * 
     * @param individualCPUSpeed
     */
    public void setIndividualCPUSpeed(org.ggf.jsdl.RangeValue_Type individualCPUSpeed) {
        this.individualCPUSpeed = individualCPUSpeed;
    }


    /**
     * Gets the individualCPUTime value for this Resources_Type.
     * 
     * @return individualCPUTime
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualCPUTime() {
        return individualCPUTime;
    }


    /**
     * Sets the individualCPUTime value for this Resources_Type.
     * 
     * @param individualCPUTime
     */
    public void setIndividualCPUTime(org.ggf.jsdl.RangeValue_Type individualCPUTime) {
        this.individualCPUTime = individualCPUTime;
    }


    /**
     * Gets the individualCPUCount value for this Resources_Type.
     * 
     * @return individualCPUCount
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualCPUCount() {
        return individualCPUCount;
    }


    /**
     * Sets the individualCPUCount value for this Resources_Type.
     * 
     * @param individualCPUCount
     */
    public void setIndividualCPUCount(org.ggf.jsdl.RangeValue_Type individualCPUCount) {
        this.individualCPUCount = individualCPUCount;
    }


    /**
     * Gets the individualNetworkBandwidth value for this Resources_Type.
     * 
     * @return individualNetworkBandwidth
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualNetworkBandwidth() {
        return individualNetworkBandwidth;
    }


    /**
     * Sets the individualNetworkBandwidth value for this Resources_Type.
     * 
     * @param individualNetworkBandwidth
     */
    public void setIndividualNetworkBandwidth(org.ggf.jsdl.RangeValue_Type individualNetworkBandwidth) {
        this.individualNetworkBandwidth = individualNetworkBandwidth;
    }


    /**
     * Gets the individualPhysicalMemory value for this Resources_Type.
     * 
     * @return individualPhysicalMemory
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualPhysicalMemory() {
        return individualPhysicalMemory;
    }


    /**
     * Sets the individualPhysicalMemory value for this Resources_Type.
     * 
     * @param individualPhysicalMemory
     */
    public void setIndividualPhysicalMemory(org.ggf.jsdl.RangeValue_Type individualPhysicalMemory) {
        this.individualPhysicalMemory = individualPhysicalMemory;
    }


    /**
     * Gets the individualVirtualMemory value for this Resources_Type.
     * 
     * @return individualVirtualMemory
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualVirtualMemory() {
        return individualVirtualMemory;
    }


    /**
     * Sets the individualVirtualMemory value for this Resources_Type.
     * 
     * @param individualVirtualMemory
     */
    public void setIndividualVirtualMemory(org.ggf.jsdl.RangeValue_Type individualVirtualMemory) {
        this.individualVirtualMemory = individualVirtualMemory;
    }


    /**
     * Gets the individualDiskSpace value for this Resources_Type.
     * 
     * @return individualDiskSpace
     */
    public org.ggf.jsdl.RangeValue_Type getIndividualDiskSpace() {
        return individualDiskSpace;
    }


    /**
     * Sets the individualDiskSpace value for this Resources_Type.
     * 
     * @param individualDiskSpace
     */
    public void setIndividualDiskSpace(org.ggf.jsdl.RangeValue_Type individualDiskSpace) {
        this.individualDiskSpace = individualDiskSpace;
    }


    /**
     * Gets the totalCPUTime value for this Resources_Type.
     * 
     * @return totalCPUTime
     */
    public org.ggf.jsdl.RangeValue_Type getTotalCPUTime() {
        return totalCPUTime;
    }


    /**
     * Sets the totalCPUTime value for this Resources_Type.
     * 
     * @param totalCPUTime
     */
    public void setTotalCPUTime(org.ggf.jsdl.RangeValue_Type totalCPUTime) {
        this.totalCPUTime = totalCPUTime;
    }


    /**
     * Gets the totalCPUCount value for this Resources_Type.
     * 
     * @return totalCPUCount
     */
    public org.ggf.jsdl.RangeValue_Type getTotalCPUCount() {
        return totalCPUCount;
    }


    /**
     * Sets the totalCPUCount value for this Resources_Type.
     * 
     * @param totalCPUCount
     */
    public void setTotalCPUCount(org.ggf.jsdl.RangeValue_Type totalCPUCount) {
        this.totalCPUCount = totalCPUCount;
    }


    /**
     * Gets the totalPhysicalMemory value for this Resources_Type.
     * 
     * @return totalPhysicalMemory
     */
    public org.ggf.jsdl.RangeValue_Type getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }


    /**
     * Sets the totalPhysicalMemory value for this Resources_Type.
     * 
     * @param totalPhysicalMemory
     */
    public void setTotalPhysicalMemory(org.ggf.jsdl.RangeValue_Type totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }


    /**
     * Gets the totalVirtualMemory value for this Resources_Type.
     * 
     * @return totalVirtualMemory
     */
    public org.ggf.jsdl.RangeValue_Type getTotalVirtualMemory() {
        return totalVirtualMemory;
    }


    /**
     * Sets the totalVirtualMemory value for this Resources_Type.
     * 
     * @param totalVirtualMemory
     */
    public void setTotalVirtualMemory(org.ggf.jsdl.RangeValue_Type totalVirtualMemory) {
        this.totalVirtualMemory = totalVirtualMemory;
    }


    /**
     * Gets the totalDiskSpace value for this Resources_Type.
     * 
     * @return totalDiskSpace
     */
    public org.ggf.jsdl.RangeValue_Type getTotalDiskSpace() {
        return totalDiskSpace;
    }


    /**
     * Sets the totalDiskSpace value for this Resources_Type.
     * 
     * @param totalDiskSpace
     */
    public void setTotalDiskSpace(org.ggf.jsdl.RangeValue_Type totalDiskSpace) {
        this.totalDiskSpace = totalDiskSpace;
    }


    /**
     * Gets the totalResourceCount value for this Resources_Type.
     * 
     * @return totalResourceCount
     */
    public org.ggf.jsdl.RangeValue_Type getTotalResourceCount() {
        return totalResourceCount;
    }


    /**
     * Sets the totalResourceCount value for this Resources_Type.
     * 
     * @param totalResourceCount
     */
    public void setTotalResourceCount(org.ggf.jsdl.RangeValue_Type totalResourceCount) {
        this.totalResourceCount = totalResourceCount;
    }


    /**
     * Gets the _any value for this Resources_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this Resources_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Resources_Type)) return false;
        Resources_Type other = (Resources_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.candidateHosts==null && other.getCandidateHosts()==null) || 
             (this.candidateHosts!=null &&
              java.util.Arrays.equals(this.candidateHosts, other.getCandidateHosts()))) &&
            ((this.fileSystem==null && other.getFileSystem()==null) || 
             (this.fileSystem!=null &&
              java.util.Arrays.equals(this.fileSystem, other.getFileSystem()))) &&
            ((this.exclusiveExecution==null && other.getExclusiveExecution()==null) || 
             (this.exclusiveExecution!=null &&
              this.exclusiveExecution.equals(other.getExclusiveExecution()))) &&
            ((this.operatingSystem==null && other.getOperatingSystem()==null) || 
             (this.operatingSystem!=null &&
              this.operatingSystem.equals(other.getOperatingSystem()))) &&
            ((this.CPUArchitecture==null && other.getCPUArchitecture()==null) || 
             (this.CPUArchitecture!=null &&
              this.CPUArchitecture.equals(other.getCPUArchitecture()))) &&
            ((this.GPUArchitecture==null && other.getGPUArchitecture()==null) || 
             (this.GPUArchitecture!=null &&
              this.GPUArchitecture.equals(other.getGPUArchitecture()))) &&
            ((this.GPUCountPerNode==null && other.getGPUCountPerNode()==null) || 
             (this.GPUCountPerNode!=null &&
              this.GPUCountPerNode.equals(other.getGPUCountPerNode()))) &&
            ((this.GPUMemoryPerNode==null && other.getGPUMemoryPerNode()==null) || 
             (this.GPUMemoryPerNode!=null &&
              this.GPUMemoryPerNode.equals(other.getGPUMemoryPerNode()))) &&
            ((this.individualCPUSpeed==null && other.getIndividualCPUSpeed()==null) || 
             (this.individualCPUSpeed!=null &&
              this.individualCPUSpeed.equals(other.getIndividualCPUSpeed()))) &&
            ((this.individualCPUTime==null && other.getIndividualCPUTime()==null) || 
             (this.individualCPUTime!=null &&
              this.individualCPUTime.equals(other.getIndividualCPUTime()))) &&
            ((this.individualCPUCount==null && other.getIndividualCPUCount()==null) || 
             (this.individualCPUCount!=null &&
              this.individualCPUCount.equals(other.getIndividualCPUCount()))) &&
            ((this.individualNetworkBandwidth==null && other.getIndividualNetworkBandwidth()==null) || 
             (this.individualNetworkBandwidth!=null &&
              this.individualNetworkBandwidth.equals(other.getIndividualNetworkBandwidth()))) &&
            ((this.individualPhysicalMemory==null && other.getIndividualPhysicalMemory()==null) || 
             (this.individualPhysicalMemory!=null &&
              this.individualPhysicalMemory.equals(other.getIndividualPhysicalMemory()))) &&
            ((this.individualVirtualMemory==null && other.getIndividualVirtualMemory()==null) || 
             (this.individualVirtualMemory!=null &&
              this.individualVirtualMemory.equals(other.getIndividualVirtualMemory()))) &&
            ((this.individualDiskSpace==null && other.getIndividualDiskSpace()==null) || 
             (this.individualDiskSpace!=null &&
              this.individualDiskSpace.equals(other.getIndividualDiskSpace()))) &&
            ((this.totalCPUTime==null && other.getTotalCPUTime()==null) || 
             (this.totalCPUTime!=null &&
              this.totalCPUTime.equals(other.getTotalCPUTime()))) &&
            ((this.totalCPUCount==null && other.getTotalCPUCount()==null) || 
             (this.totalCPUCount!=null &&
              this.totalCPUCount.equals(other.getTotalCPUCount()))) &&
            ((this.totalPhysicalMemory==null && other.getTotalPhysicalMemory()==null) || 
             (this.totalPhysicalMemory!=null &&
              this.totalPhysicalMemory.equals(other.getTotalPhysicalMemory()))) &&
            ((this.totalVirtualMemory==null && other.getTotalVirtualMemory()==null) || 
             (this.totalVirtualMemory!=null &&
              this.totalVirtualMemory.equals(other.getTotalVirtualMemory()))) &&
            ((this.totalDiskSpace==null && other.getTotalDiskSpace()==null) || 
             (this.totalDiskSpace!=null &&
              this.totalDiskSpace.equals(other.getTotalDiskSpace()))) &&
            ((this.totalResourceCount==null && other.getTotalResourceCount()==null) || 
             (this.totalResourceCount!=null &&
              this.totalResourceCount.equals(other.getTotalResourceCount()))) &&
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
        if (getCandidateHosts() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCandidateHosts());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCandidateHosts(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFileSystem() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFileSystem());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFileSystem(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getExclusiveExecution() != null) {
            _hashCode += getExclusiveExecution().hashCode();
        }
        if (getOperatingSystem() != null) {
            _hashCode += getOperatingSystem().hashCode();
        }
        if (getCPUArchitecture() != null) {
            _hashCode += getCPUArchitecture().hashCode();
        }
        if (getGPUArchitecture() != null) {
            _hashCode += getGPUArchitecture().hashCode();
        }
        if (getGPUCountPerNode() != null) {
            _hashCode += getGPUCountPerNode().hashCode();
        }
        if (getGPUMemoryPerNode() != null) {
            _hashCode += getGPUMemoryPerNode().hashCode();
        }
        if (getIndividualCPUSpeed() != null) {
            _hashCode += getIndividualCPUSpeed().hashCode();
        }
        if (getIndividualCPUTime() != null) {
            _hashCode += getIndividualCPUTime().hashCode();
        }
        if (getIndividualCPUCount() != null) {
            _hashCode += getIndividualCPUCount().hashCode();
        }
        if (getIndividualNetworkBandwidth() != null) {
            _hashCode += getIndividualNetworkBandwidth().hashCode();
        }
        if (getIndividualPhysicalMemory() != null) {
            _hashCode += getIndividualPhysicalMemory().hashCode();
        }
        if (getIndividualVirtualMemory() != null) {
            _hashCode += getIndividualVirtualMemory().hashCode();
        }
        if (getIndividualDiskSpace() != null) {
            _hashCode += getIndividualDiskSpace().hashCode();
        }
        if (getTotalCPUTime() != null) {
            _hashCode += getTotalCPUTime().hashCode();
        }
        if (getTotalCPUCount() != null) {
            _hashCode += getTotalCPUCount().hashCode();
        }
        if (getTotalPhysicalMemory() != null) {
            _hashCode += getTotalPhysicalMemory().hashCode();
        }
        if (getTotalVirtualMemory() != null) {
            _hashCode += getTotalVirtualMemory().hashCode();
        }
        if (getTotalDiskSpace() != null) {
            _hashCode += getTotalDiskSpace().hashCode();
        }
        if (getTotalResourceCount() != null) {
            _hashCode += getTotalResourceCount().hashCode();
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
        new org.apache.axis.description.TypeDesc(Resources_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "Resources_Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("candidateHosts");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CandidateHosts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CandidateHosts_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileSystem");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileSystem"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "FileSystem_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exclusiveExecution");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "ExclusiveExecution"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatingSystem");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystem"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "OperatingSystem_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUArchitecture");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitecture"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "CPUArchitecture_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUArchitecture");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitecture"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUArchitecture_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUCountPerNode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUCountPerNode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GPUMemoryPerNode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "GPUMemoryPerNode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualCPUSpeed");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualCPUSpeed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualCPUTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualCPUTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualCPUCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualCPUCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualNetworkBandwidth");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualNetworkBandwidth"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualPhysicalMemory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualPhysicalMemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualVirtualMemory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualVirtualMemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("individualDiskSpace");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "IndividualDiskSpace"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalCPUTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "TotalCPUTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalCPUCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "TotalCPUCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalPhysicalMemory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "TotalPhysicalMemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalVirtualMemory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "TotalVirtualMemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalDiskSpace");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "TotalDiskSpace"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "RangeValue_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalResourceCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl", "TotalResourceCount"));
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
