import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class SeatSelectionPanel extends JPanel {
    private Map<String, JButton> seatButtons = new HashMap<>();
    private Set<String> selectedSeats = new HashSet<>();

    public SeatSelectionPanel() {
        setLayout(new GridLayout(5, 10, 10, 10)); // 5 baris, 10 kolom (A1-A10 sampai E10)

        for (char row = 'A'; row <= 'E'; row++) {
            for (int num = 1; num <= 10; num++) {
                String seatCode = row + String.valueOf(num);
                JButton btn = new JButton(seatCode);
                btn.setBackground(Color.LIGHT_GRAY);

                btn.addActionListener(e -> {
                    if (!btn.isEnabled()) return;
                    if (selectedSeats.contains(seatCode)) {
                        btn.setBackground(Color.LIGHT_GRAY);
                        selectedSeats.remove(seatCode);
                    } else {
                        btn.setBackground(Color.GREEN);
                        selectedSeats.add(seatCode);
                    }
                });

                seatButtons.put(seatCode, btn);
                add(btn);
            }
        }
    }

    public void refreshSeats(String filmTitle, String time, String date) {
        // Reset semua tombol
        for (JButton btn : seatButtons.values()) {
            btn.setEnabled(true);
            btn.setBackground(Color.LIGHT_GRAY);
        }

        // Ambil kursi yang sudah dibooking
        List<String> booked = DBHelper.getBookedSeats(filmTitle, time, date);

        for (String seat : booked) {
            JButton btn = seatButtons.get(seat);
            if (btn != null) {
                btn.setEnabled(false);
                btn.setBackground(Color.RED);
            }
        }

        selectedSeats.clear();
    }

    public List<String> getSelectedSeats(int count) {
        if (selectedSeats.size() != count) {
            JOptionPane.showMessageDialog(this, "Pilih tepat " + count + " kursi.");
            return null;
        }
        return new ArrayList<>(selectedSeats);
    }
}
