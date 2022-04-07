package example;

import elemental2.dom.DomGlobal;
import java.util.UUID;

public class App {

    public void onModuleLoad() {
        DomGlobal.console.log("Client code runs");
        UUID uuid = UUID.fromString("hello");
        DomGlobal.console.log("Now we need emulated code. A custom UUID: "+uuid);
    }

}
