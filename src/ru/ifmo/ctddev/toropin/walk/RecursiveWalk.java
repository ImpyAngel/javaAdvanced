package ru.ifmo.ctddev.toropin.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

/**
 * Created by impy on 10.02.17.
 */

class MyException extends Exception {
    MyException(String s) {
        super(s);
    }
}

public class RecursiveWalk {
    public static class FnvHash {
        private String path;
        private final int FNV_32_PRIME = 16777619;
        private final int REQ = (1 << 8) - 1;
        private final int HVAL  =  (int)2166136261L;

        FnvHash(String path) {
            this.path = path;
        }

        int hash() {
            try {
                int c;
                int hval = HVAL;
                byte[] temp = new byte[1024];
                try (InputStream reader = new FileInputStream(path)) {
                    while ((c = reader.read(temp)) >= 0) {
                        for (int i = 0; i < c; i++) {
                            hval = (hval * FNV_32_PRIME) ^ (temp[i] & REQ);
                        }
                    }
                    return hval;
                }
            } catch (IOException e) {
                return 0;
            }
        }
    }


    private static void print(PrintWriter writerGlobal, int hash, String path) {
        writerGlobal.printf("%08x %s\n", hash, path);
        writerGlobal.flush();
    }

    public static void main(String[] args) {
        String namesOfFileWithException = null;
        try {
            if (args == null || args.length < 2) {
                throw new MyException("Not enough arguments");
            }
            namesOfFileWithException = "Output file with name \"" + args[1];
            Scanner scanner;
            try {
                scanner = new Scanner(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
            } catch (IOException e) {
                namesOfFileWithException = "InputFile with name \"" + args[0];
                throw e;
            }
            try (PrintWriter writerGlobal = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"))) {
                while (scanner.hasNext()) {
                    String pathIn = scanner.nextLine();
                    try {
                        Path startPath = Paths.get(pathIn);
                        if (Files.isDirectory(startPath)) {
                            SimpleFileVisitor<Path> myVisitor = new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                                    FnvHash fnvHash = new FnvHash(path.toString());
                                    print(writerGlobal, fnvHash.hash(), path.toString());
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                                    print(writerGlobal, 0, path.toString());
                                    return FileVisitResult.CONTINUE;
                                }
                            };
                            Files.walkFileTree(startPath, myVisitor);
                        } else {
                            if (Files.isRegularFile(startPath)) {
                                FnvHash fnvHash = new FnvHash(startPath.toString());
                                print(writerGlobal, fnvHash.hash(), startPath.toString());
                            } else {
                                print(writerGlobal, 0, startPath.toString());
                            }
                        }
                    } catch (IOException | InvalidPathException e) {
//                        e.printStackTrace();
                        print(writerGlobal, 0, pathIn);
                    }
                }
            }
        } catch (MyException e) {
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(namesOfFileWithException + "\" : Wrong with a format file");
        } catch (SecurityException e) {
            System.out.println(namesOfFileWithException + "\" : Permission denied");
        } catch (NullPointerException e) {
            System.out.println("Wrong with string's format of invoke class");
        } catch (FileNotFoundException e) {
            System.out.println(namesOfFileWithException + "\" : Not found");
        }
    }
}


