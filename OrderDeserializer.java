
/** 
 * OrderDeserializer.java
 * Responsible for updating the highestOrderID upon deserialization
 */

import java.lang.reflect.Type;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class OrderDeserializer implements JsonDeserializer<Order> {
    @Override
    public Order deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeGsonTypeAdapter());
        Gson gson = gsonBuilder.create();
        Order order = gson.fromJson(json, Order.class);
        order.updateHighestOrderID();
        return order;
    }
}
