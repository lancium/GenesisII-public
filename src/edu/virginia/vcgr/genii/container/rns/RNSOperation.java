package edu.virginia.vcgr.genii.container.rns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.ggf.rns.NameMappingType;

public class RNSOperation implements Serializable {

	static final long serialVersionUID = 0L;

	public enum OperationType {ENTRY_ADD, ENTRY_CREATE, ENTRY_REMOVE, ENTRY_RENAME}

	public static final String RENAME_NAME_MAPPING_SEPERATOR = ":";
	
	@XmlAttribute(name = "operationType", required = true)
	private OperationType operationType;

	@XmlAttribute(name = "affectedEntries", required = true)
	private String[] affectedEntries;
	
	public RNSOperation() {}

	public RNSOperation(OperationType operationType, String[] affectedEntries) {
		this.operationType = operationType;
		this.affectedEntries = affectedEntries;
	}
	
	public RNSOperation(OperationType operationType, String affectedEntry) {
		this.operationType = operationType;
		this.affectedEntries = new String[] {affectedEntry};
	}

	@XmlTransient
	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	@XmlTransient
	public String[] getAffectedEntries() {
		return affectedEntries;
	}

	public void setAffectedEntries(String[] affectedEntries) {
		this.affectedEntries = affectedEntries;
	}
	
	@XmlTransient
	public String getAffectedEntry() {
		return affectedEntries[0];
	}
	
	@XmlTransient
	public Collection<NameMappingType> getOldNameNewNameMappingsForRenameOperation() {
		
		if (!(operationType == OperationType.ENTRY_RENAME)) return null;
		
		List<NameMappingType> nameMappings = new ArrayList<NameMappingType>(affectedEntries.length);
		for (String mappingString : affectedEntries) {
			int indexOfSeperator = mappingString.indexOf(RENAME_NAME_MAPPING_SEPERATOR);
			if (indexOfSeperator == -1) {
				throw new RuntimeException("Affected entry name is not in porper format");
			}
			String oldName = mappingString.substring(0, indexOfSeperator);
			String newName = mappingString.substring(indexOfSeperator + 1);
			NameMappingType mapping = new NameMappingType(oldName, newName);
			
			nameMappings.add(mapping);
		}
		return nameMappings;
	}
}
