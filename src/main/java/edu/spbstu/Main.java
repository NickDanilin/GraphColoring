package edu.spbstu;

import edu.spbstu.menu.ResourceClassHelpInfoProvider;
import edu.spbstu.menu.StreamIOAdapter;

public class Main {
    public static void main(String[] args) {
        App app = new App(
                new StreamIOAdapter(System.out, System.in),
                new ResourceClassHelpInfoProvider(Main.class, "/infohelp.txt")
        );
        app.run();
    }
}