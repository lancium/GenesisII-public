/**
 * SPMDApplication_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ogf.jsdl.spmd;

public class SPMDApplication_Type  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.jsdl.posix.FileName_Type executable;

    private org.ggf.jsdl.posix.Argument_Type[] argument;

    private org.ggf.jsdl.posix.FileName_Type input;

    private org.ggf.jsdl.posix.FileName_Type output;

    private org.ggf.jsdl.posix.FileName_Type error;

    private org.ggf.jsdl.posix.DirectoryName_Type workingDirectory;

    private org.ggf.jsdl.posix.Environment_Type[] environment;

    private org.ggf.jsdl.posix.UserName_Type userName;

    private org.ogf.jsdl.spmd.NumberOfProcesses_Type numberOfProcesses;

    private org.ogf.jsdl.spmd.ProcessesPerHost_Type processesPerHost;

    private org.ogf.jsdl.spmd.ThreadsPerProcess_Type threadsPerProcess;

    private org.apache.axis.types.URI SPMDVariation;

    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.NCName name;  // attribute

    public SPMDApplication_Type() {
    }

    public SPMDApplication_Type(
           org.ggf.jsdl.posix.FileName_Type executable,
           org.ggf.jsdl.posix.Argument_Type[] argument,
           org.ggf.jsdl.posix.FileName_Type input,
           org.ggf.jsdl.posix.FileName_Type output,
           org.ggf.jsdl.posix.FileName_Type error,
           org.ggf.jsdl.posix.DirectoryName_Type workingDirectory,
           org.ggf.jsdl.posix.Environment_Type[] environment,
           org.ggf.jsdl.posix.UserName_Type userName,
           org.ogf.jsdl.spmd.NumberOfProcesses_Type numberOfProcesses,
           org.ogf.jsdl.spmd.ProcessesPerHost_Type processesPerHost,
           org.ogf.jsdl.spmd.ThreadsPerProcess_Type threadsPerProcess,
           org.apache.axis.types.URI SPMDVariation,
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.NCName name) {
           this.executable = executable;
           this.argument = argument;
           this.input = input;
           this.output = output;
           this.error = error;
           this.workingDirectory = workingDirectory;
           this.environment = environment;
           this.userName = userName;
           this.numberOfProcesses = numberOfProcesses;
           this.processesPerHost = processesPerHost;
           this.threadsPerProcess = threadsPerProcess;
           this.SPMDVariation = SPMDVariation;
           this._any = _any;
           this.name = name;
    }


    /**
     * Gets the executable value for this SPMDApplication_Type.
     * 
     * @return executable
     */
    public org.ggf.jsdl.posix.FileName_Type getExecutable() {
        return executable;
    }


    /**
     * Sets the executable value for this SPMDApplication_Type.
     * 
     * @param executable
     */
    public void setExecutable(org.ggf.jsdl.posix.FileName_Type executable) {
        this.executable = executable;
    }


    /**
     * Gets the argument value for this SPMDApplication_Type.
     * 
     * @return argument
     */
    public org.ggf.jsdl.posix.Argument_Type[] getArgument() {
        return argument;
    }


    /**
     * Sets the argument value for this SPMDApplication_Type.
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
     * Gets the input value for this SPMDApplication_Type.
     * 
     * @return input
     */
    public org.ggf.jsdl.posix.FileName_Type getInput() {
        return input;
    }


    /**
     * Sets the input value for this SPMDApplication_Type.
     * 
     * @param input
     */
    public void setInput(org.ggf.jsdl.posix.FileName_Type input) {
        this.input = input;
    }


    /**
     * Gets the output value for this SPMDApplication_Type.
     * 
     * @return output
     */
    public org.ggf.jsdl.posix.FileName_Type getOutput() {
        return output;
    }


    /**
     * Sets the output value for this SPMDApplication_Type.
     * 
     * @param output
     */
    public void setOutput(org.ggf.jsdl.posix.FileName_Type output) {
        this.output = output;
    }


    /**
     * Gets the error value for this SPMDApplication_Type.
     * 
     * @return error
     */
    public org.ggf.jsdl.posix.FileName_Type getError() {
        return error;
    }


    /**
     * Sets the error value for this SPMDApplication_Type.
     * 
     * @param error
     */
    public void setError(org.ggf.jsdl.posix.FileName_Type error) {
        this.error = error;
    }


    /**
     * Gets the workingDirectory value for this SPMDApplication_Type.
     * 
     * @return workingDirectory
     */
    public org.ggf.jsdl.posix.DirectoryName_Type getWorkingDirectory() {
        return workingDirectory;
    }


    /**
     * Sets the workingDirectory value for this SPMDApplication_Type.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(org.ggf.jsdl.posix.DirectoryName_Type workingDirectory) {
        this.workingDirectory = workingDirectory;
    }


    /**
     * Gets the environment value for this SPMDApplication_Type.
     * 
     * @return environment
     */
    public org.ggf.jsdl.posix.Environment_Type[] getEnvironment() {
        return environment;
    }


    /**
     * Sets the environment value for this SPMDApplication_Type.
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
     * Gets the userName value for this SPMDApplication_Type.
     * 
     * @return userName
     */
    public org.ggf.jsdl.posix.UserName_Type getUserName() {
        return userName;
    }


    /**
     * Sets the userName value for this SPMDApplication_Type.
     * 
     * @param userName
     */
    public void setUserName(org.ggf.jsdl.posix.UserName_Type userName) {
        this.userName = userName;
    }


    /**
     * Gets the numberOfProcesses value for this SPMDApplication_Type.
     * 
     * @return numberOfProcesses
     */
    public org.ogf.jsdl.spmd.NumberOfProcesses_Type getNumberOfProcesses() {
        return numberOfProcesses;
    }


    /**
     * Sets the numberOfProcesses value for this SPMDApplication_Type.
     * 
     * @param numberOfProcesses
     */
    public void setNumberOfProcesses(org.ogf.jsdl.spmd.NumberOfProcesses_Type numberOfProcesses) {
        this.numberOfProcesses = numberOfProcesses;
    }


    /**
     * Gets the processesPerHost value for this SPMDApplication_Type.
     * 
     * @return processesPerHost
     */
    public org.ogf.jsdl.spmd.ProcessesPerHost_Type getProcessesPerHost() {
        return processesPerHost;
    }


    /**
     * Sets the processesPerHost value for this SPMDApplication_Type.
     * 
     * @param processesPerHost
     */
    public void setProcessesPerHost(org.ogf.jsdl.spmd.ProcessesPerHost_Type processesPerHost) {
        this.processesPerHost = processesPerHost;
    }


    /**
     * Gets the threadsPerProcess value for this SPMDApplication_Type.
     * 
     * @return threadsPerProcess
     */
    public org.ogf.jsdl.spmd.ThreadsPerProcess_Type getThreadsPerProcess() {
        return threadsPerProcess;
    }


    /**
     * Sets the threadsPerProcess value for this SPMDApplication_Type.
     * 
     * @param threadsPerProcess
     */
    public void setThreadsPerProcess(org.ogf.jsdl.spmd.ThreadsPerProcess_Type threadsPerProcess) {
        this.threadsPerProcess = threadsPerProcess;
    }


    /**
     * Gets the SPMDVariation value for this SPMDApplication_Type.
     * 
     * @return SPMDVariation
     */
    public org.apache.axis.types.URI getSPMDVariation() {
        return SPMDVariation;
    }


    /**
     * Sets the SPMDVariation value for this SPMDApplication_Type.
     * 
     * @param SPMDVariation
     */
    public void setSPMDVariation(org.apache.axis.types.URI SPMDVariation) {
        this.SPMDVariation = SPMDVariation;
    }


    /**
     * Gets the _any value for this SPMDApplication_Type.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this SPMDApplication_Type.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the name value for this SPMDApplication_Type.
     * 
     * @return name
     */
    public org.apache.axis.types.NCName getName() {
        return name;
    }


    /**
     * Sets the name value for this SPMDApplication_Type.
     * 
     * @param name
     */
    public void setName(org.apache.axis.types.NCName name) {
        this.name = name;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SPMDApplication_Type)) return false;
        SPMDApplication_Type other = (SPMDApplication_Type) obj;
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
            ((this.userName==null && other.getUserName()==null) || 
             (this.userName!=null &&
              this.userName.equals(other.getUserName()))) &&
            ((this.numberOfProcesses==null && other.getNumberOfProcesses()==null) || 
             (this.numberOfProcesses!=null &&
              this.numberOfProcesses.equals(other.getNumberOfProcesses()))) &&
            ((this.processesPerHost==null && other.getProcessesPerHost()==null) || 
             (this.processesPerHost!=null &&
              this.processesPerHost.equals(other.getProcessesPerHost()))) &&
            ((this.threadsPerProcess==null && other.getThreadsPerProcess()==null) || 
             (this.threadsPerProcess!=null &&
              this.threadsPerProcess.equals(other.getThreadsPerProcess()))) &&
            ((this.SPMDVariation==null && other.getSPMDVariation()==null) || 
             (this.SPMDVariation!=null &&
              this.SPMDVariation.equals(other.getSPMDVariation()))) &&
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
        if (getUserName() != null) {
            _hashCode += getUserName().hashCode();
        }
        if (getNumberOfProcesses() != null) {
            _hashCode += getNumberOfProcesses().hashCode();
        }
        if (getProcessesPerHost() != null) {
            _hashCode += getProcessesPerHost().hashCode();
        }
        if (getThreadsPerProcess() != null) {
            _hashCode += getThreadsPerProcess().hashCode();
        }
        if (getSPMDVariation() != null) {
            _hashCode += getSPMDVariation().hashCode();
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
        new org.apache.axis.description.TypeDesc(SPMDApplication_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "SPMDApplication_Type"));
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
        elemField.setFieldName("userName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "UserName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "UserName_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numberOfProcesses");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "NumberOfProcesses"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "NumberOfProcesses_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processesPerHost");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "ProcessesPerHost"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "ProcessesPerHost_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("threadsPerProcess");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "ThreadsPerProcess"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "ThreadsPerProcess_Type"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SPMDVariation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "SPMDVariation"));
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
