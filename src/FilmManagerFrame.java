import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FilmManagerFrame extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private JLabel imagePreview;
    private String selectedImagePath = "";
    private JCheckBox activeCheckbox;
    private JTextField filmTitleField;
    private List<JCheckBox> showtimeCheckboxes = new ArrayList<>();

    private JButton addButton, editButton, deleteButton, clearButton, browseImageButton;

    private final String[] predefinedShowtimes = {"10:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};

    public FilmManagerFrame() {
        setTitle("Kelola Daftar Film");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Judul Film", "Gambar", "Aktif", "Jam Tayang"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        filmTitleField = new JTextField(20);
        imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(100, 150));
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        activeCheckbox = new JCheckBox("Film Aktif", true);

        JPanel showtimePanel = new JPanel(new GridLayout(0, 2));
        for (String time : predefinedShowtimes) {
            JCheckBox cb = new JCheckBox(time);
            showtimeCheckboxes.add(cb);
            showtimePanel.add(cb);
        }

        browseImageButton = new JButton("Pilih Gambar");
        browseImageButton.addActionListener(this::browseImage);

        addButton = new JButton("Tambah Film");
        addButton.addActionListener(e -> addFilm());

        editButton = new JButton("Edit Film");
        editButton.addActionListener(e -> updateFilm());

        deleteButton = new JButton("Hapus Film");
        deleteButton.addActionListener(e -> deleteFilm());

        clearButton = new JButton("Kosongkan Form");
        clearButton.addActionListener(e -> clearForm());

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.add(new JLabel("Judul Film:"));
        formPanel.add(filmTitleField);
        formPanel.add(browseImageButton);
        formPanel.add(imagePreview);
        formPanel.add(activeCheckbox);
        formPanel.add(new JLabel("Jam Tayang:"));
        formPanel.add(showtimePanel);
        formPanel.add(addButton);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(deleteButton);
        bottomPanel.add(editButton);
        bottomPanel.add(clearButton);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());

        loadFilms();
        setVisible(true);
    }

    private void browseImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            File targetDir = new File("assets/foto/");
            if (!targetDir.exists()) targetDir.mkdirs();
            File targetFile = new File(targetDir, fileName);

            try {
                Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                selectedImagePath = "assets/foto/" + fileName;
                ImageIcon icon = new ImageIcon(new ImageIcon(selectedImagePath).getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH));
                imagePreview.setIcon(icon);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar ke folder");
            }
        }
    }

    private void addFilm() {
        String title = filmTitleField.getText().trim();
        List<String> selectedShowtimes = getSelectedShowtimes();
        boolean isActive = activeCheckbox.isSelected();
        if (!title.isEmpty() && !selectedImagePath.isEmpty() && !selectedShowtimes.isEmpty()) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
                String nextId = generateNextFilmId(conn);
                String times = String.join(",", selectedShowtimes);
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO films (id, title, image_path, is_active, showtimes) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, nextId);
                stmt.setString(2, title);
                stmt.setString(3, selectedImagePath);
                stmt.setInt(4, isActive ? 1 : 0);
                stmt.setString(5, times);
                stmt.executeUpdate();
                clearForm();
                loadFilms();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lengkapi judul, gambar, dan jam tayang.");
        }
    }

    private List<String> getSelectedShowtimes() {
        List<String> times = new ArrayList<>();
        for (JCheckBox cb : showtimeCheckboxes) {
            if (cb.isSelected()) times.add(cb.getText());
        }
        return times;
    }

    private void setSelectedShowtimes(String[] selectedTimes) {
        for (JCheckBox cb : showtimeCheckboxes) {
            cb.setSelected(Arrays.asList(selectedTimes).contains(cb.getText()));
        }
    }

    private String generateNextFilmId(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM films ORDER BY id DESC LIMIT 1");
        String lastId = "Film00000";
        if (rs.next()) lastId = rs.getString("id");
        int numericPart = Integer.parseInt(lastId.replaceAll("\\D", ""));
        return String.format("Film%05d", numericPart + 1);
    }

    private void updateFilm() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film yang akan diedit.");
            return;
        }

        String id = model.getValueAt(row, 0).toString();
        String title = filmTitleField.getText().trim();
        boolean isActive = activeCheckbox.isSelected();
        List<String> selectedShowtimes = getSelectedShowtimes();

        if (title.isEmpty() || selectedShowtimes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul dan jam tayang tidak boleh kosong.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            String times = String.join(",", selectedShowtimes);
            PreparedStatement stmt = conn.prepareStatement("UPDATE films SET title = ?, image_path = ?, is_active = ?, showtimes = ? WHERE id = ?");
            stmt.setString(1, title);
            stmt.setString(2, selectedImagePath.isEmpty() ? model.getValueAt(row, 2).toString() : selectedImagePath);
            stmt.setInt(3, isActive ? 1 : 0);
            stmt.setString(4, times);
            stmt.setString(5, id);
            stmt.executeUpdate();
            clearForm();
            loadFilms();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteFilm() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String id = model.getValueAt(row, 0).toString();
            String imagePath = model.getValueAt(row, 2).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah kamu yakin ingin menghapus film ini beserta gambarnya?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
                    // Hapus data dari database
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM films WHERE id = ?");
                    stmt.setString(1, id);
                    stmt.executeUpdate();

                    // Hapus file gambar
                    File imgFile = new File(imagePath);
                    if (imgFile.exists() && imgFile.getCanonicalPath().startsWith(new File("assets/foto").getCanonicalPath())) {
                        if (!imgFile.delete()) {
                            System.err.println("Gagal menghapus file gambar: " + imagePath);
                        }
                    }

                    loadFilms();
                    clearForm();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void loadFilms() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, title, image_path, is_active, showtimes FROM films");
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String path = rs.getString("image_path");
                boolean active = rs.getInt("is_active") == 1;
                String showtimes = rs.getString("showtimes");
                model.addRow(new Object[]{id, title, path, active ? "Ya" : "Tidak", showtimes});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String title = model.getValueAt(row, 1).toString();
            String path = model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "";
            boolean active = model.getValueAt(row, 3).toString().equals("Ya");
            String[] selectedTimes = model.getValueAt(row, 4).toString().split(",");

            filmTitleField.setText(title);
            activeCheckbox.setSelected(active);
            selectedImagePath = path;

            File imgFile = new File(path);
            if (!path.isEmpty() && imgFile.exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH));
                imagePreview.setIcon(icon);
            } else {
                imagePreview.setIcon(null);
            }

            setSelectedShowtimes(selectedTimes);
        }
    }

    private void clearForm() {
        filmTitleField.setText("");
        selectedImagePath = "";
        activeCheckbox.setSelected(true);
        imagePreview.setIcon(null);
        table.clearSelection();
        for (JCheckBox cb : showtimeCheckboxes) {
            cb.setSelected(false);
        }
    }
}
