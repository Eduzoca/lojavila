package ui;

import com.formdev.flatlaf.FlatLightLaf;
import dao.UserDAO;
import model.User;
import net.miginfocom.swing.MigLayout;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class JLogin extends JFrame {
    private static final Color BG_COLOR     = new Color(250, 250, 250);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color TEXT_COLOR   = new Color(33, 33, 33);
    private static final Color PLACEHOLDER  = new Color(150, 150, 150);
    private static final int   FRAME_RADIUS = 16;
    private static final int   TITLE_HEIGHT = 30;
    private static final int   FIELD_HEIGHT = 40;
    private static final int   BUTTON_HEIGHT= 42;
    private static final int   PANEL_WIDTH  = 260;

    private final ImageIcon userIcon, passIcon, icoMin, icoClose;
    private final HintTextField     loginField = new HintTextField("Usuário");
    private final HintPasswordField pwdField   = new HintPasswordField("Senha");
    private final JButton           loginBtn   = new RoundButton("Entrar");
    private final JLabel            lblMsg     = new JLabel(" ", SwingConstants.CENTER);

    public JLogin() {
        FlatLightLaf.setup();

        userIcon = loadIcon("/icons/user.png", FIELD_HEIGHT, FIELD_HEIGHT);
        passIcon = loadIcon("/icons/lock.png", FIELD_HEIGHT, FIELD_HEIGHT);
        icoMin   = loadIcon("/icons/minimize.png", 16, 16);
        icoClose = loadIcon("/icons/close.png", 16, 16);

        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_COLOR);
        titleBar.setPreferredSize(new Dimension(PANEL_WIDTH, TITLE_HEIGHT));
        enableDrag(this, titleBar);

        JLabel lblTitle = new JLabel("  Acesso");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 14f));
        lblTitle.setForeground(TEXT_COLOR);
        titleBar.add(lblTitle, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        btns.setOpaque(false);
        btns.add(createIconButton(icoMin, e-> setState(Frame.ICONIFIED)));
        btns.add(createIconButton(icoClose, e-> dispose()));
        titleBar.add(btns, BorderLayout.EAST);

        BottomRoundedPanel content = new BottomRoundedPanel(FRAME_RADIUS, BG_COLOR);
        content.setBorder(new EmptyBorder(40, 30, 40, 30));
        content.setLayout(new MigLayout(
                "wrap 1, width " + PANEL_WIDTH + ", gapy 16", "[grow, fill]"
        ));
        enableDrag(this, content);

        JLabel welcome = new JLabel("Bem-vindo", SwingConstants.CENTER);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 24f));
        welcome.setForeground(TEXT_COLOR);
        content.add(welcome, "spanx, align center, wrap");

        lblMsg.setFont(lblMsg.getFont().deriveFont(12f));
        lblMsg.setForeground(Color.RED);
        content.add(lblMsg, "spanx, align center, wrap");

        content.add(createFieldPanel(userIcon, loginField), "h " + FIELD_HEIGHT + "!, spanx");
        content.add(createFieldPanel(passIcon, pwdField),   "h " + FIELD_HEIGHT + "!, spanx");

        loginBtn.setPreferredSize(new Dimension(0, BUTTON_HEIGHT));
        content.add(loginBtn, "h " + BUTTON_HEIGHT + "!, spanx");

        getRootPane().setDefaultButton(loginBtn);
        loginBtn.addActionListener(e -> doLogin());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titleBar, BorderLayout.NORTH);
        getContentPane().add(content, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        lblMsg.setText(" ");
        String u = loginField.getText().trim();
        String p = new String(pwdField.getPassword());
        if (u.isEmpty()||p.isEmpty()) {
            lblMsg.setText("Preencha usuário e senha.");
            return;
        }
        try {
            UserDAO dao = new UserDAO();
            User usr = dao.findByUsername(u);
            if (usr!=null && BCrypt.checkpw(p, usr.getPasswordHash())) {
                dispose();
                SwingUtilities.invokeLater(()-> new JMain(usr).setVisible(true));
            } else {
                lblMsg.setText("Usuário ou senha incorretos.");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("Erro de conexão.");
        }
    }

    private JPanel createFieldPanel(Icon icon, JTextComponent field) {
        JPanel p = new JPanel(new BorderLayout(8,0));
        p.setOpaque(false);
        p.add(new JLabel(icon), BorderLayout.WEST);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setBorder(BorderFactory.createMatteBorder(0,0,2,0,PLACEHOLDER));
        p.add(field, BorderLayout.CENTER);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createMatteBorder(0,0,2,0,ACCENT_COLOR));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createMatteBorder(0,0,2,0,PLACEHOLDER));
            }
        });
        return p;
    }

    private JButton createIconButton(Icon ico, ActionListener act) {
        JButton b = new JButton(ico);
        b.setPreferredSize(new Dimension(28,28));
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(act);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e){
                b.setOpaque(true); b.setBackground(new Color(0,0,0,20));
            }
            @Override public void mouseExited(MouseEvent e){
                b.setOpaque(false);
            }
        });
        return b;
    }

    private void enableDrag(JFrame frame, JComponent area) {
        MouseAdapter ma = new MouseAdapter() {
            Point origin;
            @Override public void mousePressed(MouseEvent e){
                origin = e.getPoint();
            }
            @Override public void mouseDragged(MouseEvent e){
                Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX()-origin.x,
                        p.y + e.getY()-origin.y);
            }
        };
        area.addMouseListener(ma);
        area.addMouseMotionListener(ma);
    }

    private ImageIcon loadIcon(String path,int w,int h){
        Image img = new ImageIcon(getClass().getResource(path))
                .getImage()
                .getScaledInstance(w,h,Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private static class BottomRoundedPanel extends JPanel {
        private final int    radius;
        private final Color  bg;
        BottomRoundedPanel(int radius, Color bg){
            this.radius = radius;
            this.bg     = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g){
            int w = getWidth(), h = getHeight(), r = radius;
            Path2D path = new Path2D.Double();
            path.moveTo(0,0);
            path.lineTo(w,0);
            path.lineTo(w,h-r);
            path.quadTo(w,h,w-r,h);
            path.lineTo(r,h);
            path.quadTo(0,h,0,h-r);
            path.closePath();
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(path);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundButton extends JButton {
        RoundButton(String text){
            super(text);
            setFont(getFont().deriveFont(Font.BOLD,14f));
            setForeground(Color.white);
            setBackground(ACCENT_COLOR);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){
                    setBackground(ACCENT_COLOR.darker());
                }
                @Override public void mouseExited(MouseEvent e){
                    setBackground(ACCENT_COLOR);
                }
            });
        }
        @Override public Dimension getPreferredSize(){
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, BUTTON_HEIGHT);
        }
    }

    private static class HintTextField extends JTextField {
        private final String hint;
        HintTextField(String hint){
            this.hint = hint;
            setFont(getFont().deriveFont(14f));
        }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(getText().isEmpty() && !isFocusOwner()){
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(PLACEHOLDER);
                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.drawString(hint, ins.left, y);
                g2.dispose();
            }
        }
    }

    private static class HintPasswordField extends JPasswordField {
        private final String hint;
        HintPasswordField(String hint){
            this.hint = hint;
            setFont(getFont().deriveFont(14f));
        }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(getPassword().length==0 && !isFocusOwner()){
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(PLACEHOLDER);
                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.drawString(hint, ins.left, y);
                g2.dispose();
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            JLogin frame = new JLogin();
            frame.setVisible(true);
        });
    }
}
