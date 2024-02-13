import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class gui extends JFrame {
    private static final String MENU_FILE = "menu.txt";
    private static final String ORDER_FILE = "orders.txt";

    private Menu cafeMenu = new Menu();
    private Order currentOrder = new Order();
    private List<Order> orderHistory = new ArrayList<>();

    private JList<String> menuList;
    private DefaultListModel<String> menuListModel;
    private JList<String> orderList;
    private DefaultListModel<String> orderListModel;
    private JLabel totalLabel;

    public gui() {
        setTitle("Cafe Management System");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeMenu();
        createMenuPanel();
        createOrderPanel();
        createControlPanel();

        loadMenuFromFile();


        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);

    }

    private void initializeMenu() {
        menuListModel = new DefaultListModel<>();
        menuList = new JList<>(menuListModel);

        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
    }

    private void createMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));

        JScrollPane menuScrollPane = new JScrollPane(menuList);
        menuPanel.add(menuScrollPane, BorderLayout.CENTER);
        add(menuPanel, BorderLayout.WEST);
    }

    private void createOrderPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Order"));

        JScrollPane orderScrollPane = new JScrollPane(orderList);
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);

        totalLabel = new JLabel("Total: ₹0.0");
        orderPanel.add(totalLabel, BorderLayout.SOUTH);

        add(orderPanel, BorderLayout.CENTER);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control Panel"));

        JButton addItemButton = new JButton("Add Item to Order");
        addItemButton.addActionListener(e -> addItemToOrder());
        controlPanel.add(addItemButton);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 20)));

        JButton removeItemButton = new JButton("Remove Item from Order");
        removeItemButton.addActionListener(e -> removeItemFromOrder());
        controlPanel.add(removeItemButton);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 20)));

        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkout());
        controlPanel.add(checkoutButton);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 20)));


        JButton cancelOrderButton = new JButton("Cancel Order");
        cancelOrderButton.addActionListener(e -> cancelOrder());
        controlPanel.add(cancelOrderButton);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 20)));

        JButton viewOrderHistoryButton = new JButton("View Order History");
        viewOrderHistoryButton.addActionListener(e -> viewOrderHistory());
        controlPanel.add(viewOrderHistoryButton);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 20)));


        JButton addMenuItemButton = new JButton("Add Menu Item");
        addMenuItemButton.addActionListener(e -> addMenuItem());
        controlPanel.add(addMenuItemButton);

        controlPanel.add(Box.createRigidArea(new Dimension(5, 20)));

        add(controlPanel, BorderLayout.EAST);
    }

    private void addItemToOrder() {
        int selectedMenuItemIndex = menuList.getSelectedIndex();
        if (selectedMenuItemIndex != -1) {
            CafeItem selectedItem = cafeMenu.getMenuItems().get(selectedMenuItemIndex);
            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity > 0) {
                    OrderItem selectedOrderItem = new OrderItem(selectedItem.getName(),selectedItem.getPrice(),quantity);
                    currentOrder.addItem(selectedOrderItem);
                    updateOrderList();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid quantity.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            }
        }
    }

    private void removeItemFromOrder() {
        int selectedOrderItemIndex = orderList.getSelectedIndex();
        if (selectedOrderItemIndex != -1) {
            currentOrder.getItems().remove(selectedOrderItemIndex);
            updateOrderList();
        }
    }

    private void checkout() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please order something first.");
        } else {
            double total = currentOrder.calculateTotal();
            totalLabel.setText("Total: ₹" + total);

            saveOrderToFile();
            orderHistory.add(currentOrder);
            JOptionPane.showMessageDialog(this, "Order placed successfully!\nTotal amount to pay: ₹" + total);
            currentOrder = new Order();
            updateOrderList();
        }
    }

    private void cancelOrder() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please order something first.");
        } else {
            JOptionPane.showMessageDialog(this, "Your order is cancelled.");
            currentOrder = new Order();
            updateOrderList();
        }
    }

    private void addMenuItem() {
        String itemName = JOptionPane.showInputDialog(this, "Enter item name:");
        if (itemName != null && !itemName.isEmpty()) {
            String priceStr = JOptionPane.showInputDialog(this, "Enter item price:");
            try {
                double itemPrice = Double.parseDouble(priceStr);
                cafeMenu.addMenuItem(itemName, itemPrice);
                saveMenuToFile();
                updateMenuList();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price. Please enter a valid number.");
            }
        }
    }

    private void viewOrderHistory() {
        JFrame historyFrame = new JFrame("Order History");
        historyFrame.setSize(600, 400);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        DefaultListModel<String> historyListModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyListModel);
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyFrame.add(historyScrollPane);

        for (Order order : orderHistory) {
            String orderDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(order.getOrderDate());
            historyListModel.addElement(orderDate + " - Total: ₹" + order.calculateTotal());
        }

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);

        historyFrame.setVisible(true);
    }

    private void loadMenuFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MENU_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    double price = Double.parseDouble(parts[1].trim());
                    cafeMenu.addMenuItem(name, price);
                }
            }
            updateMenuList();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Menu file not found. Creating an empty menu.");
        }
    }

    private void saveMenuToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MENU_FILE))) {
            for (CafeItem item : cafeMenu.getMenuItems()) {
                writer.write(item.getName() + "," + item.getPrice());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveOrderToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_FILE, true))) {
            writer.write("Order Date: " + new Date());
            writer.newLine();
            writer.write("----------------------------------------\n");
            writer.write("Name \t\t    Price \t\t Quantity \n");
            writer.write("----------------------------------------\n");
            for (OrderItem item : currentOrder.getItems()) {
                writer.write(item.getName() + "\t\t" + item.getPrice() + "\t\t" + item.getQuantity());
                writer.newLine();
            }
            writer.write("----------------------------------------\n");
            writer.write("Total: ₹" + currentOrder.calculateTotal());
            writer.newLine();
            writer.write("----------------------------------------\n");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void updateMenuList() {
        menuListModel.clear();
        for (CafeItem item : cafeMenu.getMenuItems()) {
            menuListModel.addElement(item.getName() + " - ₹" + item.getPrice());
        }
    }

    private void updateOrderList() {
        orderListModel.clear();
        for (OrderItem item : currentOrder.getItems()) {
            orderListModel.addElement(item.getName() + " - ₹" + item.getPrice() + " - Quantity: "+ item.getQuantity());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gui app = new gui();
            app.setVisible(true);
        });
    }
}

class CafeItem {
    private String name;
    private double price;

    public CafeItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

class Menu {
    private List<CafeItem> menuItems;

    public Menu() {
        this.menuItems = new ArrayList<>();
    }

    public void addMenuItem(String name, double price) {
        CafeItem newItem = new CafeItem(name, price);
        menuItems.add(newItem);
    }

    public List<CafeItem> getMenuItems() {
        return menuItems;
    }
}

class OrderItem extends CafeItem{
    private int quantity;

    public OrderItem(String name, double price, int quantity) {
        super(name,price);
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}

class Order {
    private List<OrderItem> items;
    private Date orderDate;

    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = new Date();
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Date getOrderDate() {
        return orderDate;
    }
}
