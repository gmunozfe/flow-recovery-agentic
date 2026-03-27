package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.model.OrderRiskRequest;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/workflow/order-agentic")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Blocking
public class OrderAgenticResource {

    @Inject
    OrderAgenticWorkflow orderAgenticWorkflow;

    @POST
    public Uni<Object> start(OrderRiskRequest request) {
        return orderAgenticWorkflow
                .startInstance(request)
                .onItem()
                .transform(w -> w.as(Object.class).orElseThrow());
    }
}
