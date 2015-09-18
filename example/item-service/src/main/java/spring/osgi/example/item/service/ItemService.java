package spring.osgi.example.item.service;

import javax.ws.rs.*;
import java.util.List;

/**
 * Created by nico.
 */
@Path("/items")
@Consumes("application/json")
@Produces("application/json")
public interface ItemService {

    @POST
    void create (Item item);

    @GET
    List<Item> findAll ();

    @GET
    @Path("/{title}")
    Item find (@PathParam("title") String title);

    @DELETE
    @Path("/{title}")
    void delete (@PathParam("title") String title);

}
