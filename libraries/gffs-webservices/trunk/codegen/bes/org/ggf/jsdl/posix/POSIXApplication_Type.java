/**
 * POSIXApplication_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl.posix;

public class POSIXApplication_Type  implements java.io.Serializable {
    private org.ggf.jsdl.posix.FileName_Type executable;

    private org.ggf.jsdl.posix.Argument_Type[] argument;

    private org.ggf.jsdl.posix.FileName_Type input;

    private org.ggf.jsdl.posix.FileName_Type output;

    private org.ggf.jsdl.posix.FileName_Type error;

    private org.ggf.jsdl.posix.DirectoryName_Type workingDirectory;

    private org.ggf.jsdl.posix.Environment_Type[] environment;

    private org.ggf.jsdl.posix.Limits_Type wallTimeLimit;

    private org.ggf.jsdl.posix.Limits_Type fileSizeLimit;

    private org.ggf.jsdl.posix.Limits_Type coreDumpLimit;

    private org.ggf.jsdl.posix.Limits_Type dataSegmentLimit;

    private org.ggf.jsdl.posix.Limits_Type lockedMemoryLimit;

    private org.ggf.jsdl.posix.Limits_Type memoryLimit;

    private org.ggf.jsdl.posix.Limits_Type openDescriptorsLimit;

    private org.ggf.jsdl.posix.Limits_Type pipeSizeLimit;

    private org.ggf.jsdl.posix.Limits_Type stackSizeLimit;

    private org.ggf.jsdl.posix.Limits_Type CPUTimeLimit;

    private org.ggf.jsdl.posix.Limits_Type processCountLimit;

    private org.ggf.jsdl.posix.Limits_Type virtualMemoryLimit;

    private org.ggf.jsdl.posix.Limits_Type threadCountLimit;

    private org.ggf.jsdl.posix.UserName_Type userName;

    private org.ggf.jsdl.posix.GroupName_Type groupName;

    private org.apache.axis.types.NCName name;  // attribute

    public POSIXApplication_Type() {
    }

    public POSIXApplication_Type(
           org.ggf.jsdl.posix.FileName_Type executable,
           org.ggf.jsdl.posix.Argument_Type[] argument,
           org.ggf.jsdl.posix.FileName_Type input,
           org.ggf.jsdl.posix.FileName_Type output,
           org.ggf.jsdl.posix.FileName_Type error,
           org.ggf.jsdl.posix.DirectoryName_Type workingDirectory,
           org.ggf.jsdl.posix.Environment_Type[] environment,
           org.ggf.jsdl.posix.Limits_Type wallTimeLimit,
           org.ggf.jsdl.posix.Limits_Type fileSizeLimit,
           org.ggf.jsdl.posix.Limits_Type coreDumpLimit,
           org.ggf.jsdl.posix.Limits_Type dataSegmentLimit,
           org.ggf.jsdl.posix.Limits_Type lockedMemoryLimit,
           org.ggf.jsdl.posix.Limits_Type memoryLimit,
           org.ggf.jsdl.posix.Limits_Type openDescriptorsLimit,
           org.ggf.jsdl.posix.Limits_Type pipeSizeLimit,
           org.ggf.jsdl.posix.Limits_Type stackSizeLimit,
           org.ggf.jsdl.posix.Limits_Type CPUTimeLimit,
           org.ggf.jsdl.posix.Limits_Type processCountLimit,
           org.ggf.jsdl.posix.Limits_Type virtualMemoryLimit,
           org.ggf.jsdl.posix.Limits_Type threadCountLimit,
           org.ggf.jsdl.posix.UserName_Type userName,
           org.ggf.jsdl.posix.GroupName_Type groupName,
           org.apache.axis.types.NCName name) {
           this.executable = executable;
           this.argument = argument;
           this.input = input;
           this.output = output;
           this.error = error;
           this.workingDirectory = workingDirectory;
           this.environment = environment;
           this.wallTimeLimit = wallTimeLimit;
           this.fileSizeLimit = fileSizeLimit;
           this.coreDumpLimit = coreDumpLimit;
           this.dataSegmentLimit = dataSegmentLimit;
           this.lockedMemoryLimit = lockedMemoryLimit;
           this.memoryLimit = memoryLimit;
           this.openDescriptorsLimit = openDescriptorsLimit;
           this.pipeSizeLimit = pipeSizeLimit;
           this.stackSizeLimit = stackSizeLimit;
           this.CPUTimeLimit = CPUTimeLimit;
           this.processCountLimit = processCountLimit;
           this.virtualMemoryLimit = virtualMemoryLimit;
           this.threadCountLimit = threadCountLimit;
           this.userName = userName;
           this.groupName = groupName;
           this.name = name;
    }


    /**
     * Gets the executable value for this POSIXApplication_Type.
     * 
     * @return executable
     */
    public org.ggf.jsdl.posix.FileName_Type getExecutable() {
        return executable;
    }


    /**
     * Sets the executable value for this POSIXApplication_Type.
     * 
     * @param executable
     */
    public void setExecutable(org.ggf.jsdl.posix.FileName_Type executable) {
        this.executable = executable;
    }


    /**
     * Gets the argument value for this POSIXApplication_Type.
     * 
     * @return argument
     */
    public org.ggf.jsdl.posix.Argument_Type[] getArgument() {
        return argument;
    }


    /**
     * Sets the argument value for this POSIXApplication_Type.
     * 
     * @param argument
     */
    public void setArgument(org.ggf.jsdl.posix.Argument_Type[] argument) {
        this.argument = argument;
    }

    public org.ggf.jsdl.posix.Argument_Type getArgument(int i) {
        return this.argument[i];
    }

    public void setArgument(int i, org.ggf.jsdl.posix.Argument_Type _value) {
        this.argument[i] = _value;
    }


    /**
     * Gets the input value for this POSIXApplication_Type.
     * 
     * @return input
     */
    public org.ggf.jsdl.posix.FileName_Type getInput() {
        return input;
    }


    /**
     * Sets the input value for this POSIXApplication_Type.
     * 
     * @param input
     */
    public void setInput(org.ggf.jsdl.posix.FileName_Type input) {
        this.input = input;
    }


    /**
     * Gets the output value for this POSIXApplication_Type.
     * 
     * @return output
     */
    public org.ggf.jsdl.posix.FileName_Type getOutput() {
        return output;
    }


    /**
     * Sets the output value for this POSIXApplication_Type.
     * 
     * @param output
     */
    public void setOutput(org.ggf.jsdl.posix.FileName_Type output) {
        this.output = output;
    }


    /**
     * Gets the error value for this POSIXApplication_Type.
     * 
     * @return error
     */
    public org.ggf.jsdl.posix.FileName_Type getError() {
        return error;
    }


    /**
     * Sets the error value for this POSIXApplication_Type.
     * 
     * @param error
     */
    public void setError(org.ggf.jsdl.posix.FileName_Type error) {
        this.error = error;
    }


    /**
     * Gets the workingDirectory value for this POSIXApplication_Type.
     * 
     * @return workingDirectory
     */
    public org.ggf.jsdl.posix.DirectoryName_Type getWorkingDirectory() {
        return workingDirectory;
    }


    /**
     * Sets the workingDirectory value for this POSIXApplication_Type.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(org.ggf.jsdl.posix.DirectoryName_Type workingDirectory) {
        this.workingDirectory = workingDirectory;
    }


    /**
     * Gets the environment value for this POSIXApplication_Type.
     * 
     * @return environment
     */
    public org.ggf.jsdl.posix.Environment_Type[] getEnvironment() {
        return environment;
    }


    /**
     * Sets the environment value for this POSIXApplication_Type.
     * 
     * @param environment
     */
    public void setEnvironment(org.ggf.jsdl.posix.Environment_Type[] environment) {
        this.environment = environment;
    }

    public org.ggf.jsdl.posix.Environment_Type getEnvironment(int i) {
        return this.environment[i];
    }

    public void setEnvironment(int i, org.ggf.jsdl.posix.Environment_Type _value) {
        this.environment[i] = _value;
    }


    /**
     * Gets the wallTimeLimit value for this POSIXApplication_Type.
     * 
     * @return wallTimeLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getWallTimeLimit() {
        return wallTimeLimit;
    }


    /**
     * Sets the wallTimeLimit value for this POSIXApplication_Type.
     * 
     * @param wallTimeLimit
     */
    public void setWallTimeLimit(org.ggf.jsdl.posix.Limits_Type wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }


    /**
     * Gets the fileSizeLimit value for this POSIXApplication_Type.
     * 
     * @return fileSizeLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getFileSizeLimit() {
        return fileSizeLimit;
    }


    /**
     * Sets the fileSizeLimit value for this POSIXApplication_Type.
     * 
     * @param fileSizeLimit
     */
    public void setFileSizeLimit(org.ggf.jsdl.posix.Limits_Type fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
    }


    /**
     * Gets the coreDumpLimit value for this POSIXApplication_Type.
     * 
     * @return coreDumpLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getCoreDumpLimit() {
        return coreDumpLimit;
    }


    /**
     * Sets the coreDumpLimit value for this POSIXApplication_Type.
     * 
     * @param coreDumpLimit
     */
    public void setCoreDumpLimit(org.ggf.jsdl.posix.Limits_Type coreDumpLimit) {
        this.coreDumpLimit = coreDumpLimit;
    }


    /**
     * Gets the dataSegmentLimit value for this POSIXApplication_Type.
     * 
     * @return dataSegmentLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getDataSegmentLimit() {
        return dataSegmentLimit;
    }


    /**
     * Sets the dataSegmentLimit value for this POSIXApplication_Type.
     * 
     * @param dataSegmentLimit
     */
    public void setDataSegmentLimit(org.ggf.jsdl.posix.Limits_Type dataSegmentLimit) {
        this.dataSegmentLimit = dataSegmentLimit;
    }


    /**
     * Gets the lockedMemoryLimit value for this POSIXApplication_Type.
     * 
     * @return lockedMemoryLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getLockedMemoryLimit() {
        return lockedMemoryLimit;
    }


    /**
     * Sets the lockedMemoryLimit value for this POSIXApplication_Type.
     * 
     * @param lockedMemoryLimit
     */
    public void setLockedMemoryLimit(org.ggf.jsdl.posix.Limits_Type lockedMemoryLimit) {
        this.lockedMemoryLimit = lockedMemoryLimit;
    }


    /**
     * Gets the memoryLimit value for this POSIXApplication_Type.
     * 
     * @return memoryLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getMemoryLimit() {
        return memoryLimit;
    }


    /**
     * Sets the memoryLimit value for this POSIXApplication_Type.
     * 
     * @param memoryLimit
     */
    public void setMemoryLimit(org.ggf.jsdl.posix.Limits_Type memoryLimit) {
        this.memoryLimit = memoryLimit;
    }


    /**
     * Gets the openDescriptorsLimit value for this POSIXApplication_Type.
     * 
     * @return openDescriptorsLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getOpenDescriptorsLimit() {
        return openDescriptorsLimit;
    }


    /**
     * Sets the openDescriptorsLimit value for this POSIXApplication_Type.
     * 
     * @param openDescriptorsLimit
     */
    public void setOpenDescriptorsLimit(org.ggf.jsdl.posix.Limits_Type openDescriptorsLimit) {
        this.openDescriptorsLimit = openDescriptorsLimit;
    }


    /**
     * Gets the pipeSizeLimit value for this POSIXApplication_Type.
     * 
     * @return pipeSizeLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getPipeSizeLimit() {
        return pipeSizeLimit;
    }


    /**
     * Sets the pipeSizeLimit value for this POSIXApplication_Type.
     * 
     * @param pipeSizeLimit
     */
    public void setPipeSizeLimit(org.ggf.jsdl.posix.Limits_Type pipeSizeLimit) {
        this.pipeSizeLimit = pipeSizeLimit;
    }


    /**
     * Gets the stackSizeLimit value for this POSIXApplication_Type.
     * 
     * @return stackSizeLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getStackSizeLimit() {
        return stackSizeLimit;
    }


    /**
     * Sets the stackSizeLimit value for this POSIXApplication_Type.
     * 
     * @param stackSizeLimit
     */
    public void setStackSizeLimit(org.ggf.jsdl.posix.Limits_Type stackSizeLimit) {
        this.stackSizeLimit = stackSizeLimit;
    }


    /**
     * Gets the CPUTimeLimit value for this POSIXApplication_Type.
     * 
     * @return CPUTimeLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getCPUTimeLimit() {
        return CPUTimeLimit;
    }


    /**
     * Sets the CPUTimeLimit value for this POSIXApplication_Type.
     * 
     * @param CPUTimeLimit
     */
    public void setCPUTimeLimit(org.ggf.jsdl.posix.Limits_Type CPUTimeLimit) {
        this.CPUTimeLimit = CPUTimeLimit;
    }


    /**
     * Gets the processCountLimit value for this POSIXApplication_Type.
     * 
     * @return processCountLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getProcessCountLimit() {
        return processCountLimit;
    }


    /**
     * Sets the processCountLimit value for this POSIXApplication_Type.
     * 
     * @param processCountLimit
     */
    public void setProcessCountLimit(org.ggf.jsdl.posix.Limits_Type processCountLimit) {
        this.processCountLimit = processCountLimit;
    }


    /**
     * Gets the virtualMemoryLimit value for this POSIXApplication_Type.
     * 
     * @return virtualMemoryLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getVirtualMemoryLimit() {
        return virtualMemoryLimit;
    }


    /**
     * Sets the virtualMemoryLimit value for this POSIXApplication_Type.
     * 
     * @param virtualMemoryLimit
     */
    public void setVirtualMemoryLimit(org.ggf.jsdl.posix.Limits_Type virtualMemoryLimit) {
        this.virtualMemoryLimit = virtualMemoryLimit;
    }


    /**
     * Gets the threadCountLimit value for this POSIXApplication_Type.
     * 
     * @return threadCountLimit
     */
    public org.ggf.jsdl.posix.Limits_Type getThreadCountLimit() {
        return threadCountLimit;
    }


    /**
     * Sets the threadCountLimit value for this POSIXApplication_Type.
     * 
     * @param threadCountLimit
     */
    public void setThreadCountLimit(org.ggf.jsdl.posix.Limits_Type threadCountLimit) {
        this.threadCountLimit = threadCountLimit;
    }


    /**
     * Gets the userName value for this POSIXApplication_Type.
     * 
     * @return userName
     */
    public org.ggf.jsdl.posix.UserName_Type getUserName() {
        return userName;
    }


    /**
     * Sets the userName value for this POSIXApplication_Type.
     * 
     * @param userName
     */
    public void setUserName(org.ggf.jsdl.posix.UserName_Type userName) {
        this.userName = userName;
    }


    /**
     * Gets the groupName value for this POSIXApplication_Type.
     * 
     * @return groupName
     */
    public org.ggf.jsdl.posix.GroupName_Type getGroupName() {
        return groupName;
    }


    /**
     * Sets the groupName value for this POSIXApplication_Type.
     * 
     * @param groupName
     */
    public void setGroupName(org.ggf.jsdl.posix.GroupName_Type groupName) {
        this.groupName = groupName;
    }


    /**
     * Gets the name value for this POSIXApplication_Type.
     * 
     * @return name
     */
    public org.apache.axis.types.NCName getName() {
        return name;
    }


    /**
     * Sets the name value for this POSIXApplication_Type.
     * 
     * @param name
     */
    public void setName(org.apache.axis.types.NCName name) {
        this.name = name;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof POSIXApplication_Type)) return false;
        POSIXApplication_Type other = (POSIXApplication_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.executable==null && other.getExecutable()==null) || 
             (this.executable!=null &&
              this.executable.equals(other.getExecutable()))) &&
            ((this.argument==null && other.getArgument()==null) || 
             (this.argument!=null &&
              java.util.Arrays.equals(this.argument, other.getArgument()))) &&
            ((this.input==null && other.getInput()==null) || 
             (this.input!=null &&
              this.input.equals(other.getInput()))) &&
            ((this.output==null && other.getOutput()==null) || 
             (this.output!=null &&
              this.output.equals(other.getOutput()))) &&
            ((this.error==null && other.getError()==null) || 
             (this.error!=null &&
              this.error.equals(other.getError()))) &&
            ((this.workingDirectory==null && other.getWorkingDirectory()==null) || 
             (this.workingDirectory!=null &&
              this.workingDirectory.equals(other.getWorkingDirectory()))) &&
            ((this.environment==null && other.getEnvironment()==null) || 
             (this.environment!=null &&
              java.util.Arrays.equals(this.environment, other.getEnvironment()))) &&
            ((this.wallTimeLimit==null && other.getWallTimeLimit()==null) || 
             (this.wallTimeLimit!=null &&
              this.wallTimeLimit.equals(other.getWallTimeLimit()))) &&
            ((this.fileSizeLimit==null && other.getFileSizeLimit()==null) || 
             (this.fileSizeLimit!=null &&
              this.fileSizeLimit.equals(other.getFileSizeLimit()))) &&
            ((this.coreDumpLimit==null && other.getCoreDumpLimit()==null) || 
             (this.coreDumpLimit!=null &&
              this.coreDumpLimit.equals(other.getCoreDumpLimit()))) &&
            ((this.dataSegmentLimit==null && other.getDataSegmentLimit()==null) || 
             (this.dataSegmentLimit!=null &&
              this.dataSegmentLimit.equals(other.getDataSegmentLimit()))) &&
            ((this.lockedMemoryLimit==null && other.getLockedMemoryLimit()==null) || 
             (this.lockedMemoryLimit!=null &&
              this.lockedMemoryLimit.equals(other.getLockedMemoryLimit()))) &&
            ((this.memoryLimit==null && other.getMemoryLimit()==null) || 
             (this.memoryLimit!=null &&
              this.memoryLimit.equals(other.getMemoryLimit()))) &&
            ((this.openDescriptorsLimit==null && other.getOpenDescriptorsLimit()==null) || 
             (this.openDescriptorsLimit!=null &&
              this.openDescriptorsLimit.equals(other.getOpenDescriptorsLimit()))) &&
            ((this.pipeSizeLimit==null && other.getPipeSizeLimit()==null) || 
             (this.pipeSizeLimit!=null &&
              this.pipeSizeLimit.equals(other.getPipeSizeLimit()))) &&
            ((this.stackSizeLimit==null && other.getStackSizeLimit()==null) || 
             (this.stackSizeLimit!=null &&
              this.stackSizeLimit.equals(other.getStackSizeLimit()))) &&
            ((this.CPUTimeLimit==null && other.getCPUTimeLimit()==null) || 
             (this.CPUTimeLimit!=null &&
              this.CPUTimeLimit.equals(other.getCPUTimeLimit()))) &&
            ((this.processCountLimit==null && other.getProcessCountLimit()==null) || 
             (this.processCountLimit!=null &&
              this.processCountLimit.equals(other.getProcessCountLimit()))) &&
            ((this.virtualMemoryLimit==null && other.getVirtualMemoryLimit()==null) || 
             (this.virtualMemoryLimit!=null &&
              this.virtualMemoryLimit.equals(other.getVirtualMemoryLimit()))) &&
            ((this.threadCountLimit==null && other.getThreadCountLimit()==null) || 
             (this.threadCountLimit!=null &&
              this.threadCountLimit.equals(other.getThreadCountLimit()))) &&
            ((this.userName==null && other.getUserName()==null) || 
             (this.userName!=null &&
              this.userName.equals(other.getUserName()))) &&
            ((this.groupName==null && other.getGroupName()==null) || 
             (this.groupName!=null &&
              this.groupName.equals(other.getGroupName()))) &&
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
        if (getExecutable() != null) {
            _hashCode += getExecutable().hashCode();
        }
        if (getArgument() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getArgument());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getArgument(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getInput() != null) {
            _hashCode += getInput().hashCode();
        }
        if (getOutput() != null) {
            _hashCode += getOutput().hashCode();
        }
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        if (getWorkingDirectory() != null) {
            _hashCode += getWorkingDirectory().hashCode();
        }
        if (getEnvironment() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEnvironment());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEnvironment(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWallTimeLimit() != null) {
            _hashCode += getWallTimeLimit().hashCode();
        }
        if (getFileSizeLimit() != null) {
            _hashCode += getFileSizeLimit().hashCode();
        }
        if (getCoreDumpLimit() != null) {
            _hashCode += getCoreDumpLimit().hashCode();
        }
        if (getDataSegmentLimit() != null) {
            _hashCode += getDataSegmentLimit().hashCode();
        }
        if (getLockedMemoryLimit() != null) {
            _hashCode += getLockedMemoryLimit().hashCode();
        }
        if (getMemoryLimit() != null) {
            _hashCode += getMemoryLimit().hashCode();
        }
        if (getOpenDescriptorsLimit() != null) {
            _hashCode += getOpenDescriptorsLimit().hashCode();
        }
        if (getPipeSizeLimit() != null) {
            _hashCode += getPipeSizeLimit().hashCode();
        }
        if (getStackSizeLimit() != null) {
            _hashCode += getStackSizeLimit().hashCode();
        }
        if (getCPUTimeLimit() != null) {
            _hashCode += getCPUTimeLimit().hashCode();
        }
        if (getProcessCountLimit() != null) {
            _hashCode += getProcessCountLimit().hashCode();
        }
        if (getVirtualMemoryLimit() != null) {
            _hashCode += getVirtualMemoryLimit().hashCode();
        }
        if (getThreadCountLimit() != null) {
            _hashCode += getThreadCountLimit().hashCode();
        }
        if (getUserName() != null) {
            _hashCode += getUserName().hashCode();
        }
        if (getGroupName() != null) {
            _hashCode += getGroupName().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(POSIXApplication_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "POSIXApplication_Type"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "NCName"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("executable");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Executable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "FileName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("argument");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Argument"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Argument_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("input");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Input"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "FileName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("output");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Output"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "FileName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Error"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "FileName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workingDirectory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "WorkingDirectory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "DirectoryName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("environment");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Environment"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Environment_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("wallTimeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "WallTimeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileSizeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "FileSizeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("coreDumpLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "CoreDumpLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataSegmentLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "DataSegmentLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lockedMemoryLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "LockedMemoryLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memoryLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "MemoryLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("openDescriptorsLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "OpenDescriptorsLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pipeSizeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "PipeSizeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stackSizeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "StackSizeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CPUTimeLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "CPUTimeLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processCountLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "ProcessCountLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("virtualMemoryLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "VirtualMemoryLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("threadCountLimit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "ThreadCountLimit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Limits_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "UserName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "UserName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("groupName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "GroupName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "GroupName_Type"));
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
