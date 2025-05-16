
/**
 * Sendable.java
 * Superclass for Order, Category, FoodItem, and Modifier
 * Provides all subclasses a dataType attribute
 * dataType is used for identifying a Sendable object's true type from a JSON
 *                      string during deserialization (via CustomDeserializer)
 */

public class Sendable {
    private String dataType = "SENDABLE";

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
