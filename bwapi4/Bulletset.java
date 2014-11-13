package bwapi4;

import bwapi4.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class Bulletset {


    private static Map<Long, Bulletset> instances = new HashMap<Long, Bulletset>();

    private Bulletset(long pointer) {
        this.pointer = pointer;
    }

    private static Bulletset get(long pointer) {
        Bulletset instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Bulletset(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
