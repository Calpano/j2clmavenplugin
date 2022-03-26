package com.vertispan.j2cl.tools;

import com.google.j2cl.common.SourceUtils.FileInfo;
import com.vertispan.j2cl.build.task.BuildLog;

import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Runs javac. Set this up with the appropriate classpath, directory for generated sources to be written,
 * and directory for bytecode to be written, and can be requested to preCompile any .java file where the
 * dependencies are appropriately already available.
 * <p>
 * The classesDirFile generally should be in the classpath list.
 * <p>
 * Note that incoming sources should already be pre-processed, and while it should be safe to directly
 * j2cl the generated classes, it may be necessary to pre-process them before passing them to j2cl.
 */
public class Javac {

    private final BuildLog log;
    List<String> javacOptions;
    JavaCompiler compiler;
    StandardJavaFileManager fileManager;
    private DiagnosticCollector<JavaFileObject> listener;

    public Javac(BuildLog log, File generatedClassesPath, List<File> sourcePaths, List<File> classpath, File classesDirFile, File bootstrap, Mode mode) throws IOException {
        this.log = log;
        compiler = ToolProvider.getSystemJavaCompiler();
        listener = new DiagnosticCollector<>();
        // null = standard charset on windows = cp.... (some kind of non-utf8 charset)
        fileManager = compiler.getStandardFileManager(listener, null, StandardCharsets.UTF_8);
//        for (File file : classpath) {
//            System.out.println(file.getAbsolutePath() + " " + file.exists() + " " + file.isDirectory());
//        }
        javacOptions = new ArrayList<>(Arrays.asList("-encoding", "utf8", "-implicit:none"));
        javacOptions.add("-verbose");
        if (generatedClassesPath == null) {
            javacOptions.add("-proc:none");
        }

        final String javaVersion = "java.specification.version:" + System.getProperty("java.specification.version");
        String bootClassPath = bootstrap.getCanonicalFile().getAbsolutePath();

        if (SourceVersion.latestSupported().compareTo(SourceVersion.RELEASE_8) > 0) {
            // Java version >= 9
            //javacOptions.add("--release=8");
            if (mode == Mode.JreEmulation) {
                log.info("Activate JRE emulation for this step on Java >= 9: " + javaVersion);
                javacOptions.add("--patch-module");
                javacOptions.add("java.base=" + bootClassPath);
                // Allow JRE classes are allowed to depend on the jsinterop annotations
                javacOptions.add("--add-reads");
                javacOptions.add("java.base=ALL-UNNAMED");
                log.info("Using java.base=" + bootClassPath);
                /* Maybe this works on Java 17? */
                // requires a path that meets Locations.isCurrentPlatform(p) && Files.exists(p.resolve("lib").resolve("jrt-fs.jar")) && Files.exists(systemJavaHome.resolve("modules")))
                // fileManager.setLocation( StandardLocation.SYSTEM_MODULES   ,Collections.singleton(bootstrap.getParentFile()));
            } else {
                log.info("No JRE emulation for this step on Java >= 9: " + javaVersion);
            }
//            javacOptions.add("-Xdiags:verbose");
//            javacOptions.add("-Xlint:path,auxiliaryclass,module,options");
        } else {
            // Java version <= 8
            if (mode == Mode.JreEmulation) {
                log.info("Activate JRE emulation for this step on Java <= 8: " + javaVersion);
                javacOptions.add("-bootclasspath");
                javacOptions.add(bootClassPath);
                log.info("Using bootclasspath " + bootClassPath);
                /* This works on AdoptOpenJDK Java 8, not on Oracle Java 17 */
                prependPathsWith(fileManager, StandardLocation.PLATFORM_CLASS_PATH, bootstrap);
            } else {
                log.info("No JRE emulation for this step on Java <= 8: " + javaVersion);
            }
        }

        fileManager.setLocation(StandardLocation.SOURCE_PATH, sourcePaths);
        if (generatedClassesPath != null) {
            fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.singleton(generatedClassesPath));
        }
        fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(classesDirFile));


        log.info("Javac options are: ");
        for (String opt : javacOptions) {
            log.info("'" + opt + "'");
        }
    }

    private static void prependPathsWith(StandardJavaFileManager fileManager, StandardLocation location, File prependPath) throws IOException {
        ArrayList<File> paths = new ArrayList<>();
        paths.add(prependPath);
        Optional.ofNullable(fileManager.getLocation(location)).ifPresent(files -> files.forEach(paths::add));
        fileManager.setLocation(location, paths);
    }

    public boolean compile(List<FileInfo> modifiedJavaFiles) {
        // preCompile java files with javac into classesDir
        Iterable<? extends JavaFileObject> modifiedFileObjects = fileManager.getJavaFileObjectsFromStrings(modifiedJavaFiles.stream().map(FileInfo::sourcePath).collect(Collectors.toList()));
        //TODO pass-non null for "classes" to properly kick apt?
        //TODO consider a different classpath for this tasks, so as to not interfere with everything else?

        CompilationTask task = compiler.getTask(null, fileManager, listener, javacOptions, null, modifiedFileObjects);

        try {
            return task.call();
        } finally {
            listener.getDiagnostics().forEach(d -> {
                String messageToLog = d.getMessage(Locale.getDefault());
                JavaFileObject source = d.getSource();
                if (source != null) {
                    String longFileName = source.toUri().getPath();
                    String prefix = longFileName + ((d.getLineNumber() > 0) ? ":" + d.getLineNumber() : "") + " ";
                    messageToLog = prefix + messageToLog;
                }
                switch (d.getKind()) {
                    case ERROR:
                        log.error(messageToLog);
                        break;
                    case WARNING:
                    case MANDATORY_WARNING:
                        log.warn(messageToLog);
                        break;
                    case NOTE:
                        log.info(messageToLog);
                        break;
                    case OTHER:
                        log.debug(messageToLog);
                        break;
                }
            });
        }
    }

    public enum Mode {
        JreEmulation, NativeJre
    }
}
