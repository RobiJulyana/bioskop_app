import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class FilmCatalog extends JFrame {
    private JPanel catalogPanel;
    private JScrollPane scrollPane;
    private Timer autoReloadTimer;

    public FilmCatalog() {
        setTitle("Katalog Film");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLayout(new BorderLayout());

        // Panel atas
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton adminLoginBtn = new JButton("Login Admin");
        JButton reloadBtn = new JButton("Reload");

        topPanel.add(adminLoginBtn);
        topPanel.add(reloadBtn);
        add(topPanel, BorderLayout.NORTH);

        // Panel katalog
        catalogPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        scrollPane = new JScrollPane(catalogPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Action button
        adminLoginBtn.addActionListener(e -> new AdminLoginFrame());
        reloadBtn.addActionListener(e -> loadCatalog());

        // Muat katalog awal
        loadCatalog();

        // Timer: Auto-reload setiap 30 detik
        autoReloadTimer = new Timer(20000, e -> loadCatalog());
        autoReloadTimer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadCatalog() {
        catalogPanel.removeAll(); // Kosongkan panel

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, title, image_path, showtimes FROM films WHERE is_active = 1");

            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String imagePath = rs.getString("image_path");
                String showtimes = rs.getString("showtimes");

                try {
                    File imgFile = new File(imagePath);
                    if (!imgFile.exists()) {
                        throw new Exception("File tidak ditemukan: " + imagePath);
                    }

                    BufferedImage originalImage = ImageIO.read(imgFile);
                    Image scaledImage = originalImage.getScaledInstance(250, 350, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaledImage);

                    JPanel filmPanel = new JPanel(new BorderLayout());
                    filmPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    filmPanel.setBackground(Color.DARK_GRAY);

                    JButton btn = new JButton(title, icon);
                    btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btn.setHorizontalTextPosition(SwingConstants.CENTER);
                    btn.setFont(new Font("Arial", Font.BOLD, 16));
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(Color.BLACK);
                    btn.setOpaque(true);
                    btn.setBorderPainted(false);
                    btn.addActionListener(e -> {
                        autoReloadTimer.stop(); // stop timer sebelum pindah halaman
                        dispose();
                        new BookingPanel(id);
                    });

                    JLabel infoLabel = new JLabel("<html><center>ID: " + id + "<br>Jam Tayang :  " + showtimes + "</center></html>");
                    infoLabel.setForeground(Color.WHITE);
                    infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

                    filmPanel.add(btn, BorderLayout.CENTER);
                    filmPanel.add(infoLabel, BorderLayout.SOUTH);

                    catalogPanel.add(filmPanel);
                } catch (Exception imgEx) {
                    System.err.println("Gagal memuat gambar: " + imagePath);
                    imgEx.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        catalogPanel.revalidate(); // Refresh layout
        catalogPanel.repaint();
    }
}
