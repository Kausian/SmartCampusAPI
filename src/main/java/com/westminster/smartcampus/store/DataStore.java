package com.westminster.smartcampus.store;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms =
            Collections.synchronizedMap(new HashMap<>());

    private final Map<String, Sensor> sensors =
            Collections.synchronizedMap(new HashMap<>());

    private final Map<String, List<SensorReading>> readingsBySensor =
            Collections.synchronizedMap(new HashMap<>());

    private DataStore() {
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getReadingsBySensor() {
        return readingsBySensor;
    }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        synchronized (readingsBySensor) {
            return readingsBySensor.computeIfAbsent(
                    sensorId,
                    k -> Collections.synchronizedList(new ArrayList<>())
            );
        }
    }
}