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
 * Generates properties info in CSV format.
 * Intended for use with Sphinx.
 * 
 * @author schuller
 */
public class CSVFormatter implements HelpFormatter
{
	@Override
	public String format(String pfx, Map<String, PropertyMD> metadata)
	{
		if (metadata.size() == 0)
			return "";
		
		Set<String> keys = metadata.keySet();
		Map<DocumentationCategory, Set<String>> keysInCats = new HashMap<>();
		for (String key: keys)
		{
			PropertyMD meta = metadata.get(key);
			DocumentationCategory cat = meta.getCategory();
			Set<String> current = keysInCats.get(cat);
			if (current == null)
			{
				current = new TreeSet<>();
				keysInCats.put(cat, current);
			}
			current.add(meta.getSortKey() != null ? meta.getSortKey() + "-_-_-"+key : key);
		}
		StringBuilder ret = new StringBuilder();
		// header line
		ret.append("Property name,Type, Default value \\/ mandatory,Description\n");
		Set<String> noCat = keysInCats.remove(null);
		if (noCat != null) {
			ret.append(formatCategory(noCat, pfx, metadata));
		}
		Set<DocumentationCategory> catsSet = new TreeSet<>(keysInCats.keySet());
		for (DocumentationCategory cat: catsSet)
		{
			//ret.append("*").append(cat.getName()).append("*,,,\n");
			ret.append(formatCategory(keysInCats.get(cat), pfx, metadata));
		}
		return ret.toString();
	}
	
	private String[] mustEscape = new String[] { "http:", "https:", "jdbc:", "file:", "*"};
	
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

			// property name
			ret.append("\"");
			if (!md.isStructuredListEntry())
				ret.append(pfx).append(key);
			else
			{
				PropertyMD listMeta = metadata.get(md.getStructuredListEntryId());
				String listKey = listMeta.numericalListKeys() ? "<NUMBER>." : "\\*.";
				ret.append(pfx + md.getStructuredListEntryId() + listKey + key);
			}

			if (md.getType() == Type.LIST || md.getType() == Type.STRUCTURED_LIST)
				ret.append(md.numericalListKeys() ? "<NUMBER> " : "\\* ");
			if (md.canHaveSubkeys())
				ret.append(".\\* ");

			ret.append("\",");
			
			// type
			ret.append("\"");
			ret.append(md.getTypeDescription());
			if (md.canHaveSubkeys())
				ret.append(" *can have subkeys* ");
			ret.append("\",");
			
			// default
			ret.append("\"");
			if (md.isMandatory())
				ret.append(" *mandatory* ");
			else if (md.hasDefault())
			{
				if (md.getDefault()!=null && md.getDefault().equals(""))
					ret.append("*empty string*");
				else {
					String value = md.getDefault();
					if(value!=null) {
						for (String x: mustEscape) {
							if(value.startsWith(x) || value.equals(x)) {
								ret.append("\\");
							}
						}
					}
					ret.append(value);	
				}
			}
			ret.append("\",");
			
			// description
			ret.append("\"");
			String desc = md.getDescription();
			if (desc == null)
				desc = " ";
			ret.append(desc);
			if (md.isUpdateable())
				ret.append("(runtime updateable)");
			ret.append("\"\n");
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
		ClassLoader loader = CSVFormatter.class.getClassLoader();
		Class<?> clazz = loader.loadClass(clazzName);
		
		
		Field fMeta = getField(clazz, "META", DocumentationReferenceMeta.class, Map.class);
		if (prefix == null)
		{
			Field fPrefix = getField(clazz, "DEFAULT_PREFIX", DocumentationReferencePrefix.class, String.class);
			prefix = (String) fPrefix.get(null);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, PropertyMD> meta = (Map<String, PropertyMD>) fMeta.get(null);

		CSVFormatter formatter = new CSVFormatter();
		String result = formatter.format(prefix, meta);
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(folder, destination)));
		w.write(result);
		w.close();
	}
}
