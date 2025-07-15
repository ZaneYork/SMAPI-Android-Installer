package org.jf.dexlib2.builder;


public abstract class ItemWithLocation {

    MethodLocation location;

    public boolean isPlaced() {
        return location != null;
    }

    public void setLocation(MethodLocation methodLocation) {
        location = methodLocation;
    }
}
