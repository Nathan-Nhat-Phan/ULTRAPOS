
/** 
 * CustomDeserializer.java
 * Deserializer for Sendable objects
 * Determines/returns the correct object type from JSON data
 */

import java.lang.reflect.Type;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CustomDeserializer implements JsonDeserializer<Sendable> {
    @Override
    public Sendable deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeGsonTypeAdapter());
        gsonBuilder.registerTypeAdapter(Category.class, new CategoryDeserializer());
        gsonBuilder.registerTypeAdapter(FoodItem.class, new FoodItemDeserializer());
        gsonBuilder.registerTypeAdapter(Modifier.class, new ModifierDeserializer());
        Gson gson = gsonBuilder.create();

        JsonObject jsonObject = (JsonObject) json;

        System.out.println(jsonObject.get("dataType").getAsString());
        if (jsonObject.get("dataType").getAsString().equals(Category.DATATYPE)) {
            System.out.println("category parsed");
            return gson.fromJson(json, Category.class);
        }
        if (jsonObject.get("dataType").getAsString().equals(FoodItem.DATATYPE)) {
            System.out.println("food item parsed");
            return gson.fromJson(json, FoodItem.class);
        }
        if (jsonObject.get("dataType").getAsString().equals(Modifier.DATATYPE)) {
            System.out.println("modifier parsed");
            return gson.fromJson(json, Modifier.class);
        }
        if (jsonObject.get("dataType").getAsString().equals(Order.DATATYPE)) {
            System.out.println("order parsed");
            return gson.fromJson(json, Order.class);
        }
        System.out.println("Non-Specific Sendable");
        System.out.println(json);
        return gson.fromJson(json, Sendable.class);
    }
}
