/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package eu.unicore.util.configuration;

import java.util.Arrays;



/**
 * Provides an optional metadata for properties retrieved using {@link PropertiesHelper}.
 * Uses the fluent style and shortened syntax
 * @author K. Benedyczak
 */
public class PropertyMD implements Cloneable
{
	public enum Type {INT, LONG, FLOAT, BOOLEAN, STRING, PATH, ENUM, LIST, CLASS, STRUCTURED_LIST}
	
	private boolean secret;
	private boolean hide;
	private String defaultValue;
	private boolean hasDefault;
	private boolean mandatory;
	private boolean deprecated;
	private boolean canHaveSubkeys = false;
	private boolean numericalListKeys = false;
	private boolean updateable = false;
	private String structuredListId;
	private String description;
	private DocumentationCategory category;
	private String sortKey = null;
	private long min = Integer.MIN_VALUE;
	private long max = Integer.MAX_VALUE;
	private double minFloat = Double.MIN_VALUE;
	private double maxFloat = Double.MAX_VALUE;
	private Type type = Type.STRING; 
	private Enum<?> enumTypeInstance;
	private Class<?> baseClass;

	/**
	 * Creates a non-secret property, with a default value 
	 * (it can't become mandatory property as we have a default).
	 * The property type will be guessed in that order: int, long, float, boolean. If the default 
	 * value can not be mapped to any of those types, the type will be String. The type can be later
	 * freely changed, but default value must be of the type set. 
	 */
	public PropertyMD(String defaultValue) {
		this.defaultValue = defaultValue;
		this.hasDefault = true;
		if (isInt(defaultValue))
			this.type = Type.INT;
		else if (isLong(defaultValue))
			this.type = Type.LONG;
		else if (isFloat(defaultValue))
			this.type = Type.FLOAT;
		else if (isBoolean(defaultValue))
			this.type = Type.BOOLEAN;
		else
			this.type = Type.STRING;
	}
	
	/**
	 * Creates a property of class type with a desired class default value.
	 * All values must be loadable classes which extend the baseClass 
	 * @param defaultValue default value
	 * @param baseClass base class for this property
	 */
	public PropertyMD(Class<?> defaultValue, Class<?> baseClass) {
		this.defaultValue = defaultValue == null ? null : defaultValue.getName();
		this.baseClass = baseClass;
		if (defaultValue != null && !baseClass.isAssignableFrom(defaultValue))
			throw new IllegalArgumentException(defaultValue + " must extend " + baseClass);
		this.hasDefault = true;
		this.type = Type.CLASS;
	}
	
	/**
	 * Creates a property of enum type with a desired enum default value.
	 */
	public <T extends Enum<T>> PropertyMD(T defaultValue) {
		enumTypeInstance = defaultValue;
		this.hasDefault = true;
		this.defaultValue = defaultValue.name();
		this.type = Type.ENUM;
	}

	/**
	 * public, non mandatory property without a default value of String type.
	 */
	public PropertyMD() {
	}

	public boolean isSecret() {
		return secret;
	}
	public PropertyMD setSecret() {
		this.secret = true;
		return this;
	}
	/**
	 * @return if property should be ignored in documentation. Useful for subkeys.
	 */
	public boolean isHidden() {
		return hide;
	}
	public PropertyMD setHidden() {
		this.hide = true;
		return this;
	}
	
	public String getDefault() {
		return defaultValue;
	}
	public PropertyMD setDefault(String defaultValue) {
		if (isMandatory())
			throw new IllegalStateException("A property can not have a default " +
					"value and be mandatory at the same time");
		checkDefault(type, defaultValue);
		this.defaultValue = defaultValue;
		this.hasDefault = true;
		return this;
	}
	
	protected void checkDefault(Type type, String defaultValue) {
		if (defaultValue == null)
			return;
		if (type == Type.BOOLEAN && !isBoolean(defaultValue))
			throw new IllegalStateException("A property default type must be valid value of its type: boolean");
		if (type == Type.INT && !isInt(defaultValue))
			throw new IllegalStateException("A property default type must be valid value of its type: int");
		if (type == Type.LONG && !isLong(defaultValue))
			throw new IllegalStateException("A property default type must be valid value of its type: long");
		if (type == Type.FLOAT && !isFloat(defaultValue))
			throw new IllegalStateException("A property default type must be valid value of its type: float");
		if (type == Type.CLASS && !isClass(defaultValue))
			throw new IllegalStateException("A property default type must be valid value of its type: class");
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	public PropertyMD setMandatory() {
		if (hasDefault())
			throw new IllegalStateException("A property can not have a default " +
					"value and be mandatory at the same time");
		this.mandatory = true;
		return this;
	}
	public PropertyMD setDescription(String description) {
		this.description = description;
		return this;
	}
	public boolean hasDefault() {
		return hasDefault;
	}
	
	public boolean isUpdateable() {
		return updateable;
	}
	public PropertyMD setUpdateable() {
		this.updateable = true;
		return this;
	}
	
	public PropertyMD setBounds(long min, long max) {
		if (type != Type.INT && type != Type.LONG)
			throw new IllegalStateException("Integer bounds can be only set for int or long property");
		this.min = min;
		this.max = max;
		return this;
	}
	public PropertyMD setBounds(double min, double max) {
		if (type != Type.FLOAT)
			throw new IllegalStateException("Floating point bounds can be only set for Floating point property");
		this.minFloat = min;
		this.maxFloat = max;
		return this;
	}
	public PropertyMD setPositive() {
		if (type != Type.FLOAT && type != Type.INT && type != Type.LONG)
			throw new IllegalStateException("Floating point bounds can be only set for number properties");
		this.min = 1;
		this.minFloat = 0.001;
		return this;
	}
	public PropertyMD setNonNegative() {
		if (type != Type.FLOAT && type != Type.INT && type != Type.LONG)
			throw new IllegalStateException("Floating point bounds can be only set for Floating point property");
		this.min = 0;
		this.minFloat = 0.0;
		return this;
	}
	public PropertyMD setMin(long min) {
		if (type != Type.INT && type != Type.LONG)
			throw new IllegalStateException("Integer bounds can be only set for int or long property");
		this.min = min;
		return this;
	}
	public PropertyMD setMax(long max) {
		if (type != Type.INT && type != Type.LONG)
			throw new IllegalStateException("Integer bounds can be only set for int or long property");
		this.max = max;
		return this;
	}
	public PropertyMD setMin(double min) {
		if (type != Type.FLOAT)
			throw new IllegalStateException("Floating point bounds can be only set for Floating point property");
		this.minFloat = min;
		return this;
	}
	public PropertyMD setMax(double max) {
		if (type != Type.FLOAT)
			throw new IllegalStateException("Floating point bounds can be only set for Floating point property");
		this.maxFloat = max;
		return this;
	}
	public PropertyMD setLong() {
		checkDefault(Type.LONG, defaultValue);
		this.type = Type.LONG;
		this.max = Long.MAX_VALUE;
		this.min = Long.MIN_VALUE;
		return this;
	}
	/**
	 * Sets enum type. Note that the value passed as argument doesn't matter, it
	 * must be only of the proper enum, which shall make an enum type of the property.
	 */
	public <T extends Enum<T>> PropertyMD setEnum(T defaultValue) {
		enumTypeInstance = defaultValue;
		this.type = Type.ENUM;
		return this;
	}
	public PropertyMD setClass(Class<?> baseClass) {
		this.type = Type.CLASS;
		this.baseClass = baseClass;
		checkDefault(Type.CLASS, defaultValue);
		return this;
	}
	public PropertyMD setInt() {
		checkDefault(Type.INT, defaultValue);
		this.type = Type.INT;
		this.max = Integer.MAX_VALUE;
		this.min = Integer.MIN_VALUE;
		return this;
	}
	public PropertyMD setFloat() {
		checkDefault(Type.FLOAT, defaultValue);
		this.type = Type.FLOAT;
		this.minFloat = Double.MIN_VALUE;
		this.maxFloat = Double.MAX_VALUE;
		return this;
	}
	public PropertyMD setBoolean() {
		checkDefault(Type.BOOLEAN, defaultValue);
		this.type = Type.BOOLEAN;
		return this;
	}
	public PropertyMD setPath() {
		this.type = Type.PATH;
		return this;
	}
	public PropertyMD setList(boolean numericalKeys) {
		this.type = Type.LIST;
		this.numericalListKeys = numericalKeys;
		return this;
	}
	public boolean numericalListKeys() {
		return numericalListKeys;
	}

	public PropertyMD setStructuredList(boolean numericalKeys) {
		this.type = Type.STRUCTURED_LIST;
		this.numericalListKeys = numericalKeys;
		return this;
	}

	public PropertyMD setStructuredListEntry(String listId) {
		this.structuredListId = listId;
		return this;
	}
	
	public String getStructuredListEntryId() {
		return structuredListId;
	}
	public boolean isStructuredListEntry() {
		return structuredListId != null;
	}
	
	
	public boolean canHaveSubkeys() {
		return canHaveSubkeys;
	}
	public PropertyMD setCanHaveSubkeys()
	{
		this.canHaveSubkeys = true;
		return this;
	}

	public String getDescription() {
		return description;
	}
	public long getMin() {
		return min;
	}
	public long getMax() {
		return max;
	}
	public double getMinFloat() {
		return minFloat;
	}
	public double getMaxFloat() {
		return maxFloat;
	}
	public Type getType() {
		return type;
	}
	public Enum<?> getEnumTypeInstance() {
		return enumTypeInstance;
	}
	public Class<?> getBaseClass() {
		return baseClass;
	}
	public boolean isDeprecated()
	{
		return deprecated;
	}
	public PropertyMD setDeprecated()
	{
		this.deprecated = true;
		setHidden();
		return this;
	}

	protected boolean isBoolean(String val) {
		if (val == null)
			return false;
	       	if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes"))
	       		return true;
	       	if (val.equalsIgnoreCase("false") || val.equalsIgnoreCase("no"))
	       		return true;
	       	return false;
	}

	protected boolean isLong(String val) {
		try
		{
			Long.parseLong(val);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}

	protected boolean isInt(String val) {
		try
		{
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}

	protected boolean isFloat(String val) {
		try
		{
			if (val == null)
				return false;
			Double.parseDouble(val);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}

	protected boolean isClass(String val) {
		try
		{
			if (val == null)
				return false;
			Class<?> cls = Class.forName(val);
			if (baseClass != null && !baseClass.isAssignableFrom(cls))
				return false;
			return true;
		} catch (ClassNotFoundException e)
		{
			return false;
		}
	}
	
	/**
	 * Returns human friendly description of the property type
	 */
	public String getTypeDescription() {
		switch(type)
		{
		case STRING:
			return "string";
		case BOOLEAN:
			return "[true, false]";
		case ENUM:
			Object[] allValues = (enumTypeInstance.getDeclaringClass()).getEnumConstants();
			return Arrays.toString(allValues);
		case INT:
		case LONG:
			boolean hasMin = false;
			if (min != Integer.MIN_VALUE && min != Long.MIN_VALUE)
				hasMin = true;
			boolean hasMax = false;
			if (max != Integer.MAX_VALUE && max != Long.MAX_VALUE)
				hasMax = true;
			if (!hasMin && !hasMax)
				return "integer number";
			if (hasMin && hasMax)
				return "integer [" + min + " -- " + max + "]";
			if (hasMin)
				return "integer >= " + min;
			if (hasMax)
				return "integer <= " + max;
		case PATH:
			return "filesystem path";
		case LIST:
			return "list of properties with a common prefix";
		case FLOAT:
			boolean hasMinF = false;
			if (minFloat != Double.MIN_VALUE && minFloat != Double.MIN_VALUE)
				hasMinF = true;
			boolean hasMaxF = false;
			if (maxFloat != Double.MAX_VALUE && maxFloat != Double.MAX_VALUE)
				hasMaxF = true;
			if (!hasMinF && !hasMaxF)
				return "floating point number";
			if (hasMinF && hasMaxF)
				return "floating [" + minFloat + " -- " + maxFloat + "]";
			if (hasMinF)
				return "floating > " + minFloat;
			if (hasMaxF)
				return "floating < " + maxFloat;
		case CLASS:
			return "Class extending " + baseClass.getName();
		case STRUCTURED_LIST:
			return "Structured list";
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * @return the category
	 */
	public DocumentationCategory getCategory() {
		return category;
	}

	/**
	 * @param category property category
	 */
	public PropertyMD setCategory(DocumentationCategory category) {
		this.category = category;
		return this;
	}
	
	/**
	 * @return the sortKey
	 */
	public String getSortKey()
	{
		return sortKey;
	}

	/**
	 * @param sortKey the sortKey to set
	 */
	public PropertyMD setSortKey(String sortKey)
	{
		this.sortKey = sortKey;
		return this;
	}

	/**
	 * Defines documentation category, options are grouped in categories.
	 */
	public static class DocumentationCategory implements Comparable<DocumentationCategory> {
		private String name;
		private String sortKey = null;
		
		public DocumentationCategory(String name) {
			this.name = name;
			if (name == null)
				name = "";
		}

		public DocumentationCategory(String name, String sortKey)	{
			this(name);
			this.sortKey = sortKey;
		}

		public String getName()	{
			return name;
		}

		public String getSortKey() {
			return sortKey;
		}

		@Override
		public int compareTo(DocumentationCategory o)
		{
			String myKey = sortKey == null ? name : sortKey;
			String otherKey = o.getSortKey() == null ? o.getName() : o.getSortKey();
			return myKey.compareTo(otherKey);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((sortKey == null) ? 0 : sortKey.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DocumentationCategory other = (DocumentationCategory) obj;
			if (name == null)
			{
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (sortKey == null)
			{
				if (other.sortKey != null)
					return false;
			} else if (!sortKey.equals(other.sortKey))
				return false;
			return true;
		}
	}
	
	public PropertyMD clone() {
		try
		{
			return (PropertyMD) super.clone();
		} catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("BUG: " + e);
		}
	}
}
