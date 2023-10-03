## Problem Description
When the user tries to supply JRE types **on Java 9+**, they can currently not be used.

## Example
Two projects:
- app - `.war`, client code, using `super`
- super - customer, user-supplied JRE emulation classes

The relevant lines in the `build.log` are

1) First the compile starts fine ...
```
[INFO] Starting lib-with-jre-supersource:super:1.0/bytecode
[INFO] lib-with-jre-supersource:super:1.0/bytecode: No JRE emulation for this step on Java >= 9: java.specification.version:17
```
2) ...  then `javac` determines that `java.util.UUID` was already in another _module_, namely in `java.base`.
```
[ERROR] lib-with-jre-supersource:super:1.0/bytecode:
   /C:/_data_/_p_/_git/GitHub/j2clmavenplugin/j2cl-maven-plugin/target
   /it-tests/lib-with-jre-supersource/super/src/main/java/java/util/UUID.java:16 
   package exists in another module: java.base
```
3) After loading the emulated UUID failed, the original UUID causes problems in J2CL.
```
[ERROR] lib-with-jre-supersource:super:1.0/bytecode:
   /C:/_data_/_p_/_git/GitHub/j2clmavenplugin/j2cl-maven-plugin/target
   /it-tests/lib-with-jre-supersource/super/src/main/java/java/util/UUID.java:43 
   no suitable constructor found for UUID(no arguments)
    constructor java.util.UUID.UUID(byte[]) is not applicable
      (actual and formal argument lists differ in length)
    constructor java.util.UUID.UUID(long,long) is not applicable
      (actual and formal argument lists differ in length)
```

NOTE: The exact same error can happen not only in `bytecode` but also in `stripped_bytecode`.

## Solution Ideas
To actually make `javac` read the new UUID class from our supplied `super`-library, we must:

- Use `--patch-module` to add `lib-with-jre-supersource-super-1.0.jar` as a JAR on the patch-module-path 
- Allow reads from any module to `java.base`


    --patch-module java.base={{bootClassPath}}
    --add-reads java.base=ALL-UNNAMED


**And because we don't know which author of JAR dependencies intend on overwriting JRE built-in classes (this set varies between JDK versions), we must put _all_ JARs into the bootClassPath?**

**But we don't even know, which Java modules is targeted, so need to repeat all JARs to all --patch-module modules?**

**And we must open all modules?**
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

## Conclusion
We should add options in the maven plugin to state entries for `--patch-module` and `--add-reads`.

    <supersource>
        <module>
            <name>java.base</name>
            <groupId>com.example</groupId>
            <artifactId>libA</artifactId>
            <versions>1.0</version>
        <module>
        <module>
            <name>java.net.http</name>
            <groupId>com.example</groupId>
            <artifactId>libB</artifactId>
            <versions>1.0</version>
        <module>
    </supersource>

which results in 

    --patch-module java.base=.m2/com/example/com-example-libA-1.0.jar java.net.http=.m2/com/example/com-example-libB-1.0.jar
    --add-reads java.base=ALL-UNNAMED java.net.http=ALL-UNNAMED

