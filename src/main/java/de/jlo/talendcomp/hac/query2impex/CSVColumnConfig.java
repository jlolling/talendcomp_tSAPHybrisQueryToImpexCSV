package de.jlo.talendcomp.hac.query2impex;

public class CSVColumnConfig {

	private String columnName = null;
	private int position = 0;
	private String type = null;
	private BasicDataType basicType = null; 
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getDBType() {
		return type;
	}
	public void setDBType(String type) {
		this.type = type;
		basicType = BasicDataType.getBasicTypeObjectByName(type);
	}
	
	public boolean isString() {
		return basicType == BasicDataType.CHARACTER;
	}

	public boolean isDouble() {
		return basicType == BasicDataType.DOUBLE;
	}

	public boolean isInteger() {
		return basicType == BasicDataType.INTEGER;
	}

	public boolean isLong() {
		return basicType == BasicDataType.LONG;
	}

	public boolean isBoolean() {
		return basicType == BasicDataType.BOOLEAN;
	}

	public boolean isDate() {
		return basicType == BasicDataType.DATE;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CSVColumnConfig) {
			return ((CSVColumnConfig) o).columnName.equalsIgnoreCase(columnName);
		} else if (o instanceof String) {
			return ((String) o).equalsIgnoreCase(columnName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return columnName.hashCode();
	}
	
	@Override
	public String toString() {
		return "column=" + columnName + " position=" +position + " db-type=" + type + " basic-type=" + basicType;
	}

}
