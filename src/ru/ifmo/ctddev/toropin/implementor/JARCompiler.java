package ru.ifmo.ctddev.toropin.implementor;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Provides static methods for compiling *.java files
 * and writing *.class files into JAR archive.
 *
 **/
public class JARCompiler {

    /**
     * Only static methods of class should be used, so default constructor made private.
     * <p>
     * Does nothing.
     */
    private JARCompiler() {

    }

    public static void compile(Path packageRoot, Path codeFileName) throws CompilationException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new CompilationException("Compiler not found");
        }
        List<String> args = new ArrayList<>();
        args.add(codeFileName.toString());
        args.add("-cp");
        args.add(packageRoot + File.pathSeparator + System.getProperty("java.class.path"));

        int code = compiler.run(null, null, null, args.toArray(new String[args.size()]));
        if (code != 0) {
            throw new CompilationException("Compiler returned non-zero exit code");
        }

    }

    /**
     * Creates jar archive at <code>jarDirectory</code> with given <code>jarName</code>
     * and copies existing <code>fileName</code> to it.
     * <p>
     * If archive already exists, overwrites it.
     *
     * @param jarFile  The path for jar archive. Directories on path should exist.
     * @param fileName The name of file to be copied into archive. Should exist.
     * @throws IOException If I/O error occurred, e.g. if <code>fileName</code> of <code>jarDirectory</code>
     *                     don't exist, or if unexpected error happened during creating or writing to archive.
     */
    public static void writeJAR(Path jarFile, Path fileName) throws IOException {
        try (
                JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile));
                InputStream file = Files.newInputStream(fileName)) {

            stream.putNextEntry(new ZipEntry(fileName.toString()));
            int c;
            byte[] buffer = new byte[1024];
            while ((c = file.read(buffer)) >= 0) {
                stream.write(buffer, 0, c);
            }
            stream.closeEntry();
        }
    }

    /**
     * Generates a path to compiled .class file by path to .java file.
     * In fact, replaces <code>implementedFilename</code> .java extension to .class extension.
     *
     * @param implementedFilename A path to .java file
     * @return A path to corresponding .class file in same folder
     * @throws IllegalArgumentException If <code>implementedFilename</code> doesn't have .java extension
     */
    static Path getClassFilename(Path implementedFilename) {
        String s = implementedFilename.toString();
        if (!s.endsWith(".java")) {
            throw new IllegalArgumentException("Provided path is not a path to .java file");
        }
        s = s.substring(0, s.length() - 5);
        return Paths.get(s + ".class");
    }
}
