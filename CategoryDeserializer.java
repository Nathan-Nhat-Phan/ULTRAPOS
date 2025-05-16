
/** 
 * CategoryDeserializer.java
 * Responsible for updating the highestCategoryID upon deserialization
 */

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class CategoryDeserializer implements JsonDeserializer<Category> {
    @Override
    public Category deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(FoodItem.class, new FoodItemDeserializer());
        gsonBuilder.registerTypeAdapter(Modifier.class, new ModifierDeserializer());
        Gson gson = gsonBuilder.create();
        Category category = gson.fromJson(json, Category.class);
        category.updateHighestCategoryID();
        return category;
    }
}
