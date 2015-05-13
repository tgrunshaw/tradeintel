/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.trademe.utility.meta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ClassUtils;

/**
 * Possibly the most difficult code to comprehend.
 * 
 * See Wiki for documentation.
 *
 * @author Tim Grunshaw
 * @version 23.10.13
 */
public class ClassHierarchyPrinter {

    Set<Class> classes;

    public ClassHierarchyPrinter(Class c) {
        Set<Class> classesToIgnore = new HashSet<>();
        classesToIgnore.add(c);
        classes = traverseClass(c, classesToIgnore);
        classes.add(c); // Add itself.
    }

    /**
     * aClass - The class to use to find all other classes referenced.
     * classesToIgnore - Classes to not delve into as they have already been
     * traversed. (To eliminate cyclic issues). Add aClass to this list! (Incase
     * aClass contains a reference to itself.
     */
    private Set<Class> traverseClass(Class aClass, Set<Class> classesToIgnore) {
        // classSet contains the classes referenced by this class
        Set<Class> classSet = new HashSet<>();

        makeFieldsAccessable(aClass);
        List<Field> fields = getAllFields(aClass);

        for (Field f : fields) {
            // Basically, need to deal with: List<Something> field;
            // Doing it without these next few lines just returns List
            // Need a set because: Map<Something, Anotherthing> field2;
            Type type = f.getGenericType();
            Set<Class> classesOfField = new HashSet<>();
            if (type instanceof ParameterizedType) {
                classesOfField.addAll(getClassesOfParameterizedType(type));
            } else {
                classesOfField.add(f.getType());
            }

            for (Class fc : classesOfField) {
                // Skip all non trademe classes.
                if (!fc.getName().contains("nz.co.trademe")) {
                    // Remove from the list & skip.
                    classesOfField.remove(fc);
                    continue;
                }

                if (!classSet.contains(fc) && !classesToIgnore.contains(fc)) {
                    // Not already in this classes set & not in the classes to ignore
                    classSet.add(fc);
                    classesToIgnore.add(fc);
                    Set<Class> temp = traverseClass(fc, classesToIgnore);
                    classSet.addAll(temp);
                }


                // Get all of the superclasses of this field
                List<? extends Class> superClasses = ClassUtils.getAllSuperclasses(aClass);
                for (Class clazz : superClasses) {
                    // Skip all non trademe classes.
                    if (!clazz.getName().contains("nz.co.trademe")) {
                        // Skip
                        continue;
                    }

                    // Do not need to look at superClass members as they have already 
                    // been found in the getAllFields() method.
                    // Just add the superClass 
                    classSet.add(clazz);
                }
            }
        }
        return classSet;
    }

    /**
     * If it is actually something like a List
     *
     * @param type
     * @return
     */
    private Set<Class> getClassesOfParameterizedType(Type type) {
        assert type instanceof ParameterizedType;

        Set<Class> typeClasses = new HashSet<>();

        // See: http://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
        ParameterizedType pType = (ParameterizedType) type;
        for (Type t : pType.getActualTypeArguments()) {
            if (t instanceof ParameterizedType) {
                //assert false : "There is a List<List<Something>> type thing!"; // Easiest to see if this is ever an issue.
                // Recursion... if the ParameterizedType takes a ParameterisedType as its argument.
                // E.g. List<List<ListedItemDetail>> twoDimensionalList;
                typeClasses.addAll(getClassesOfParameterizedType(t));
            } else {
                // See: http://stackoverflow.com/questions/1942644/get-generic-type-of-java-util-list
                Class<?> theClassType = (Class<?>) t;
                typeClasses.add(theClassType);
            }
        }
        return typeClasses;
    }

    /**
     * Returns all fields (including inherited fields).
     */
    private List<Field> getAllFields(Class c) {
        List<? extends Class> superClasses = ClassUtils.getAllSuperclasses(c);
        List<Field> allFields = new ArrayList<>();
        for (Class clazz : superClasses) {
            makeFieldsAccessable(clazz);
            allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        // Current class fields too.
        allFields.addAll(Arrays.asList(c.getDeclaredFields()));
        return allFields;
    }

    private void makeFieldsAccessable(Class c) {
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
        }
    }

    public ArrayList<String> getClassNames() {
        // Needed as some filenames will be the same after string manipulation.
        // As they existed as an array for classType before.
        ArrayList<String> fileNames = new ArrayList<>();
        for (Class c : classes) {
            String fullName = c.getName();
            String name = fullName.substring(fullName.lastIndexOf(".") + 1);
            name = name.indexOf(";") != -1 ? name.substring(0, name.length() - 1) : name;
            name += ".java";
            if (fileNames.contains(name)) {
                continue; // Do not add to b.
            } else {
                fileNames.add(name);
            }
        }
        // Sort alphabetically
        Collections.sort(fileNames);

        return fileNames;
    }

    @Override
    public String toString() {
        ArrayList<String> fileNames = getClassNames();

        // Add to the string buffer alphabetically
        StringBuilder b = new StringBuilder();
        for (String s : fileNames) {
            b.append(s);
            b.append("\n");
        }

        //b.append("Total number of classes used: " + classes.size() + "\r\n");
        String result = b.toString();
        return result;
    }

    /**
     *
     * @param args - The path of the text file containing each root class to
     * load.
     */
    public static void main(String[] args) {

        String fileWithClassList = "relevantClassList";
        SortedSet<String> classNames = new TreeSet<>();

        InputStream classListStream = ClassHierarchyPrinter.class.getResourceAsStream(fileWithClassList);

        Path p = Paths.get(fileWithClassList);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(classListStream, "UTF-8"))) {
            String className;
            while ((className = r.readLine()) != null) {
                if (className.startsWith("#")) {
                    // Comment character
                    continue;
                }
                Class clazz = ClassLoader.getSystemClassLoader().loadClass(className);
                ClassHierarchyPrinter printer = new ClassHierarchyPrinter(clazz);
                classNames.addAll(printer.getClassNames());
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String s : classNames) {
                if (first) {
                    first = false;
                } else {
                    sb.append("\n");
                }
                sb.append(s);
            }
            System.out.println(sb.toString());

        } catch (NoSuchFileException ex) {
            Logger.getLogger(ClassHierarchyPrinter.class.getName()).log(Level.SEVERE, "Could not "
                    + "find the file: " + fileWithClassList);
        } catch (IOException ex) {
            Logger.getLogger(ClassHierarchyPrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClassHierarchyPrinter.class.getName()).log(Level.SEVERE,
                    "Could not find one of the classes specified", ex);
        }
    }
}
