package bwapi4;

import bwapi4.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class Event {

    public Position getPosition() {
        return getPosition_native(pointer);
    }

    public String getText() {
        return getText_native(pointer);
    }

    public Unit getUnit() {
        return getUnit_native(pointer);
    }

    public Player getPlayer() {
        return getPlayer_native(pointer);
    }

    public boolean isWinner() {
        return isWinner_native(pointer);
    }


    private static Map<Long, Event> instances = new HashMap<Long, Event>();

    private Event(long pointer) {
        this.pointer = pointer;
    }

    private static Event get(long pointer) {
        Event instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Event(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native Position getPosition_native(long pointer);

    private native String getText_native(long pointer);

    private native Unit getUnit_native(long pointer);

    private native Player getPlayer_native(long pointer);

    private native boolean isWinner_native(long pointer);


}
