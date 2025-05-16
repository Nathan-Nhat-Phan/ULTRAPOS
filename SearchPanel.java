
/** 
 * SearchPanel.java
 * Panel with components used for searching by food/filtering by date
 * Used in KitchenView and MgmtView
 */

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SearchPanel extends JPanel {
    private TableRowSorter<DefaultTableModel> sorter;
    private ArrayList<RowFilter<Object, Object>> appliedFiltersList;
    private RowFilter<Object, Object> comboFilter;
    private JSpinner creationDateSpinner;
    private JToggleButton creationDateFilterToggle;
    private JTextField searchBar;
    private ArrayList<Order> ordersList;

    private RowFilter<Object, Object> creationDateFilter;
    private RowFilter<Object, Object> searchFilter;
    private RowFilter<Object, Object> completionDateRangeFilter;

    private JSpinner completionStartDateSpinner;
    private JSpinner completionEndDateSpinner;
    private JToggleButton completionDateFilterToggle;
    private static final String EDITOR_DATE_FORMAT_PATTERN = "MMM dd, yyyy"; // New constant for editor
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(EDITOR_DATE_FORMAT_PATTERN); // Use the same pattern for consistency


    public SearchPanel(TableRowSorter<DefaultTableModel> sorter, RowFilter<Object, Object> comboFilter,
            ArrayList<RowFilter<Object, Object>> appliedFiltersList, ArrayList<Order> ordersList) {
        this.appliedFiltersList = appliedFiltersList;
        this.sorter = sorter;
        this.comboFilter = comboFilter;
        this.ordersList = ordersList;

        // Create a SpinnerDateModel for creation date
        Date initialCreationDate = new Date();
        SpinnerDateModel creationDateModel = new SpinnerDateModel(initialCreationDate, null, null, Calendar.DAY_OF_MONTH);
        creationDateSpinner = new JSpinner(creationDateModel);

        // Set the editor for the JSpinner to display the creation date
        JSpinner.DateEditor creationEditor = new JSpinner.DateEditor(creationDateSpinner, EDITOR_DATE_FORMAT_PATTERN);
        creationDateSpinner.setEditor(creationEditor);
        
        // Create a toggle button for creation date filtering
        creationDateFilterToggle = new JToggleButton("Filter by Creation Date");
        
        // --- Completion Date Range Filter ---
        // Start Date
        Date initialCompletionStartDate = new Date();
        SpinnerDateModel completionStartDateModel = new SpinnerDateModel(initialCompletionStartDate, null, null, Calendar.DAY_OF_MONTH);
        completionStartDateSpinner = new JSpinner(completionStartDateModel);
        JSpinner.DateEditor completionStartEditor = new JSpinner.DateEditor(completionStartDateSpinner, EDITOR_DATE_FORMAT_PATTERN);
        completionStartDateSpinner.setEditor(completionStartEditor);
        
        // End Date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1); // Default end date to tomorrow initially
        Date initialCompletionEndDate = cal.getTime();
        SpinnerDateModel completionEndDateModel = new SpinnerDateModel(initialCompletionEndDate, null, null, Calendar.DAY_OF_MONTH);
        completionEndDateSpinner = new JSpinner(completionEndDateModel);
        JSpinner.DateEditor completionEndEditor = new JSpinner.DateEditor(completionEndDateSpinner, EDITOR_DATE_FORMAT_PATTERN);
        completionEndDateSpinner.setEditor(completionEndEditor);
        
        // Set end date to be after start date initially
        // This line was triggering the listener before completionDateFilterToggle was initialized.
        // completionEndDateSpinner.setValue(new Date(completionStartDateSpinner.getValue().hashCode() + 24 * 60 * 60 * 1000));

        completionDateFilterToggle = new JToggleButton("Filter by Completion Date Range");

        // Create search bar with placeholder text
        searchBar = new JTextField(25);
        
        // Add listeners AFTER all components are initialized
        creationDateSpinner.addChangeListener(e -> updateCreationDateFilter());
        creationDateFilterToggle.addActionListener(e -> updateCreationDateFilter());
        completionStartDateSpinner.addChangeListener(e -> updateCompletionDateRangeFilter());
        completionEndDateSpinner.addChangeListener(e -> updateCompletionDateRangeFilter());
        completionDateFilterToggle.addActionListener(e -> updateCompletionDateRangeFilter());
        
        // Set end date to be after start date initially - moved after listeners are set up, though ideally this logic might be better placed elsewhere or handled more robustly.
        // For now, moving it here avoids the NPE during construction.
        completionEndDateSpinner.setValue(new Date(((Date)completionStartDateSpinner.getValue()).getTime() + 24 * 60 * 60 * 1000));
        searchBar.addActionListener(e -> updateSearch());

        // Placeholder text for the search bar
        if (searchBar.getText().isEmpty()) {
            searchBar.setText("Search for orders with a specific food item");
            searchBar.setForeground(Color.GRAY);
        }
        searchBar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchBar.getText().equals("Search for orders with a specific food item")) {
                    searchBar.setText("");
                    searchBar.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchBar.getText().isEmpty()) {
                    searchBar.setText("Search for orders with a specific food item");
                    searchBar.setForeground(Color.GRAY);
                }
            }
        });

        // Create labels for the search bar and date filter
        JLabel searchBarLabel = new JLabel("Search Orders:");
        JLabel creationDateFilterLabel = new JLabel("Creation Date Filter:");
        JLabel completionDateFilterLabel = new JLabel("Completion Date Filter:");


        // Add components to the panel with spacing
        add(searchBarLabel);
        add(searchBar);
        add(Box.createHorizontalStrut(20));
        add(creationDateFilterLabel);
        add(creationDateSpinner);
        add(creationDateFilterToggle);
        add(Box.createHorizontalStrut(20));
        add(completionDateFilterLabel);
        add(new JLabel("Start:"));
        add(completionStartDateSpinner);
        add(new JLabel("End:"));
        add(completionEndDateSpinner);
        add(completionDateFilterToggle);
    }

    public void updateCreationDateFilter() {
        if (creationDateFilterToggle.isSelected()) {
            appliedFiltersList.remove(creationDateFilter);
            // Apply the creation date filter (column 4)
            String selectedDateStr = creationDateSpinner.getValue().toString();
            // Convert JSpinner's date (which can be complex) to a simple "MMM d, yyyy" string for regex matching
            Date selectedDateObj = (Date) creationDateSpinner.getValue();
            String formattedSelectedDate = DATE_FORMAT.format(selectedDateObj);
            
            creationDateFilter = RowFilter.regexFilter(formattedSelectedDate, 4); // Column 4 for creation date
            appliedFiltersList.add(creationDateFilter);
        } else {
            // Remove the creation date filter
            appliedFiltersList.remove(creationDateFilter);
        }
        updateTable();
    }

    public void updateCompletionDateRangeFilter() {
        appliedFiltersList.remove(completionDateRangeFilter); // Remove previous filter if any

        if (completionDateFilterToggle.isSelected()) {
            Date tempStartDate = (Date) completionStartDateSpinner.getValue();
            Date tempEndDate = (Date) completionEndDateSpinner.getValue();

            // Ensure end date is after start date
            if (tempEndDate.before(tempStartDate)) {
                completionEndDateSpinner.setValue(tempStartDate); // Or show an error message
                tempEndDate = tempStartDate;
            }
            
            final Date normStartDate = normalizeDate(tempStartDate, false);
            final Date normEndDate = normalizeDate(tempEndDate, true);

            completionDateRangeFilter = new RowFilter<Object, Object>() {
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object completionDateValue = entry.getValue(5); // Column 5 for completion date
                    if (completionDateValue == null || completionDateValue.toString().isEmpty()) {
                        return false; // Don't include if completion date is not set
                    }
                    try {
                        Date completionDate = DATE_FORMAT.parse(completionDateValue.toString());
                        Date normCompletionDate = normalizeDate(completionDate, false);

                        return !normCompletionDate.before(normStartDate) && !normCompletionDate.after(normEndDate);
                    } catch (ParseException e) {
                        // Handle parsing error, e.g., log it or return false
                        System.err.println("Error parsing completion date in filter: " + completionDateValue);
                        return false;
                    }
                }
            };
            appliedFiltersList.add(completionDateRangeFilter);
        }
        updateTable();
    }

    private Date normalizeDate(Date date, boolean toEndOfDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (toEndOfDay) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }

    // searches Orders for ones that contain the inputted food item
    // creates a filter for each found Order's ID
    public ArrayList<RowFilter<Object, Object>> searchOrders(String searchItem) {
        appliedFiltersList.remove(searchFilter); // Remove previous search filter
        ArrayList<RowFilter<Object, Object>> filtersOfFound = new ArrayList<>();

        if (searchItem == null || searchItem.trim().isEmpty() || searchItem.equals("Search for orders with a specific food item")) {
            return filtersOfFound; // Return empty list if search is empty or placeholder
        }

        for (Order order : ordersList) {
            if (order.searchItem(searchItem) != -1) { // searchItem checks against food items in the order
                Integer id = order.getID();
                // ID is now in column 7
                filtersOfFound.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, id, 7));
            }
        }
        return filtersOfFound;
    }

    // updates the search filters and table filtering
    public void updateSearch() { // called whenever the search button is pressed
        String searchItem = searchBar.getText();
        if (searchItem.isBlank()) {
            clearSearch();
        } else {
            ArrayList<RowFilter<Object, Object>> filtersOfFound = searchOrders(searchItem);
            searchFilter = RowFilter.orFilter(filtersOfFound);
            appliedFiltersList.add(searchFilter);
            updateTable();
        }
    }

    public void clearSearch() {
        searchBar.setText("");
        appliedFiltersList.remove(searchFilter);
        updateTable();
    }

    // recombines filters and refilters the table
    public void updateTable() {
        // Combine all active filters
        comboFilter = RowFilter.andFilter(appliedFiltersList);

        // Apply the combined filter to the sorter
        sorter.setRowFilter(comboFilter);
    }
}
