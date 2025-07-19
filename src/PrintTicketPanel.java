import java.awt.*;
import java.awt.print.*;
import java.util.List;
import javax.swing.*;

public class PrintTicketPanel extends JFrame implements Printable {

    private String name, film, date, time;
    private List<String> seats;

    public PrintTicketPanel(String name, String film, String date, String time, List<String> seats) {
        this.name = name;
        this.film = film;
        this.date = date;
        this.time = time;
        this.seats = seats;

        setTitle("Tiket Bioskop");
        setSize(600, 400);
        setLocationRelativeTo(null); // Tengah layar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Warna latar
        Color bgColor = new Color(255, 250, 240); // beige lembut
        Color borderColor = new Color(200, 200, 200);

        // Panel utama
        JPanel ticketPanel = new JPanel();
        ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));
        ticketPanel.setBackground(bgColor);
        ticketPanel.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        ticketPanel.setPreferredSize(new Dimension(500, 250));
        ticketPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Judul
        JLabel title = new JLabel("== TIKET BIOSKOP ==", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(60, 60, 60));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        ticketPanel.add(title);

        // Info tiket
        Font infoFont = new Font("Courier New", Font.PLAIN, 16);
        ticketPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        ticketPanel.add(createInfoLabel("Nama", name, infoFont));
        ticketPanel.add(createInfoLabel("Film", film, infoFont));
        ticketPanel.add(createInfoLabel("Tanggal", date, infoFont));
        ticketPanel.add(createInfoLabel("Jam", time, infoFont));
        ticketPanel.add(createInfoLabel("Kursi", String.join(", ", seats), infoFont));
        ticketPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Panel tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(bgColor);

        JButton backButton = new JButton("Kembali ke Katalog");
        JButton cancelButton = new JButton("Batalkan Pesanan");
        JButton printButton = new JButton("Cetak Tiket");

        printButton.addActionListener(e -> printTicket());

        backButton.addActionListener(e -> {
            dispose();
            new FilmCatalog();
        });

        cancelButton.addActionListener(e -> {
            DBHelper.deleteBooking(name, film, time);
            JOptionPane.showMessageDialog(this, "Pesanan dibatalkan.");
            dispose();
            new FilmCatalog();
        });

        buttonPanel.add(printButton);
        buttonPanel.add(backButton);
        buttonPanel.add(cancelButton);

        // Atur ke frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(bgColor);
        getContentPane().add(ticketPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private JPanel createInfoLabel(String label, String value, Font font) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(255, 250, 240));

        JLabel labelComp = new JLabel(label + ": ");
        labelComp.setFont(font);
        labelComp.setPreferredSize(new Dimension(100, 25));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(font);

        panel.add(labelComp);
        panel.add(valueComp);

        return panel;
    }

    private void printTicket() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Tiket Bioskop");
        printerJob.setPrintable(this);

        boolean doPrint = printerJob.printDialog();
        if (doPrint) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mencetak tiket: " + ex.getMessage());
            }
        }
    }

     @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int centerX = (int) (pf.getImageableWidth() / 2);
        int centerY = (int) (pf.getImageableHeight() / 2);

        int ticketWidth = 400;
        int ticketHeight = 220;

        int startX = centerX - ticketWidth / 2;
        int startY = centerY - ticketHeight / 2;

        // Border tiket
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(startX, startY, ticketWidth, ticketHeight, 20, 20);

        // Judul tiket
        Font titleFont = new Font("SansSerif", Font.BOLD, 16);
        Font infoFont = new Font("Monospaced", Font.PLAIN, 12);
        g2d.setFont(titleFont);
        g2d.drawString("  TIKET BIOSKOP  ", startX + 100, startY + 30);

        // Garis pemisah
        g2d.drawLine(startX + 20, startY + 40, startX + ticketWidth - 20, startY + 40);

        // Info pemesanan
        g2d.setFont(infoFont);
        int y = startY + 65;
        g2d.drawString("Nama    : " + name, startX + 30, y); y += 20;
        g2d.drawString("Film    : " + film, startX + 30, y); y += 20;
        g2d.drawString("Tanggal : " + date, startX + 30, y); y += 20;
        g2d.drawString("Jam     : " + time, startX + 30, y); y += 20;
        g2d.drawString("Kursi   : " + String.join(", ", seats), startX + 30, y); y += 30;

        // Garis putus-putus horizontal
        for (int i = startX + 20; i < startX + ticketWidth - 20; i += 10) {
            g2d.drawLine(i, y, i + 5, y);
        }

        y += 20;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2d.drawString("Tiket ini hanya berlaku untuk 1 kali penayangan.", startX + 30, y);
        y += 15;
        g2d.drawString("Silakan tiba 15 menit sebelum film dimulai.", startX + 30, y);
        y += 15;
        g2d.drawString("Terima kasih telah menggunakan CinemaApp.", startX + 30, y);

        return PAGE_EXISTS;
    }
}