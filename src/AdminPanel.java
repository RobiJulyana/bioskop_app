import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminPanel extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public AdminPanel() {
        setTitle("Panel Admin - Data Pemesanan");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Nama", "Film", "Jam", "Kursi"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Tombol hapus
        JButton deleteBtn = new JButton("Hapus Pesanan Terpilih");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = model.getValueAt(row, 0).toString(); // Ambil ID as String
                DBHelper.deleteBooking1(id); // Panggil dengan string, bukan integer
                model.removeRow(row);
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
            }
        });

        // Tombol Kelola Film
        JButton manageFilmsBtn = new JButton("Kelola Daftar Film");
        manageFilmsBtn.addActionListener(e -> {
            new FilmManagerFrame(); // Buka jendela baru untuk kelola film
        });

        // Tombol Kelola Studio
        JButton kelolaStudioBtn = new JButton("Kelola Studio");
        kelolaStudioBtn.addActionListener(e -> {
            new StudioManagerFrame(); // Buka jendela baru untuk kelola film
        });

        // Panel tombol bawah
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(deleteBtn);
        bottomPanel.add(manageFilmsBtn);
        bottomPanel.add(kelolaStudioBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadBookings();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadBookings() {
        List<String[]> bookings = DBHelper.getAllBookings();
        for (String[] row : bookings) {
            model.addRow(row);
        }
    }
}
