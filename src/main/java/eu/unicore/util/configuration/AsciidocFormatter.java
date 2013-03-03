/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package eu.unicore.util.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import eu.unicore.util.configuration.PropertyMD.DocumentationCategory;
import eu.unicore.util.configuration.PropertyMD.Type;

/**
 * Generates Asciidoc table with properties info
 * @author K. Benedyczak
 */
public class AsciidocFormatter implements HelpFormatter
{
	@Override
	public String format(String pfx, Map<String, PropertyMD> metadata)
	{
		if (metadata.size() == 0)
			return "";
		
		Set<String> keys = metadata.keySet();
		Map<DocumentationCategory, Set<String>> keysInCats = new HashMap<DocumentationCategory, Set<String>>();
		for (String key: keys)
		{
			PropertyMD meta = metadata.get(key);
			DocumentationCategory cat = meta.getCategory();
			Set<String> current = keysInCats.get(cat);
			if (current == null)
			{
				current = new TreeSet<String>();
				keysInCats.put(cat, current);
			}
			
			current.add(meta.getSortKey() != null ? meta.getSortKey() + "-_-_-"+key : key);
		}
		
		
		StringBuilder ret = new StringBuilder();
		ret.append("[width=\"100%\",cols=\"<30%m,20%<,15%<,<35%\",frame=\"topbot\",options=\"header\"]\n");
		ret.append("|=====================================================================\n");
		ret.append("|Property name |Type |Default value / mandatory |Description \n");

		Set<String> noCat = keysInCats.remove(null);
		if (noCat != null)
			ret.append(formatCategory(noCat, pfx, metadata));
		
		Set<DocumentationCategory> catsSet = new TreeSet<DocumentationCategory>(keysInCats.keySet());
		for (DocumentationCategory cat: catsSet)
		{
			ret.append("4+^e| --- " + cat.getName() + " ---");
			ret.append(formatCategory(keysInCats.get(cat), pfx, metadata));
		}
		ret.append("|=====================================================================\n");
		return ret.toString();
	}
	
	private String formatCategory(Set<String> keys, String pfx, Map<String, PropertyMD> metadata)
	{
		StringBuilder ret = new StringBuilder();
		for (String key: keys)
		{
			if (key.contains("-_-_-"))
				key = key.split("-_-_-")[1];
			
			PropertyMD md = metadata.get(key);
			if (md.isHidden())
				continue;

			if (!md.isStructuredListEntry())
				ret.append("|" + pfx + key);
			else
			{
				PropertyMD listMeta = metadata.get(md.getStructuredListEntryId());
				String listKey = listMeta.numericalListKeys() ? "<NUMBER>." : "*.";
				ret.append("|" + pfx + md.getStructuredListEntryId() + listKey + key);
			}
			if (md.getType() == Type.LIST || md.getType() == Type.STRUCTURED_LIST)
				ret.append(md.numericalListKeys() ? "<NUMBER>" : "*");
			if (md.canHaveSubkeys())
				ret.append("[.*]");
			ret.append("|");
			
			ret.append(md.getTypeDescription());
			if (md.canHaveSubkeys())
				ret.append(" _can have subkeys_");
			ret.append("|");
			
			if (md.isMandatory())
				ret.append("_mandatory to be set_|");
			else if (md.hasDefault())
			{
				if (md.getDefault() == null)
					ret.append("_not set_|");
				else if (md.getDefault().equals(""))
					ret.append("_empty string_|");
				else
					ret.append("`" + md.getDefault() +"`|");
			} else
				ret.append("-|");
			String desc = md.getDescription();
			if (desc == null)
				desc = " ";
			ret.append(desc);
			if (md.isUpdateable())
				ret.append(" _(runtime updateable)_");
			ret.append(" \n");
		}
		return ret.toString();
	}
	
	public static void main(String... args) throws Exception
	{
		if (args.length < 2)
			throw new IllegalArgumentException("Args: <target directory> <triple as one string: class|target file|prefix where prefix is optional>");
		for (int i=1; i<args.length; i++)
		{
			String[] genArgs = args[i].split("\\|");
			if (genArgs.length < 2 || genArgs.length > 3)
				throw new IllegalArgumentException("Args: <target directory> <triple as one string: class|target file|prefix where prefix is optional>");
			String prefix = genArgs.length == 3 ? genArgs[2] : null;
			processFile(args[0], genArgs[0], genArgs[1], prefix);
		}
	}
	
	private static Field getField(Class<?> clazz, String defaultName, 
			Class<? extends Annotation> annotation, Class<?> desiredType) throws Exception
	{
		Field field = null;
		Field[] fields = clazz.getDeclaredFields();
		for (Field f: fields)
		{
			if (f.getAnnotation(annotation) != null)
			{
				field = f;
				break;
			}
		}
		if (field == null)
			field = clazz.getDeclaredField(defaultName);
		if (!Modifier.isStatic(field.getModifiers()))
			throw new IllegalArgumentException("The field " + field.getName() + " of the class " + 
					clazz.getName() + " is not static");
		if (!desiredType.isAssignableFrom(field.getType()))
			throw new IllegalArgumentException("The field " + field.getName() + " of the class " +
					clazz.getName() + " is not of " + desiredType.getName() + " type");
		field.setAccessible(true);
		return field;
	}
	
	public static void processFile(String folder, String clazzName, String destination, String prefix) throws Exception
	{
		System.out.println("Generating from: " + clazzName + " to " + destination + " prefix: " + prefix);
		ClassLoader loader = AsciidocFormatter.class.getClassLoader();
		Class<?> clazz = loader.loadClass(clazzName);
		
		
		Field fMeta = getField(clazz, "META", DocumentationReferenceMeta.class, Map.class);
		if (prefix == null)
		{
			Field fPrefix = getField(clazz, "DEFAULT_PREFIX", DocumentationReferencePrefix.class, String.class);
			prefix = (String) fPrefix.get(null);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, PropertyMD> meta = (Map<String, PropertyMD>) fMeta.get(null);

		AsciidocFormatter formatter = new AsciidocFormatter();
		String result = formatter.format(prefix, meta);
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(folder, destination)));
		w.write(result);
		w.close();
	}
}
