package example;

import example.sharedlib.SharedLib;
import elemental2.dom.DomGlobal;

public class App {

    public void onModuleLoad() {
        DomGlobal.console.log("Client code runs");
        DomGlobal.console.log("Let's calculate. "+SharedLib.add(3,7));
    }

}
