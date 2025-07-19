import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StudioManagerFrame extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private JTextField studioNameField;
    private JButton addButton, deleteButton;

    public StudioManagerFrame() {
        setTitle("Kelola Studio");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Nama Studio"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new FlowLayout());
        studioNameField = new JTextField(20);
        addButton = new JButton("Tambah");
        deleteButton = new JButton("Hapus");

        formPanel.add(new JLabel("Nama Studio:"));
        formPanel.add(studioNameField);
        formPanel.add(addButton);
        formPanel.add(deleteButton);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> tambahStudio());
        deleteButton.addActionListener(e -> hapusStudio());

        loadStudio();
        setVisible(true);
    }

    private void tambahStudio() {
        String name = studioNameField.getText().trim();
        if (!name.isEmpty()) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO studios (name) VALUES (?)");
                stmt.setString(1, name);
                stmt.executeUpdate();
                studioNameField.setText("");
                loadStudio();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Studio sudah ada atau gagal ditambahkan.");
                e.printStackTrace();
            }
        }
    }

    private void hapusStudio() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String name = model.getValueAt(row, 0).toString();
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM studios WHERE name = ?");
                stmt.setString(1, name);
                stmt.executeUpdate();
                loadStudio();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadStudio() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM studios");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("name")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
