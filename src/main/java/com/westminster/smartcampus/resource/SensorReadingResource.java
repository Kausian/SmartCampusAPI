package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.SensorUnavailableException;
import com.westminster.smartcampus.model.ApiError;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.store.DataStore;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings(@Context UriInfo uriInfo) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor not found", uriInfo);
        }

        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor not found", uriInfo);
        }

        if (reading == null) {
            return buildError(Response.Status.BAD_REQUEST, "Reading body is required", uriInfo);
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is under maintenance");
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        synchronized (store) {
            List<SensorReading> readings = store.getReadingsForSensor(sensorId);
            readings.add(reading);
            sensor.setCurrentValue(reading.getValue());
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(reading.getId())
                .build();

        return Response.created(location)
                .entity(reading)
                .build();
    }

    private Response buildError(Response.Status status, String message, UriInfo uriInfo) {
        ApiError error = new ApiError(
                status.getStatusCode(),
                status.getReasonPhrase(),
                message,
                uriInfo.getPath()
        );

        return Response.status(status)
                .entity(error)
                .build();
    }
}