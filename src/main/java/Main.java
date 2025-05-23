import com.formdev.flatlaf.FlatLightLaf;
import ui.JLogin;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            Graphics2D g = splash.createGraphics();
            if (g != null) {
                g.setFont(new Font("Dialog", Font.PLAIN, 12));
                g.setColor(Color.WHITE);
                String msg = "Carregando módulos...";
                int x = 20;
                int y = splash.getSize().height - 20;
                g.drawString(msg, x, y);
                splash.update();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SwingUtilities.invokeLater(() -> {
            new JLogin().setVisible(true);
        });
    }

}
