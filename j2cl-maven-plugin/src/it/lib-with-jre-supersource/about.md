Two projects:
- app - `.war`, client code, using `super`
- super - customer, user-supplied JRE emulation classes

The relevant lines in the `build.log` are

1)
```
[INFO] Starting lib-with-jre-supersource:super:1.0/bytecode
[INFO] lib-with-jre-supersource:super:1.0/bytecode: No JRE emulation for this step on Java >= 9: java.specification.version:17
[INFO] Starting com.google.jsinterop:jsinterop-annotations:2.0.0/stripped_sources
[INFO] Starting com.vertispan.jsinterop:base:1.0.0-1/stripped_sources
```
2)
```
[ERROR] lib-with-jre-supersource:super:1.0/bytecode: /C:/_data_/_p_/_git/GitHub/j2clmavenplugin/j2cl-maven-plugin/target/it-tests/lib-with-jre-supersource/super/src/main/java/java/util/UUID.java:16 package exists in another module: java.base
```
3)
```
[ERROR] lib-with-jre-supersource:super:1.0/bytecode: /C:/_data_/_p_/_git/GitHub/j2clmavenplugin/j2cl-maven-plugin/target/it-tests/lib-with-jre-supersource/super/src/main/java/java/util/UUID.java:43 no suitable constructor found for UUID(no arguments)
    constructor java.util.UUID.UUID(byte[]) is not applicable
      (actual and formal argument lists differ in length)
    constructor java.util.UUID.UUID(long,long) is not applicable
      (actual and formal argument lists differ in length)
```

First the compile starts fine (1), then `javac` determine that `java.util.UUID` was already in another _module_, namely in `java.base`.

NOTE: The exact same error can happen in `stripped_bytecode`.

To actually make `javac` read the new UUID class from our supplied `super`-library, we must:

- Use `--patch-module` to add `lib-with-jre-supersource-super-1.0.jar` as a JAR on the patch-module-path 
- Allow reads from any module to `java.base`


    --patch-module java.base={{bootClassPath}}
    --add-reads java.base=ALL-UNNAMED


And because we don't know which author of JAR dependencies intend on overwriting JRE built-in classes (this set varies between JDK versions), we must put _all_ JARs into the bootClassPath?

Which java modules besides base exist? `java --list-modules` tells us:

- java.base
- java.compiler
- java.datatransfer
- java.desktop
- java.instrument
- java.logging
- java.management
- java.management.rmi
- java.naming
- java.net.http
- java.prefs
- java.rmi
- java.scripting
- java.se
- java.security.jgss
- java.security.sasl
- java.smartcardio
- java.sql
- java.sql.rowset
- java.transaction.xa
- java.xml
- java.xml.crypto



