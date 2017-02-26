package ru.ifmo.ctddev.toropin.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.ServerError;
import java.util.Formatter;
import java.util.Scanner;

/**
 * Created by impy on 10.02.17.
 */

class MyException extends Exception {

    public MyException(String s) {
        super(s);
    }
}
public class RecursiveWalk {
    public static class FnvHash {
        private String path;
        private final long FNV_32_PRIME = 16777619;
        private final long REQ = (1 << 8) - 1;
        private final long MOD = (1L << 32);
        public FnvHash(String path) {
            this.path = path;
        }

        public long hash() {
            long hval = 2166136261L;
            byte[] temp = new byte[1024];
            try {
                InputStream reader = new FileInputStream(path);
                int c;
                while ((c = reader.read(temp)) >= 0) {
                    for (int i = 0; i < c; i++) {

                        hval = (hval * FNV_32_PRIME) % MOD ^ ((long) temp[i] & REQ);
                    }
                }
                reader.close();
            }
            catch (IOException e) {
                hval = 0;
            }
            return hval;
        }
    }

    public static void main(String[] args) throws IOException {

        try {
            if (args.length < 2) {
            throw new MyException("Not enough arguments");
        }
            Scanner scanner = new Scanner(new File(args[0]));
            PrintWriter writerGlobal = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
            while (scanner.hasNext()) {
                    String pathIn = scanner.nextLine();
                    Path startPath = Paths.get(pathIn);

                    try {
                        if (Files.isDirectory(startPath)) {
                            SimpleFileVisitor<Path> myVisitor = new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                                    FnvHash fnvHash = new FnvHash(path.toString());
                                    writerGlobal.printf("%08x %s\n", fnvHash.hash(), path);
                                    writerGlobal.flush();

                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                                    writerGlobal.printf("%08x %s\n", 0, path);
                                    writerGlobal.flush();

                                    return FileVisitResult.CONTINUE;
                                }
                            };

                            Files.walkFileTree(startPath, myVisitor);

                        } else {
                            if (Files.isRegularFile(startPath)) {
                                FnvHash fnvHash = new FnvHash(startPath.toString());

                                writerGlobal.printf("%08x %s\n", fnvHash.hash(), startPath);
                                writerGlobal.flush();

                            } else {
                                writerGlobal.printf("%08x %s\n", 0, startPath);
                                writerGlobal.flush();
                            }
                        }
                    } catch (IOException | InvalidPathException e) {
                            e.printStackTrace();
                            writerGlobal.printf("%08x %s\n", 0, startPath);
                            writerGlobal.flush();
                    }

            }
            writerGlobal.close();
        }
        catch (MyException e) {
            System.out.println(e.getMessage());
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        catch (UnsupportedEncodingException e) {
            System.out.println("Wrong with a format");
        }
        catch (SecurityException e) {
            System.out.println("Permission denied");
        }


    }
}


