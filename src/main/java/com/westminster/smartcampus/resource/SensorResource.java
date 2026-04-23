package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import com.westminster.smartcampus.model.ApiError;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.store.DataStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensors;

        synchronized (store.getSensors()) {
            sensors = new ArrayList<>(store.getSensors().values());
        }

        if (type != null && !type.trim().isEmpty()) {
            sensors = sensors.stream()
                    .filter(sensor -> sensor.getType() != null
                            && sensor.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null) {
            return buildError(Response.Status.BAD_REQUEST, "Request body is required", uriInfo);
        }

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Sensor id is required", uriInfo);
        }

        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Sensor type is required", uriInfo);
        }

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Sensor status is required", uriInfo);
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "roomId is required", uriInfo);
        }

        Map<String, Sensor> sensors = store.getSensors();
        Map<String, Room> rooms = store.getRooms();

        synchronized (store) {
            if (sensors.containsKey(sensor.getId())) {
                return buildError(Response.Status.CONFLICT, "Sensor with this id already exists", uriInfo);
            }

            Room room = rooms.get(sensor.getRoomId());
            if (room == null) {
                throw new LinkedResourceNotFoundException(
                        "Cannot create sensor because room " + sensor.getRoomId() + " does not exist"
                );
            }

            sensors.put(sensor.getId(), sensor);

            if (room.getSensorIds() == null) {
                room.setSensorIds(new ArrayList<>());
            }

            room.getSensorIds().add(sensor.getId());

            store.getReadingsForSensor(sensor.getId());
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        return Response.created(location)
                .entity(sensor)
                .build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId, @Context UriInfo uriInfo) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor not found", uriInfo);
        }

        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
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