// src/ui/JReport.java
package ui;

import dao.ProductDAO;
import dao.SaleDAO;
import model.Product;
import model.UserSales;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class JReport extends JPanel {
    private static final Color BG_COLOR     = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color TEXT_COLOR   = new Color(33, 33, 33);
    private static final Font  TITLE_FONT   = new Font("SansSerif", Font.BOLD, 16);
    private static final Font  BTN_FONT     = new Font("SansSerif", Font.BOLD, 13);
    private static final Font  TABLE_FONT   = new Font("SansSerif", Font.PLAIN, 13);

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO    saleDAO    = new SaleDAO();
    private final NumberFormat fmt      =
            NumberFormat.getCurrencyInstance(new Locale("pt","BR"));

    private final DefaultTableModel prodModel = new DefaultTableModel(
            new Object[]{"ID","Nome","Preço","Quantidade"}, 0
    );
    private final JTable prodTable = new JTable(prodModel);

    private final JLabel lblProdCount      = new JLabel();
    private final JLabel lblProdTotalValue = new JLabel();
    private final JButton btnProdRefresh   = makeButton("Atualizar");
    private final JButton btnProdExport    = makeButton("Exportar CSV");

    private final DefaultTableModel userModel = new DefaultTableModel(
            new Object[]{"Usuário","Total Vendido"},0
    );
    private final JTable userTable = new JTable(userModel);
    private final JButton btnUserRefresh = makeButton("Atualizar");

    public JReport() {
        setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        initUI();
        loadProductData();
        loadUserSales();
        attachListeners();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(TITLE_FONT);

        JPanel pProd = new JPanel(new MigLayout("wrap 1, ins 10","[grow]","[pref][grow][pref]"));
        pProd.setBackground(BG_COLOR);

        JPanel prodBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        prodBar.setOpaque(false);
        prodBar.add(btnProdRefresh);
        prodBar.add(btnProdExport);
        pProd.add(prodBar, "growx");

        prodTable.setFont(TABLE_FONT);
        prodTable.setRowHeight(24);
        prodTable.getTableHeader().setFont(TABLE_FONT.deriveFont(Font.BOLD));
        pProd.add(new JScrollPane(prodTable), "grow, push");

        JPanel prodSummary = new JPanel(new GridLayout(1,2,10,0));
        prodSummary.setOpaque(false);
        prodSummary.add(lblProdCount);
        prodSummary.add(lblProdTotalValue);
        pProd.add(prodSummary, "growx");

        JPanel pUser = new JPanel(new MigLayout("wrap 1, ins 10","[grow]","[pref][grow]"));
        pUser.setBackground(BG_COLOR);

        JPanel userBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        userBar.setOpaque(false);
        userBar.add(btnUserRefresh);
        pUser.add(userBar, "growx");

        userTable.setFont(TABLE_FONT);
        userTable.setRowHeight(24);
        userTable.getTableHeader().setFont(TABLE_FONT.deriveFont(Font.BOLD));
        pUser.add(new JScrollPane(userTable), "grow, push");

        tabs.addTab("Produtos", pProd);
        tabs.addTab("Vendas por Usuário", pUser);

        add(tabs, BorderLayout.CENTER);
    }

    private void loadProductData() {
        prodModel.setRowCount(0);
        try {
            List<Product> list = productDAO.findAll();
            double totalValue = 0;
            for (Product p : list) {
                prodModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        fmt.format(p.getPrice()),
                        p.getQuantity()
                });
                totalValue += p.getPrice() * p.getQuantity();
            }
            lblProdCount.setText("Total de produtos: " + list.size());
            lblProdTotalValue.setText("Valor em estoque: " + fmt.format(totalValue));
        } catch (Exception ex) {
            showError("Erro ao carregar produtos: " + ex.getMessage());
        }
    }

    private void loadUserSales() {
        userModel.setRowCount(0);
        try {
            List<UserSales> sales = saleDAO.findSalesByUser();
            for (UserSales us : sales) {
                userModel.addRow(new Object[]{
                        us.getUsername(),
                        fmt.format(us.getTotalSold())
                });
            }
        } catch (SQLException ex) {
            showError("Erro ao carregar vendas: " + ex.getMessage());
        }
    }

    private void attachListeners() {
        btnProdRefresh.addActionListener(e -> loadProductData());
        btnProdExport.addActionListener(e -> exportProductCsv());
        btnUserRefresh.addActionListener(e -> loadUserSales());
    }

    private void exportProductCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar CSV de produtos");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path file = chooser.getSelectedFile().toPath();
        try (FileWriter w = new FileWriter(file.toFile());
             var csv = new org.apache.commons.csv.CSVPrinter(
                     w,
                     org.apache.commons.csv.CSVFormat.DEFAULT
                             .withHeader("ID","Nome","Preço","Quantidade")
             )) {
            for (int i = 0; i < prodModel.getRowCount(); i++) {
                csv.printRecord(
                        prodModel.getValueAt(i,0),
                        prodModel.getValueAt(i,1),
                        prodModel.getValueAt(i,2),
                        prodModel.getValueAt(i,3)
                );
            }
            csv.flush();
            JOptionPane.showMessageDialog(
                    this,
                    "Exportado com sucesso para:\n" + file,
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            showError("Falha ao exportar CSV: " + ex.getMessage());
        }
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BTN_FONT);
        b.setBackground(ACCENT_COLOR);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
