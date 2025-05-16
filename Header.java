
/** 
 * Header.java
 * Panel with toggleable buttons to select Views
 */

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import java.awt.CardLayout;
import java.awt.GridLayout;

public class Header extends JPanel {
    private JPanel body; //JFrame's body panel used to display Views
    private CardLayout bodyCard; // used to show different Views in body
    private ButtonGroup viewButtonsGroup; // ensures only one button can be pressed at a time
    private JToggleButton retailButton; // toggleable button to show RetailVIew
    private JToggleButton kitchenButton; // toggleable button to show KitchenView
    private JToggleButton mgmtButton; // toggleable button to show MgmtView

    public Header(JPanel body, CardLayout bodyCard) {
        this.body = body;
        this.bodyCard = bodyCard;
        setLayout(new GridLayout(1, 3, 10, 0));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                "View Select", TitledBorder.CENTER, TitledBorder.BELOW_TOP));

        viewButtonsGroup = new ButtonGroup();

        add(Box.createHorizontalGlue());

        // Set Up Retail View Button
        retailButton = new JToggleButton("Retail");
        retailButton.addActionListener(e -> bodyCard.show(body, "retail"));
        retailButton.setFocusable(false);
        viewButtonsGroup.add(retailButton);
        add(retailButton);

        // Set Up Kitchen View Button
        kitchenButton = new JToggleButton("Kitchen");
        kitchenButton.addActionListener(e -> bodyCard.show(body, "kitchen"));
        kitchenButton.setFocusable(false);
        viewButtonsGroup.add(kitchenButton);
        add(kitchenButton);

        // Set Up Management View Button
        mgmtButton = new JToggleButton("Management");
        mgmtButton.addActionListener(e -> bodyCard.show(body, "mgmt"));
        mgmtButton.setFocusable(false);
        viewButtonsGroup.add(mgmtButton);
        add(mgmtButton);

        add(Box.createHorizontalGlue());

        viewButtonsGroup.setSelected(retailButton.getModel(), true);
        bodyCard.show(body, "Retail");
    }
}