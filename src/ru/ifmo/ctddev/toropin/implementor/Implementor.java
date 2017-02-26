package ru.ifmo.ctddev.toropin.implementor;

import java.io.File;
import java.lang.reflect.*;

/**
 * Created by impy on 26.02.17.
 */
public class Implementor {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("Integer");
        String path = new File("").getAbsolutePath();
        File file = new File(path + args[0] + ".java");
        for (Field m : aClass.getDeclaredFields()) {
            System.out.println(m);
        }
        for (Constructor m : aClass.getDeclaredConstructors()) {
            System.out.println(m);
        }
        for (Method m : aClass.getDeclaredMethods()) {
            System.out.println(m);
        }
    }
}
