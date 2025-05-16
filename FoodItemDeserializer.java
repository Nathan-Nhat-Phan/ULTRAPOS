
/** 
 * FoodItemDeserializer.java
 * Responsible for updating the highestFoodItemID upon deserialization
 */

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class FoodItemDeserializer implements JsonDeserializer<FoodItem> {
    @Override
    public FoodItem deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Modifier.class, new ModifierDeserializer());
        Gson gson = gsonBuilder.create();
        FoodItem foodItem = gson.fromJson(json, FoodItem.class);
        foodItem.updateHighestFoodItemID();
        return foodItem;
    }
}
