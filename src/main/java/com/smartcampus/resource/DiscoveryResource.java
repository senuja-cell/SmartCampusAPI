package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", "1.0");
        response.put("name", "Smart Campus API");
        response.put("description", "REST API for managing campus rooms, sensors and readings");

        Map<String, String> contact = new HashMap<>();
        contact.put("owner", "Senuja Ranmith");
        contact.put("email", "w2120691@westminster.ac.uk");
        response.put("contact", contact);

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("readings", "/api/v1/sensors/{sensorId}/readings");
        response.put("resources", links);

        return Response.ok(response).build();
    }
}
