package ru.ifmo.ctddev.toropin.implementor;

/**
 * This exception is throw when we see something wrong with compilation
 *
 * @author Toropin Konstantin
 *
 * @see JARCompiler#compile
 **/
public class CompilationException extends Exception {

    /**
     * Ordinary exception bilder for making Jar files
     *
     * @param message Error message
     */
    public CompilationException(String message) {
        super(message);
    }
}
