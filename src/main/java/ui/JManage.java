package ui;

import com.formdev.flatlaf.FlatLightLaf;
import dao.UserDAO;
import model.User;
import net.miginfocom.swing.MigLayout;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class JManage extends JPanel {
    private static final Color BG_COLOR     = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color TEXT_COLOR   = new Color(33, 33, 33);
    private static final Font  LABEL_FONT   = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font  BTN_FONT     = new Font("SansSerif", Font.BOLD, 13);

    private final UserDAO userDAO = new UserDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Usuário", "Perfil"}, 0
    );
    private final JTable table = new JTable(tableModel);

    private final JTextField       tfUsername = new JTextField();
    private final JPasswordField   pfPassword = new JPasswordField();
    private final JComboBox<String> cbRole     = new JComboBox<>(new String[]{"EMPLOYEE", "ADMIN"});

    private final JButton btnNew    = makeButton("Novo");
    private final JButton btnSave   = makeButton("Salvar");
    private final JButton btnEdit   = makeButton("Editar");
    private final JButton btnDelete = makeButton("Excluir");
    private final JButton btnClear  = makeButton("Limpar");

    public JManage() {
        FlatLightLaf.setup();

        setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        initUI();
        loadUserData();
        attachListeners();
    }

    private void initUI() {
        JPanel content = new JPanel(new MigLayout(
                "wrap 2, ins 10, gapx 10, gapy 8",
                "[right][grow, fill]",
                "[grow, fill]10[pref!]"
        ));
        content.setOpaque(false);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        content.add(new JScrollPane(table), "span 2, grow, push");

        content.add(createLabel("Usuário:"),    "align label");
        content.add(tfUsername,                 "growx, wrap");

        content.add(createLabel("Senha:"),      "align label");
        content.add(pfPassword,                 "growx, wrap");

        content.add(createLabel("Perfil:"),     "align label");
        content.add(cbRole,                     "growx, wrap");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClear);
        btnPanel.add(btnNew);
        btnPanel.add(btnSave);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        content.add(btnPanel, "span 2, align right");

        add(content, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_FONT);
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BTN_FONT);
        b.setBackground(ACCENT_COLOR);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userDAO.findAll();
            for (User u : users) {
                tableModel.addRow(new Object[]{
                        u.getId(), u.getUsername(), u.getRole()
                });
            }
        } catch (SQLException ex) {
            showError("Erro ao carregar usuários: " + ex.getMessage());
        }
    }

    private void attachListeners() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                tfUsername.setText((String) tableModel.getValueAt(r,1));
                cbRole.setSelectedItem((String) tableModel.getValueAt(r,2));
                pfPassword.setText("");
            }
        });

        btnClear.addActionListener(e -> clearForm());

        btnNew.addActionListener(e -> {
            clearForm();
            tfUsername.requestFocus();
        });

        btnSave.addActionListener(e -> {
            String user = tfUsername.getText().trim();
            char[] pw = pfPassword.getPassword();
            String role = (String) cbRole.getSelectedItem();
            if (user.isEmpty() || pw.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "Usuário e senha são obrigatórios.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String hash = BCrypt.hashpw(new String(pw), BCrypt.gensalt());
                userDAO.save(new User(null, user, hash, role));
                loadUserData();
                clearForm();
            } catch (SQLException ex) {
                showError("Erro ao salvar: " + ex.getMessage());
            }
        });

        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) return;
            Integer id = (Integer) tableModel.getValueAt(r,0);
            String user = tfUsername.getText().trim();
            char[] pw = pfPassword.getPassword();
            String role = (String) cbRole.getSelectedItem();
            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Usuário não pode ficar vazio.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                User u = new User(id, user, null, role);
                if (pw.length > 0) {
                    u.setPasswordHash(BCrypt.hashpw(new String(pw), BCrypt.gensalt()));
                }
                userDAO.update(u);
                loadUserData();
                clearForm();
            } catch (SQLException ex) {
                showError("Erro ao atualizar: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) return;
            if (JOptionPane.showConfirmDialog(this,
                    "Excluir usuário selecionado?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            Integer id = (Integer) tableModel.getValueAt(r,0);
            try {
                userDAO.delete(id);
                loadUserData();
                clearForm();
            } catch (SQLException ex) {
                showError("Erro ao excluir: " + ex.getMessage());
            }
        });
    }

    private void clearForm() {
        tfUsername.setText("");
        pfPassword.setText("");
        cbRole.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(
                this, msg, "Erro", JOptionPane.ERROR_MESSAGE
        );
    }
}
