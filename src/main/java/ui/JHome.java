// src/ui/JHome.java
package ui;

import dao.ProductDAO;
import dao.SaleDAO;
import model.ProductSales;
import model.UserSales;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JHome extends JPanel {
    private static final Color BG_COLOR   = new Color(245,245,245);
    private static final Color TEXT_COLOR = new Color(33,33,33);
    private static final Color ACCENT1    = new Color(66,133,244);
    private static final Color ACCENT2    = new Color(244,180,0);
    private static final Color ACCENT3    = new Color(15,157,88);
    private static final Color ACCENT4    = new Color(219,68,55);
    private static final Font  CARD_VAL   = new Font("SansSerif",Font.BOLD,24);
    private static final Font  CARD_LBL   = new Font("SansSerif",Font.PLAIN,12);

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO    saleDAO    = new SaleDAO();
    private final NumberFormat fmt      = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));

    public JHome() {
        setBackground(BG_COLOR);
        setLayout(new MigLayout(
                "wrap 2, ins 10, gap 10",
                "[grow][grow]",
                "[60!][60!][10][200!][10][200!]"
        ));
        rebuildDashboard();
    }

    public void reloadData() {
        removeAll();
        rebuildDashboard();
        revalidate();
        repaint();
    }

    private void rebuildDashboard() {
        int totalProd = 0;
        double valorEstoque = 0, receitaTotal = 0, vendaHoje = 0, mediaDiaria = 0;

        try {
            // dados dos produtos
            var prods = productDAO.findAll();
            totalProd = prods.size();
            valorEstoque = prods.stream()
                    .mapToDouble(p -> p.getPrice() * p.getQuantity())
                    .sum();

            // receita total por usuário
            List<UserSales> byUser = saleDAO.findSalesByUser();
            receitaTotal = byUser.stream()
                    .mapToDouble(UserSales::getTotalSold)
                    .sum();

            // vendas por dia últimos 7 dias
            Map<LocalDate, Double> daily7 = saleDAO.findSalesByDay(7);
            LocalDate today = LocalDate.now();
            vendaHoje   = daily7.getOrDefault(today, 0.0);
            mediaDiaria = daily7.values().stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao recarregar dashboard: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        // cards
        add(new DashCard("Total Produtos", String.valueOf(totalProd), ACCENT1), "growx");
        add(new DashCard("Valor Estoque",  fmt.format(valorEstoque),       ACCENT2), "growx");
        add(new DashCard("Receita Total",  fmt.format(receitaTotal),       ACCENT3), "growx");
        add(new DashCard("Vendas Hoje",    fmt.format(vendaHoje),          ACCENT4), "growx");
        add(new DashCard("Média 7 dias",   fmt.format(mediaDiaria),        ACCENT1),
                "spanx, growx");

        // gráfico vendas 7 dias
        DefaultCategoryDataset ds7 = new DefaultCategoryDataset();
        try {
            Map<LocalDate, Double> daily7 = saleDAO.findSalesByDay(7);
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                double v = daily7.getOrDefault(d, 0.0);
                String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
                ds7.addValue(v, "Vendas", label);
            }
        } catch (SQLException ignored) { }
        JFreeChart chart7 = ChartFactory.createLineChart(
                "Vendas Últimos 7 Dias",
                "",
                "R$",
                ds7
        );
        add(new ChartPanel(chart7), "spanx, grow, h 200!");

        // gráfico top 5 produtos
        DefaultCategoryDataset dsTop = new DefaultCategoryDataset();
        try {
            List<ProductSales> top5 = saleDAO.findTopSelling(5);
            for (ProductSales ps : top5) {
                dsTop.addValue(ps.getTotalQuantity(), "Quantidade", ps.getProductName());
            }
        } catch (SQLException ignored) { }
        JFreeChart chartTop = ChartFactory.createBarChart(
                "Top 5 Produtos",
                "",
                "Unidades",
                dsTop
        );
        add(new ChartPanel(chartTop), "spanx, growx, h 200!");

    }

    private static class DashCard extends JPanel {
        DashCard(String label, String value, Color color) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(color, 2));
            setPreferredSize(new Dimension(0, 60));
            JLabel v = new JLabel(value, SwingConstants.CENTER);
            v.setFont(CARD_VAL);
            v.setForeground(color);
            add(v, BorderLayout.CENTER);
            JLabel l = new JLabel(label, SwingConstants.CENTER);
            l.setFont(CARD_LBL);
            l.setForeground(TEXT_COLOR);
            add(l, BorderLayout.SOUTH);
        }
    }
}
