package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.RoomNotEmptyException;
import com.westminster.smartcampus.model.ApiError;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.store.DataStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        synchronized (store.getRooms()) {
            List<Room> rooms = new ArrayList<>(store.getRooms().values());
            return Response.ok(rooms).build();
        }
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null) {
            return buildError(Response.Status.BAD_REQUEST, "Request body is required", uriInfo);
        }

        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Room id is required", uriInfo);
        }

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Room name is required", uriInfo);
        }

        if (room.getCapacity() <= 0) {
            return buildError(Response.Status.BAD_REQUEST, "Room capacity must be greater than 0", uriInfo);
        }

        Map<String, Room> rooms = store.getRooms();

        synchronized (rooms) {
            if (rooms.containsKey(room.getId())) {
                return buildError(Response.Status.CONFLICT, "Room with this id already exists", uriInfo);
            }

            if (room.getSensorIds() == null) {
                room.setSensorIds(new ArrayList<>());
            }

            rooms.put(room.getId(), room);
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(location)
                .entity(room)
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId, @Context UriInfo uriInfo) {
        Room room = store.getRooms().get(roomId);

        if (room == null) {
            return buildError(Response.Status.NOT_FOUND, "Room not found", uriInfo);
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId, @Context UriInfo uriInfo) {
        Room room = store.getRooms().get(roomId);

        if (room == null) {
            return buildError(Response.Status.NOT_FOUND, "Room not found", uriInfo);
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " still contains sensors");
        }

        store.getRooms().remove(roomId);

        Map<String, String> message = new HashMap<>();
        message.put("message", "Room deleted successfully");

        return Response.ok(message).build();
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