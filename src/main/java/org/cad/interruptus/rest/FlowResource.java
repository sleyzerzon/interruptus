package org.cad.interruptus.rest;

import com.espertech.esper.client.dataflow.EPDataFlowState;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cad.interruptus.core.EntityNotFoundException;
import org.cad.interruptus.core.esper.FlowConfiguration;
import org.cad.interruptus.entity.Flow;
import org.cad.interruptus.repository.FlowRepository;
import org.cad.interruptus.repository.zookeeper.listener.ConfigurationEventDispatcher;

@Singleton
@Path("/flow")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Api(value = "/flow", description = "Flow operations")
public class FlowResource
{
    @Inject
    FlowRepository repository;

    @Inject
    FlowConfiguration configuration;

    @Inject
    ConfigurationEventDispatcher dispatcher;

    Log logger = LogFactory.getLog(getClass());

    @GET
    @ApiOperation(
        value = "List all flows",
        notes = "List all flows, whether is runnig or not",
        response = Flow.class,
        responseContainer = "List"
    )
    public List<Flow> listFlows()
    {
        try {
            return repository.findAll();
        } catch (Exception ex) {
            logger.error(this, ex);
            throw new ResourceException(Response.Status.SERVICE_UNAVAILABLE, ex.getMessage());
        }
    }

    @POST
    @ApiOperation(
        value = "Save a flow configuration",
        notes = "Save a flow configuration, if the flow already exists will be overwritten",
        response = Boolean.class
    )
    public Boolean saveFlow(Flow entity)
    {
        try {

            configuration.save(entity);
            repository.save(entity);
            dispatcher.dispatchSave(entity);

            return Boolean.TRUE;
        } catch (Exception ex) {
            logger.error(this, ex);
            throw new ResourceException(Response.Status.SERVICE_UNAVAILABLE, ex.getMessage());
        }
    }

    @GET
    @Path("/{name}")
    @ApiOperation(
        value = "Retreives a flow configuration",
        notes = "Retreives a flow configuration, throws exception if does not exists",
        response = Flow.class
    )
    @ApiResponses({
        @ApiResponse(code = 404, message = "Flow doesn't exists")
    })
    public Flow showFlow(@ApiParam(value = "Flow name to lookup for", required = true) @PathParam("name") String name)
    {
        try {
            return repository.findById(name);
        } catch (EntityNotFoundException ex) {
            throw new ResourceException(ex);
        } catch (Exception ex) {
            logger.error(this, ex);
            throw new ResourceException(Response.Status.SERVICE_UNAVAILABLE, ex.getMessage());
        }
    }

    @DELETE
    @Path("/{name}")
    @ApiOperation(
        value = "Removes a flow configuration",
        notes = "Removes a flow configuration, throws exception if does not exists",
        response = Flow.class
    )
    @ApiResponses({
        @ApiResponse(code = 404, message = "Flow doesn't exists")
    })
    public Boolean removeFlow(@ApiParam(value = "Flow name to lookup for", required = true) @PathParam("name") String name)
    {
        try {

            final Flow entity = repository.findById(name);

            configuration.stop(name);
            repository.remove(name);
            dispatcher.dispatchDelete(entity);

            return true;
        } catch (EntityNotFoundException ex) {
            throw new ResourceException(ex);
        } catch (Exception ex) {
            logger.error(this, ex);
            throw new ResourceException(Response.Status.SERVICE_UNAVAILABLE, ex.getMessage());
        }
    }

    @POST
    @Path("/{name}/start")
    @ApiOperation(
        value = "Start a flow in esper",
        notes = "Stop a existing in esper, throws exception if does not exists",
        response = Flow.class
    )
    @ApiResponses({
        @ApiResponse(code = 404, message = "Flow doesn't exists")
    })
    public Boolean startFlow(@ApiParam(value = "Flow name to lookup for", required = true) @PathParam("name") String name) throws Exception
    {
        final Flow entity = repository.findById(name);

        entity.setStarted(true);
        dispatcher.dispatchSave(entity);
        repository.save(entity);

        return true;
    }

    @POST
    @Path("/{name}/stop")
    @ApiOperation(
        value = "Stop a flow in esper",
        notes = "Stop a existing flow in esper, throws exception if does not exists",
        response = Flow.class
    )
    @ApiResponses({
        @ApiResponse(code = 404, message = "Flow doesn't exists")
    })
    public Boolean stopFlow(@ApiParam(value = "Flow name to lookup for", required = true) @PathParam("name") String name) throws Exception
    {
        final Flow entity = repository.findById(name);

        if ( ! configuration.stop(name)) {
            return false;
        }

        entity.setStarted(false);
        repository.save(entity);
        dispatcher.dispatchSave(entity);

        return true;
    }

    @GET
    @Path("/{name}/state")
    @ApiOperation(
        value = "Retrives the state for a flow",
        notes = "Retrives the state for a flow, throws exception if does not exists",
        response = Flow.class
    )
    @ApiResponses({
        @ApiResponse(code = 404, message = "Flow doesn't exists")
    })
    public Map<String, String> state(@ApiParam(value = "Flow name to lookup for", required = true) @PathParam("name") String name) throws Exception
    {
        final Map<String, String> map = new HashMap<>();
        final EPDataFlowState state   = configuration.getFlowState(name);

        map.put("name", name);
        map.put("status", "STOPPED");

        if (repository.findById(name) == null) {
            throw new EntityNotFoundException(Flow.class, name);
        }

        if (state != null) {
            map.put("status", state.toString());
        }

        return map;
    }
}