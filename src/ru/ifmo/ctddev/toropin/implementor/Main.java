package ru.ifmo.ctddev.toropin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by impy on 13.03.17.
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException, ImplerException {
        Implementor impl = new Implementor();
        impl.implement(javax.accessibility.Accessible.class, Paths.get("."));
    }
}
