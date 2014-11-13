package bwapi4;

import bwapi4.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class CompareFilter {


    private static Map<Long, CompareFilter> instances = new HashMap<Long, CompareFilter>();

    private CompareFilter(long pointer) {
        this.pointer = pointer;
    }

    private static CompareFilter get(long pointer) {
        CompareFilter instance = instances.get(pointer);
        if (instance == null ) {
            instance = new CompareFilter(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
