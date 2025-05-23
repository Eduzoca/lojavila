package ui;

import com.formdev.flatlaf.FlatLightLaf;
import dao.ProductDAO;
import dao.SaleDAO;
import model.Product;
import model.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class JSale extends JPanel {
    private static final Color BG_COLOR     = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Font  LABEL_FONT   = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font  BTN_FONT     = new Font("SansSerif", Font.BOLD, 13);

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO    saleDAO    = new SaleDAO();
    private final User       currentUser;

    private final DefaultTableModel saleModel = new DefaultTableModel(
            new Object[]{"Produto","Qtd","Preço Unit.","Subtotal"}, 0
    );
    private final JTable tblSale = new JTable(saleModel);

    private final JComboBox<Product> cbProduct = new JComboBox<>();
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JButton btnAdd    = makeButton("Adicionar");
    private final JButton btnRemove = makeButton("Remover");
    private final JButton btnFinish = makeButton("Finalizar");
    private final JButton btnCancel = makeButton("Cancelar");
    private final JLabel lblTotal  = new JLabel("Total: R$ 0,00");

    private double total = 0;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));
    private final JProduct jProduct;

    public JSale(User currentUser, JProduct jProduct) {
        this.currentUser = currentUser;
        this.jProduct = jProduct;
        FlatLightLaf.setup();

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        initProductSelector();
        initTable();
        initControls();
        loadProducts();
        attachListeners();
    }

    private void initProductSelector() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);

        JLabel l1 = new JLabel("Produto:");
        l1.setFont(LABEL_FONT);
        top.add(l1);

        cbProduct.setPreferredSize(new Dimension(220,25));
        cbProduct.setFont(LABEL_FONT);
        cbProduct.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                loadProducts();
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        cbProduct.addActionListener(e -> updateSpinnerModel());
        cbProduct.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value instanceof Product) {
                    Product p = (Product)value;
                    setText(String.format("%s (R$ %s) — estoque: %d",
                            p.getName(),
                            fmt.format(p.getPrice()),
                            p.getQuantity()));
                }
                return this;
            }
        });
        top.add(cbProduct);

        JLabel l2 = new JLabel("Quantidade:");
        l2.setFont(LABEL_FONT);
        top.add(l2);

        spQty.setPreferredSize(new Dimension(60,25));
        top.add(spQty);

        btnAdd.setFont(BTN_FONT);
        top.add(btnAdd);

        add(top,BorderLayout.NORTH);
    }

    private void updateSpinnerModel() {
        Product p = (Product)cbProduct.getSelectedItem();
        if (p != null) {
            int max = p.getQuantity().intValue();
            spQty.setModel(new SpinnerNumberModel(1, 1, max, 1));
        }
    }

    private void initTable() {
        tblSale.setRowHeight(24);
        tblSale.setAutoCreateRowSorter(true);
        add(new JScrollPane(tblSale),BorderLayout.CENTER);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD,16f));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private void initControls() {
        JPanel bottom = new JPanel(new MigLayout("insets 0, gap 8","[grow][][][]","[]8[]"));
        bottom.setOpaque(false);
        bottom.add(btnRemove, "cell 0 0");
        bottom.add(btnFinish, "cell 1 0");
        bottom.add(btnCancel, "cell 2 0, wrap");
        bottom.add(lblTotal,   "cell 0 1 4 1, growx, align right");
        add(bottom,BorderLayout.SOUTH);
    }

    private void loadProducts() {
        List<Product> list = productDAO.findAll();
        DefaultComboBoxModel<Product> model = new DefaultComboBoxModel<>();
        list.forEach(model::addElement);
        cbProduct.setModel(model);
        updateSpinnerModel();
    }

    private void attachListeners() {
        btnAdd.addActionListener(e -> {
            Product p = (Product)cbProduct.getSelectedItem();
            int qty = (int)spQty.getValue();
            if (p==null || qty<=0) return;

            int inCart=0;
            for (int i=0;i<saleModel.getRowCount();i++){
                if (saleModel.getValueAt(i,0).equals(p.getName()))
                    inCart += (int)saleModel.getValueAt(i,1);
            }
            if (inCart+qty>p.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "Estoque insuficiente: só há "+p.getQuantity(),
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double unit=p.getPrice(), sub=unit*qty;
            for (int i=0;i<saleModel.getRowCount();i++){
                if (saleModel.getValueAt(i,0).equals(p.getName())){
                    int old=(int)saleModel.getValueAt(i,1),
                            neo=old+qty;
                    saleModel.setValueAt(neo,i,1);
                    saleModel.setValueAt(fmt.format(neo*unit),i,3);
                    total+=sub;
                    updateTotal();
                    return;
                }
            }
            saleModel.addRow(new Object[]{
                    p.getName(),qty,fmt.format(unit),fmt.format(sub)
            });
            total+=sub;
            updateTotal();
        });

        btnRemove.addActionListener(e->{
            int r=tblSale.getSelectedRow(); if(r<0)return;
            int m=tblSale.convertRowIndexToModel(r);
            try{ total-=fmt.parse((String)saleModel.getValueAt(m,3)).doubleValue(); }
            catch(Exception ignored){}
            saleModel.removeRow(m);
            updateTotal();
        });

        btnFinish.addActionListener(e->{
            if(saleModel.getRowCount()==0){
                JOptionPane.showMessageDialog(this,
                        "Nenhum item adicionado.","Aviso",JOptionPane.WARNING_MESSAGE);
                return;
            }
            try{
                for(int i=0;i<saleModel.getRowCount();i++){
                    String name=(String)saleModel.getValueAt(i,0);
                    int qty=(int)saleModel.getValueAt(i,1);
                    Product p=productDAO.findByName(name);
                    saleDAO.save(p.getId(), currentUser.getId(), qty, p.getPrice());
                    p.setQuantity(p.getQuantity()-qty);
                    productDAO.update(p);
                }
                JOptionPane.showMessageDialog(this,
                        "Venda finalizada: " + fmt.format(total),
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                clearAll();
                loadProducts();
                if (jProduct != null) {
                    jProduct.loadTableData();
                }
            } catch(Exception ex){
                JOptionPane.showMessageDialog(this,
                        "Erro: "+ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e-> clearAll());
    }

    private void updateTotal() {
        lblTotal.setText("Total: "+fmt.format(total));
    }

    private void clearAll() {
        saleModel.setRowCount(0);
        total=0;
        updateTotal();
    }

    private JButton makeButton(String t) {
        JButton b=new JButton(t);
        b.setFont(BTN_FONT);
        b.setBackground(ACCENT_COLOR);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }
}
