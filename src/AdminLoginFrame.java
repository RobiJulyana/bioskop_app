import javax.swing.*;

public class AdminLoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLoginFrame() {
        setTitle("Login Admin");
        setSize(300, 180);
        setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        usernameField = new JTextField();
        usernameField.setBounds(110, 20, 150, 25);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 60, 80, 25);
        passwordField = new JPasswordField();
        passwordField.setBounds(110, 60, 150, 25);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(100, 100, 100, 30);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (DBHelper.loginAdmin(username, password)) {
                JOptionPane.showMessageDialog(this, "Login berhasil!");
                dispose();
                new AdminPanel();  // tampilkan panel admin
            } else {
                JOptionPane.showMessageDialog(this, "Login gagal! Username atau Password salah.");
            }
        });

        add(userLabel); add(usernameField);
        add(passLabel); add(passwordField);
        add(loginButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
