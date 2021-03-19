package de.jlo.talendcomp.hac.query2impex;

public class SeparatedColumnConfig {
	
	private String columnName = null;
	private boolean excludeFromCSV = true;
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		if (TypeUtil.isEmpty(columnName)) {
			throw new IllegalArgumentException("columnName cannot be null or empty");
		}
		this.columnName = columnName;
	}
	public boolean isExcludeFromCSV() {
		return excludeFromCSV;
	}
	public void setExcludeFromCSV(Boolean excludeFromCSV) {
		if (excludeFromCSV != null) {
			this.excludeFromCSV = excludeFromCSV;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SeparatedColumnConfig) {
			return ((SeparatedColumnConfig) o).columnName.equalsIgnoreCase(columnName);
		} else if (o instanceof String) {
			return ((String) o).equalsIgnoreCase(columnName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return columnName.hashCode();
	}

}
