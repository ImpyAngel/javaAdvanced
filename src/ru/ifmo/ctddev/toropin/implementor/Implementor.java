package ru.ifmo.ctddev.toropin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors ;

/**
 *  Build code for implementing classes and interfaces by <tt>token</tt>
 *  <p>
 *  <code>Implementor</code> implements all abstract methods from class with default
 *  value and constructors with <code>super()</code> Name of this file is name of <tt>token</tt> with suffix <tt>Impl.java</tt>
 *  For details of usage, see method implement(Class, Path)
 *  @author Toropin Konstantin
 */
public class Implementor implements JarImpler{

    /**
     * Field with code of implementional class
     */
    private StringBuilder data;

    /**
     * Create and return path of implementional <code>token</code> in the path <code>root</code>
     * @param token class to implement
     * @param root Directory of packages root
     * @return Path path to the directory, where would placed implemention
     * @throws IOException when creating files has failed
     */

    private static Path getImplementedFilename(Class<?> token, Path root) throws IOException {
        if (token.getPackage() != null) {
            root = root.resolve(token.getPackage().getName().replace('.', File.separatorChar) + File.separator);
            Files.createDirectories(root);
        }
        return root.resolve(token.getSimpleName() + "Impl.java");
    }

    /**
     * Appends to string with textual representation
     * @param mod Modifiers to print in class
     * @param type mask of Modifiers
     * @see Modifier#toString(int)
     */
    private void modifiers(int mod, int type) {
        data.append(Modifier.toString(mod & ~Modifier.ABSTRACT & type)).append(" ");
    }

    /**
     * Created header of class with <tt>implements</tt> and <tt>extends</tt>
     * @param token class for the implement
     */
    private void classHeader(Class<?> token) {

        modifiers(token.getModifiers(), Modifier.classModifiers());
        data.append("class ");
        data.append(token.getSimpleName());
        data.append("Impl ");
        if (token.isInterface()) {
            data.append("implements ");
        } else {
            data.append("extends ");
        }
        data.append(token.getCanonicalName());
        data.append(" {");
        data.append(System.lineSeparator());
    }

    /**
     * Add parameters to method with their default arguments names
     * <p>
     * Code write to the data
     * @param parameters array of method parameters
     */
    private void buildParameters(Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            modifiers(p.getModifiers(), Modifier.parameterModifiers());
            data.append(p.getType().getCanonicalName());
            data.append(' ');
            data.append(p.getName());
            if (i != parameters.length - 1) {
                data.append(", ");
            }
        }
    }

    /**
     * Generate code for implement constructors of calss <tt>token</tt>
     * this code is default <code>super()</code>
     * <p>
     * Code write to the data
     * @param token class for implement
     * @param cons constructor of class
     */
    private void buildConstructor(Class<?> token,Constructor<?> cons) {

        data.append("\t");
        modifiers(cons.getModifiers(), Modifier.constructorModifiers());
        data.append(token.getSimpleName()).append("Impl");
        data.append("(");
        buildParameters(cons.getParameters());
        data.append(")");
        Class<?>[] exc = cons.getExceptionTypes();
        if (exc.length > 0) {
            data.append(" throws ");
            for (int i = 0; i < exc.length; i++) {
                data.append(exc[i].getCanonicalName());
                if (i != exc.length - 1) {
                    data.append(", ");
                }
            }
        }
        data
                .append(" {")
                .append(System.lineSeparator())
                .append("\t\t")
                .append("super(");
        Parameter[] parameters = cons.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            data.append(parameters[i].getName());
            if (i != parameters.length - 1) {
                data.append(", ");
            }
        }

        data
                .append(");")
                .append(System.lineSeparator())
                .append("\t")
                .append("}")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

    }

    /**
     * Produced <tt>.jar</tt> file implementing class by <tt>aClass</tt>
     * <p>
     * Name of file is name of class with suffix <tt>Impl.jar</tt>
     *
     * @param aClass class for implementing
     * @param path place for created file
     * @throws ImplerException when implementation can't be generated.
     */
    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        try {
            Path tmp = Paths.get(".");

            this.implement(aClass, tmp);
            Path code = Implementor.getImplementedFilename(aClass, tmp).normalize();
            JARCompiler.compile(tmp, code);
            Path classfile = JARCompiler.getClassFilename(code);
            JARCompiler.writeJAR(path, classfile);
            classfile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new ImplerException("I/O error: " + e.getMessage());
        } catch (CompilationException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Class for container Methods.
     * <p>
     * Methods compare and create hash code by name of method.
     * It need for the HashSet
     */
    private class Methods {
        /**
         * Data with method
         */
        final Method m;

        /**
         * simply constructor
         * @param m data
         */
        private Methods(Method m) {
            this.m = m;
        }

         /**
         * Two methods are considering equals here, if they have have same names and parameters types.
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Methods)) {
                return false;
            }
            Method m2 = ((Methods) obj).m;

            return m.getName().equals(m2.getName()) && Arrays.equals(m.getParameterTypes(), m2.getParameterTypes());
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int hash = m.getName().hashCode();
            for (Parameter p : m.getParameters()) {
                hash ^= p.getType().hashCode();
            }
            return hash;
        }
    }

    /**
     * Collects all abstract methods of class represented by <code>token</code> type token.
     * <p>
     * Abstract methods are collected from class itself, all its interfaces and superclasses.
     * If method is not overridden in this class, but has implementation in one of superclasses,
     * it is not considered abstract.
     *
     * @param token to collect methods from
     * @return set of all abstract methods
     */
    private Set<Method> collectAbstractMethods(Class<?> token) {

        Set<Methods> abstractMethods = new HashSet<>();

        if (token == null || !Modifier.isAbstract(token.getModifiers())) {
            return Collections.emptySet();
        }

        for (Method m : token.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                abstractMethods.add(new Methods(m));
            }
        }

        while (token != null) {
            if (!Modifier.isAbstract(token.getModifiers())) {
                break;
            }

            for (Method m : token.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (!Modifier.isPrivate(mod) && !Modifier.isPublic(mod)) {
                    abstractMethods.add(new Methods(m));
                }
            }
            token = token.getSuperclass();
        }

        abstractMethods.removeIf(method -> !Modifier.isAbstract(method.m.getModifiers()));

        return abstractMethods.stream().map(box -> box.m).collect(Collectors.toSet());
    }


    /**
     * Generates the string representing the default value of class with given <code>token</code>.
     * <p>
     * It is <tt>null</tt> for non-primitive types,
     * <tt>false</tt> for Boolean boolean,
     * empty string for
     * and <tt>0</tt> for other.
     *
     * @param token type token to get default value for.
     * @return with default value.
     */
    public static String defaultValue(Class<?> token) {
        if (!token.isPrimitive()) {
            return "null";
        } else if (token.equals(void.class)) {
            return "";
        } else if (token.equals(boolean.class)) {
            return "false";
        } else {
            return "0";
        }
    }
     /**
     * Generates code with implementation of given method.
     * Code does not do anything by returning default value of method's return type.
     * <p>
     * This method resets and uses it to construct the text.
     *
     * @param m method to generate implementation for
     */
    private void buildMethod(Method m) {

        data.append("\t");
        modifiers(m.getModifiers(), Modifier.methodModifiers());
        data
                .append(m.getReturnType().getCanonicalName())
                .append(" ")
                .append(m.getName())
                .append("(");
        buildParameters(m.getParameters());
        data
                .append(")")
                .append(" {")
                .append(System.lineSeparator())
                .append("\t").append("\t").append("return ")
                .append(defaultValue(m.getReturnType()))
                .append(";")
                .append(System.lineSeparator())
                .append("\t").append("}")
                .append(System.lineSeparator());

        data.append(System.lineSeparator());

    }
     /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when implementation cannot be
     *                         generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        data = new StringBuilder();
        if (token == null || root == null) {
            throw new ImplerException("Files are empty");
        }

        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Class is final!");
        }
        if (token == Enum.class) {
            throw new ImplerException("Class is enum");
        }

        if (token.getPackage() != null) {
            data.append("package ")
                    .append(token.getPackage().getName())
                    .append(";")
                    .append(System.lineSeparator());
        }
        classHeader(token);

        int contCnt = 0;
        Constructor<?>[] constructors = token.getDeclaredConstructors();
        for (Constructor<?> cons : constructors) {
            if (!Modifier.isPrivate(cons.getModifiers())) {
                buildConstructor(token, cons);
                contCnt++;
            }
        }

        if (contCnt == 0 && constructors.length > 0) {
            throw new ImplerException("Have no constructors!");
        }

        Set<Method> methods = collectAbstractMethods(token);
        for (Method m : methods) {
            buildMethod(m);
        }
        data.append("}");
        try (Writer writer = new ImplerWriter(new OutputStreamWriter
                (new FileOutputStream(String.valueOf(getImplementedFilename(token, root))), "UTF-8"))) {
            writer.write(data.toString());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Filters non-ASCII characters in output stream and converts it to "\\uXXXX" sequences.
     * <p>
     * You should use write(String, int, int) to filter.
     */
    private class ImplerWriter extends FilterWriter {
         /**
         * Construct the Filter on provided Writer
         *
         */
        protected ImplerWriter(Writer writer) {
            super(writer);
        }

        /**
         * Replaces unicode characters in <code>string</code> to "\\uXXXX" sequences
         * <p>
         * {@inheritDoc}
         */
        @Override
        public void write(String string, int off, int len) throws IOException {
            StringBuilder b = new StringBuilder();
            for (char c : string.substring(off, off + len).toCharArray()) {
                if (c >= 128) {
                    b.append(String.format("\\u%04X", (int) c));
                } else {
                    b.append(c);
                }
            }
            super.write(b.toString(), 0, b.length());
        }
    }
}
