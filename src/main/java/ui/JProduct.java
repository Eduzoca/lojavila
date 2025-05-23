package ui;

import com.formdev.flatlaf.FlatLightLaf;
import dao.ProductDAO;
import model.Product;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class JProduct extends JPanel {
    private static final Color BG_COLOR     = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Font  LABEL_FONT   = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font  BTN_FONT     = new Font("SansSerif", Font.BOLD, 13);

    private final ProductDAO dao = new ProductDAO();
    private final NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("pt", "BR"));

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Preço", "Quantidade"}, 0
    );
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField tfName   = new JTextField();
    private final JTextField tfPrice  = new JTextField();
    private final JSpinner spQuantity = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

    private final JButton btnNew    = makeButton("Novo");
    private final JButton btnSave   = makeButton("Salvar");
    private final JButton btnEdit   = makeButton("Editar");
    private final JButton btnDelete = makeButton("Excluir");
    private final JButton btnClear  = makeButton("Limpar");

    private final JLabel statusBar = new JLabel("Pronto");

    public JProduct() {
        FlatLightLaf.setup();
        fmt.setGroupingUsed(true);

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        initSearch();
        initTable();
        initForm();
        initStatus();
        loadTableData();
        attachListeners();
    }

    private void initSearch() {
        JPanel north = new JPanel(new BorderLayout(5,0));
        north.setOpaque(false);
        JLabel lbl = new JLabel("Buscar:");
        lbl.setFont(LABEL_FONT);
        north.add(lbl, BorderLayout.WEST);

        JTextField tfSearch = new JTextField();
        tfSearch.getDocument().addDocumentListener(new DocumentListener(){
            void f(){
                String t = tfSearch.getText().trim();
                sorter.setRowFilter(
                        t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t, 1)
                );
            }
            public void insertUpdate(DocumentEvent e){ f(); }
            public void removeUpdate(DocumentEvent e){ f(); }
            public void changedUpdate(DocumentEvent e){}
        });
        north.add(tfSearch, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
    }

    private void initTable() {
        table.setFont(LABEL_FONT);
        table.setRowHeight(24);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initForm() {
        JPanel f = new JPanel(new MigLayout("insets 0, gap 8",
                "[right][grow,fill][pref!]", "[]8[]8[]8[]"));
        f.setOpaque(false);
        f.add(new JLabel("Nome:"),       "cell 0 0");
        f.add(tfName,                    "cell 1 0 2 1");
        f.add(new JLabel("Preço:"),      "cell 0 1");
        f.add(tfPrice,                   "cell 1 1 2 1");
        f.add(new JLabel("Quantidade:"),"cell 0 2");
        spQuantity.setFont(LABEL_FONT);
        f.add(spQuantity,                "cell 1 2");
        f.add(btnClear,  "cell 1 3, split 5");
        f.add(btnNew);
        f.add(btnSave);
        f.add(btnEdit);
        f.add(btnDelete);
        add(f, BorderLayout.EAST);
    }

    private void initStatus() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(statusBar);
        add(south, BorderLayout.SOUTH);
    }

    private JButton makeButton(String txt) {
        JButton b = new JButton(txt);
        b.setFont(BTN_FONT);
        b.setBackground(ACCENT_COLOR);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    public void loadTableData() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            List<Product> list = dao.findAll();
            for (Product p : list) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        fmt.format(p.getPrice()),
                        p.getQuantity()
                });
            }
            statusBar.setText("Carregados " + list.size() + " produtos");
        });
    }

    private void attachListeners() {
        table.getSelectionModel().addListSelectionListener(e->{
            if (!e.getValueIsAdjusting() && table.getSelectedRow()>=0) {
                int r = table.convertRowIndexToModel(table.getSelectedRow());
                tfName.setText((String)tableModel.getValueAt(r,1));
                tfPrice.setText((String)tableModel.getValueAt(r,2));
                spQuantity.setValue(tableModel.getValueAt(r,3));
            }
        });

        btnNew.addActionListener(e-> clearForm());

        btnSave.addActionListener(e-> {
            try {
                String name = tfName.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Nome vazio");

                for (Product existing : dao.findAll()) {
                    if (existing.getName().equalsIgnoreCase(name)) {
                        throw new IllegalArgumentException("Produto '" + name + "' já existe.");
                    }
                }

                String raw = tfPrice.getText().trim()
                        .replace(".", "")
                        .replace(",", ".");
                double price = Double.parseDouble(raw);
                int qty = (int) spQuantity.getValue();

                Product p = new Product(null, name, price, qty);
                dao.save(p);
                loadTableData();
                clearForm();
                statusBar.setText("Criado ID=" + p.getId());
            } catch (NumberFormatException ex) {
                showError("Preço inválido");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                showError("Erro: " + ex.getMessage());
            }
        });

        btnEdit.addActionListener(e-> {
            int sel = table.getSelectedRow();
            if (sel < 0) return;
            try {
                int r = table.convertRowIndexToModel(sel);
                Integer id = (Integer) tableModel.getValueAt(r, 0);
                String name = tfName.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Nome vazio");

                for (Product existing : dao.findAll()) {
                    if (!existing.getId().equals(id)
                            && existing.getName().equalsIgnoreCase(name)) {
                        throw new IllegalArgumentException("Produto '" + name + "' já existe.");
                    }
                }

                String raw = tfPrice.getText().trim()
                        .replace(".", "")
                        .replace(",", ".");
                double price = Double.parseDouble(raw);
                int qty = (int) spQuantity.getValue();

                Product p = new Product(id, name, price, qty);
                dao.update(p);
                loadTableData();
                clearForm();
                statusBar.setText("Atualizado ID=" + id);
            } catch (NumberFormatException ex) {
                showError("Preço inválido");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                showError("Erro: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e-> {
            int sel = table.getSelectedRow();
            if (sel < 0) return;
            if (JOptionPane.showConfirmDialog(this,
                    "Excluir produto?", "Confirma",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            int r = table.convertRowIndexToModel(sel);
            Integer id = (Integer) tableModel.getValueAt(r, 0);
            try {
                dao.delete(id);
                loadTableData();
                clearForm();
                statusBar.setText("Excluído ID=" + id);
                JMain.refreshDashboard();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Não permitido",
                        JOptionPane.WARNING_MESSAGE
                );
            } catch (Exception ex) {
                showError("Erro: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e-> clearForm());
    }

    private void clearForm() {
        table.clearSelection();
        tfName.setText("");
        tfPrice.setText("");
        spQuantity.setValue(0);
        statusBar.setText("Pronto");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
        statusBar.setText("Erro");
    }
}
