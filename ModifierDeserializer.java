
/** 
 * ModifierDeserializer.java
 * Responsible for updating the highestModifierID upon deserialization
 */

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ModifierDeserializer implements JsonDeserializer<Modifier> {
    @Override
    public Modifier deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        Modifier modifier = gson.fromJson(json, Modifier.class);
        modifier.updateHighestModifierID();
        return modifier;
    }
}
