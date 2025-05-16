
/** 
 * DateTimeGsonTypeAdapter.java
 * Deserializer for LocalDateTime objects
 */

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class DateTimeGsonTypeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a");

    @Override
    public void write(JsonWriter writer, LocalDateTime value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value.format(dateTimeFormat));
    }

    @Override
    public LocalDateTime read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.parse(reader.nextString(), dateTimeFormat);
        return dateTime;
    }

}
