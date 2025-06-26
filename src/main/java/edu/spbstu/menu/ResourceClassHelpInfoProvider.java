package edu.spbstu.menu;

import java.io.InputStream;

public class ResourceClassHelpInfoProvider implements HelpInfoProvider {

    private final String helpInfo;

    public ResourceClassHelpInfoProvider(Class<?> resourceClass, String resourceName) {
        try (InputStream inputStream = resourceClass.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            this.helpInfo = new String(inputStream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load help info from resource: " + resourceName, e);
        }
    }

    @Override
    public String getHelpInfo() {
        return helpInfo;
    }
}
