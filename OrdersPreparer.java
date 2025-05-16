
/** 
 * OrdersPreparer.java
 * Turns Orders into JTable rows (Object[])
 */

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class OrdersPreparer {
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a");

    public static Object[][] prepareOrders(ArrayList<Order> ordersList) // converts Orders to a row
    {
        Object[][] orderHistoryData = new Object[ordersList.size()][8]; // Updated to 8 columns
        for (int i = 0; i < ordersList.size(); i++) {
            Object[] orderRow = prepareOrder(ordersList.get(i));
            orderHistoryData[i] = orderRow;
        }
        return orderHistoryData;
    }

    public static Object[] prepareOrder(Order order) // converts Order to a row
    {
        Object[] orderRow = new Object[8]; // Updated to 8 columns
        orderRow[0] = order.getFood().toString().replaceAll("[\\[\\]]", "")
                .replaceAll(", ", "\n");
        orderRow[1] = order.getQuantity().toString().replaceAll("[\\[\\]]", "")
                .replaceAll(", ", "\n");
        orderRow[2] = order.getSpecial().toString().replaceAll("[\\[\\]]", "")
                .replaceAll(", ", "\n").replaceAll("; ", ", ");
        orderRow[3] = order.getStatus();
        orderRow[4] = order.getDateTime().format(dateTimeFormat);
        orderRow[5] = order.getCompletionDate() != null ? order.getCompletionDate().format(dateTimeFormat) : ""; // Completion Date
        orderRow[6] = order.getStudentID(); // Student ID
        orderRow[7] = order.getID(); // ID
        return orderRow;
    }
}
