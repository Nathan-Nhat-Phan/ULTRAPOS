
/** 
 * GUI.java
 * Creates and manages the frame that holds the GUI
 */

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class GUI {
    private POS pos;

    private JFrame frame; // window that holds GUI components
    private Header header; // headerPanel to select views
    private JPanel body; // frame's body that displays selected View
    private CardLayout bodyCard; // used to show different Views in body

    /* Views shown in body */
    private RetailView retailView; // View for retail use
    private KitchenView kitchenView; // View for kitchen use
    private JScrollPane mgmtViewScroll; // Allows mgmtView to be scrollable
    private MgmtView mgmtView; // View for management use

    public GUI(POS pos) {
        this.pos = pos;

        frame = new JFrame();
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Point of Sale Interface");
        frame.setLayout(new BorderLayout());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pos.saveOrders();
            }
        });

        body = new JPanel();
        bodyCard = new CardLayout();
        body.setLayout(bodyCard);

        retailView = new RetailView(this);
        kitchenView = new KitchenView(this);
        mgmtView = new MgmtView(this);
        mgmtViewScroll = new JScrollPane(mgmtView);

        body.add(retailView, "retail");
        body.add(kitchenView, "kitchen");
        body.add(mgmtViewScroll, "mgmt");

        header = new Header(body, bodyCard);

        frame.add(header, BorderLayout.NORTH);
        frame.add(body, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    public POS getPOS() {
        return pos;
    }

    public KitchenView getKitchenView() {
        return kitchenView;
    }

    public MgmtView getMgmtView() {
        return mgmtView;
    }

    public RetailView getRetailView() {
        return retailView;
    }
}
