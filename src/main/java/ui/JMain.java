package ui;

import com.formdev.flatlaf.FlatLightLaf;
import model.User;

import javax.swing.*;
import java.awt.*;

public class JMain extends JFrame {
    private static final Color BG_COLOR   = new Color(245,245,245);
    private static final Color NAV_BG     = new Color(33,33,33);
    private static final Color NAV_ACTIVE = new Color(66,133,244);
    private static final Color NAV_HOVER  = new Color(55,55,55);

    private static final Font NAV_FONT = new Font("SansSerif",Font.PLAIN,14);

    private static JMain instance;

    private final User       currentUser;
    private final JPanel     cardsPanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JHome      homePanel;
    private final JProduct   productPanel;

    public JMain(User user) {
        instance = this;
        this.currentUser = user;
        FlatLightLaf.setup();

        setTitle("Loja Vila");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);

        homePanel     = new JHome();
        productPanel  = new JProduct();
        JSale salesPanel   = new JSale(currentUser, productPanel);
        JReport reportPanel = new JReport();
        JManage managePanel = "ADMIN".equalsIgnoreCase(currentUser.getRole())
                ? new JManage()
                : null;

        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(BG_COLOR);
        cardsPanel.add(homePanel,       "HOME");
        cardsPanel.add(productPanel,    "PRODUCTS");
        cardsPanel.add(salesPanel,      "SALES");
        cardsPanel.add(reportPanel,     "REPORTS");
        if (managePanel != null) {
            cardsPanel.add(managePanel, "USERS");
        }

        initUI();
        showCard("HOME");
        setVisible(true);
    }

    private void initUI() {
        JPanel nav = new JPanel();
        nav.setBackground(NAV_BG);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(180, getHeight()));

        JButton btnHome    = makeNav("Home",      () -> showCard("HOME"));
        JButton btnProd    = makeNav("Produtos",  () -> showCard("PRODUCTS"));
        JButton btnSales   = makeNav("Vendas",    () -> showCard("SALES"));
        JButton btnReport  = makeNav("Relatórios",() -> showCard("REPORTS"));
        nav.add(btnHome);
        nav.add(btnProd);
        nav.add(btnSales);
        nav.add(btnReport);

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JButton btnUsers = makeNav("Usuários", () -> showCard("USERS"));
            nav.add(btnUsers);
        }

        nav.add(Box.createVerticalGlue());
        JButton btnLogout = makeNav("Logout", this::doLogout);
        nav.add(btnLogout);

        setActiveNav(btnHome);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(nav,        BorderLayout.WEST);
        getContentPane().add(cardsPanel, BorderLayout.CENTER);
    }

    private JButton makeNav(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(NAV_FONT);
        b.setForeground(Color.WHITE);
        b.setBackground(NAV_BG);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setBorder(BorderFactory.createEmptyBorder(5,15,5,5));
        b.setFocusPainted(false);

        b.addActionListener(e -> {
            setActiveNav(b);
            action.run();
            if ("HOME".equals(currentKey())) {
                homePanel.reloadData();
            }
        });
        b.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseEntered(java.awt.event.MouseEvent e){
                if (b != activeNav) b.setBackground(NAV_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e){
                if (b != activeNav) b.setBackground(NAV_BG);
            }
        });
        return b;
    }

    private JButton activeNav;
    private void setActiveNav(JButton b) {
        if (activeNav != null) activeNav.setBackground(NAV_BG);
        activeNav = b;
        activeNav.setBackground(NAV_ACTIVE);
    }

    private void showCard(String key) {
        cardLayout.show(cardsPanel, key);
    }

    private String currentKey() {
        for (Component c : cardsPanel.getComponents()) {
            if (c.isVisible()) {
                if (c == homePanel)      return "HOME";
                if (c == productPanel)   return "PRODUCTS";
                if (c instanceof JSale)   return "SALES";
                if (c instanceof JReport) return "REPORTS";
                if (c instanceof JManage) return "USERS";
            }
        }
        return "";
    }

    public static void refreshDashboard() {
        if (instance != null) instance.homePanel.reloadData();
    }

    private void doLogout() {
        dispose();
        SwingUtilities.invokeLater(() -> new JLogin().setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new JMain(new User(null,"admin",null,"ADMIN"))
        );
    }
}
