package ru.otr.nzx.config;

import org.json.JSONObject;

public class DumperConfig {
    public final static String TANK_CAPACITY = "tank_capacity";

    public final int tank_capacity;

    public DumperConfig(JSONObject src) {
        this.tank_capacity = src.getInt(TANK_CAPACITY);
    }

    public JSONObject toJSON() {
        return new JSONObject().put(TANK_CAPACITY, tank_capacity);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
