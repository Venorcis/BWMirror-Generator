package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class Force {

    public int getID() {
        return getID_native(pointer);
    }

    public String getName() {
        return getName_native(pointer);
    }

    public List<Player> getPlayers() {
        return getPlayers_native(pointer);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Force other = (Force)o;

        if (getID() != other.getID()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = getID();
        return result;
    }

    private static Map<Long, Force> instances = new HashMap<Long, Force>();

    private Force(long pointer) {
        this.pointer = pointer;
    }

    private static Force get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Force instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Force(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native String getName_native(long pointer);

    private native List<Player> getPlayers_native(long pointer);


}
