package org.aincraft.container.display;

public class PropertyNotFoundException extends Exception {

    private final String identifier;
    public PropertyNotFoundException(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
