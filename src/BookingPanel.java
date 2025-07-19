import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class BookingPanel extends JFrame {
    private JTextField filmField;
    private JTextField nameField;
    private JComboBox<String> timeBox;
    private JSpinner seatCountSpinner;
    private SeatSelectionPanel seatPanel;
    private String selectedFilmId;
    private String selectedFilmTitle;
    private JTextField dateField;

    public BookingPanel(String filmId) {
        this.selectedFilmId = filmId;

        // Ambil title berdasarkan ID
        selectedFilmTitle = getFilmTitleById(filmId);

        setTitle("Pemesanan Tiket - " + selectedFilmTitle);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 10, 50));

        // 1. Judul Film (readonly)
        formPanel.add(new JLabel("Judul Film:"));
        filmField = new JTextField(selectedFilmTitle);
        filmField.setEditable(false);
        formPanel.add(filmField);

        // 2. Nama Pemesan
        formPanel.add(new JLabel("Nama Pemesan:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        // 3. Tanggal Hari Ini (readonly)
        formPanel.add(new JLabel("Tanggal:"));
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dateField.setEditable(false);
        formPanel.add(dateField);

        // 4. Jam Tayang
        formPanel.add(new JLabel("Jam Tayang:"));
        timeBox = new JComboBox<>();
        loadShowtimesFromDatabase(filmId); // ambil dari DB berdasarkan ID
        formPanel.add(timeBox);

        // 5. Jumlah Kursi
        formPanel.add(new JLabel("Jumlah Kursi:"));
        seatCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        formPanel.add(seatCountSpinner);

        add(formPanel, BorderLayout.NORTH);

        // Panel kursi
        seatPanel = new SeatSelectionPanel();
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);

        // Saat pertama kali tampil
        if (timeBox.getItemCount() > 0) {
            seatPanel.refreshSeats(selectedFilmTitle, (String) timeBox.getSelectedItem(), dateField.getText());
        }

        // Saat combobox jam tayang diubah
        timeBox.addActionListener(e -> {
            String selectedTime = (String) timeBox.getSelectedItem();
            seatPanel.refreshSeats(selectedFilmTitle, selectedTime, dateField.getText());
        });


        // Panel tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        JButton submitButton = new JButton("Simpan & Cetak Tiket");
        JButton backButton = new JButton("Kembali");

        submitButton.setPreferredSize(new Dimension(200, 40));
        backButton.setPreferredSize(new Dimension(200, 40));

        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String time = (String) timeBox.getSelectedItem();
            String date = dateField.getText();
            List<String> selectedSeats = seatPanel.getSelectedSeats((int) seatCountSpinner.getValue());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama pemesan harus diisi!");
                return;
            }

            if (selectedSeats == null || selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih kursi terlebih dahulu!");
                return;
            }

            DBHelper.saveBooking(name, selectedFilmTitle, date, time, selectedSeats);
            dispose();
            new PrintTicketPanel(name, selectedFilmTitle, date, time, selectedSeats);
        });

        backButton.addActionListener(e -> {
            dispose();
            new FilmCatalog();
        });

        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Ambil showtimes berdasarkan ID film
    private void loadShowtimesFromDatabase(String filmId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT showtimes FROM films WHERE id = ?");
            stmt.setString(1, filmId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String[] times = rs.getString("showtimes").split(",");
                for (String time : times) {
                    timeBox.addItem(time.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ambil judul berdasarkan ID film
    private String getFilmTitleById(String filmId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT title FROM films WHERE id = ?");
            stmt.setString(1, filmId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Film";
    }
}
