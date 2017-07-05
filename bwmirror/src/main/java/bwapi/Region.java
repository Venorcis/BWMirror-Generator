package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import bwapi.CenteredObject;

public class Region extends CenteredObject 
{

    public int getID() {
        return getID_native(pointer);
    }

    public int getRegionGroupID() {
        return getRegionGroupID_native(pointer);
    }

    public Position getCenter() {
        return getCenter_native(pointer);
    }

    public boolean isHigherGround() {
        return isHigherGround_native(pointer);
    }

    public int getDefensePriority() {
        return getDefensePriority_native(pointer);
    }

    public boolean isAccessible() {
        return isAccessible_native(pointer);
    }

    public List<Region> getNeighbors() {
        return getNeighbors_native(pointer);
    }

    public int getBoundsLeft() {
        return getBoundsLeft_native(pointer);
    }

    public int getBoundsTop() {
        return getBoundsTop_native(pointer);
    }

    public int getBoundsRight() {
        return getBoundsRight_native(pointer);
    }

    public int getBoundsBottom() {
        return getBoundsBottom_native(pointer);
    }

    public Region getClosestAccessibleRegion() {
        return getClosestAccessibleRegion_native(pointer);
    }

    public Region getClosestInaccessibleRegion() {
        return getClosestInaccessibleRegion_native(pointer);
    }

    public int getDistance(Region other) {
        return getDistance_native(pointer, other);
    }

    public List<Unit> getUnits() {
        return getUnits_native(pointer);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region other = (Region)o;

        if (getID() != other.getID()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = getID();
        return result;
    }

    private static Map<Long, Region> instances = new HashMap<Long, Region>();

    private Region(long pointer) {
        this.pointer = pointer;
    }

    private static Region get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Region instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Region(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native int getRegionGroupID_native(long pointer);

    private native Position getCenter_native(long pointer);

    private native boolean isHigherGround_native(long pointer);

    private native int getDefensePriority_native(long pointer);

    private native boolean isAccessible_native(long pointer);

    private native List<Region> getNeighbors_native(long pointer);

    private native int getBoundsLeft_native(long pointer);

    private native int getBoundsTop_native(long pointer);

    private native int getBoundsRight_native(long pointer);

    private native int getBoundsBottom_native(long pointer);

    private native Region getClosestAccessibleRegion_native(long pointer);

    private native Region getClosestInaccessibleRegion_native(long pointer);

    private native int getDistance_native(long pointer, Region other);

    private native List<Unit> getUnits_native(long pointer);


}
