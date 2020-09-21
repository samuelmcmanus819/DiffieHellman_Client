import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FrontEnd {
    /*
    Name: Login
    Purpose: Displays the login screen to the user
    Author: Samuel McManus
    Uses: TestValidity, Connect
    Used By: Main
    Date: September 14, 2020
     */
    static void Login(User user){
        //Creates two text boxes with widths of 20
        JTextField UsernameBox = new JTextField(20);
        JTextField PasswordBox = new JTextField(20);
        //Creates a designated login button used for returning users
        JButton LoginButton = new JButton("Log In");
        //Creates a designated register button for new users
        JButton RegisterButton = new JButton("Register");

        //Creates a new JPanel with text fields for username and password
        JPanel LoginPanel = new JPanel();
        LoginPanel.add(new JLabel("Username:"));
        LoginPanel.add(UsernameBox);
        LoginPanel.add(new JLabel("Password:"));
        LoginPanel.add(PasswordBox);
        //Adds buttons to the panel for login and registration
        LoginPanel.add(LoginButton);
        LoginPanel.add(RegisterButton);

        //Creates a frame with the title log in
        final JFrame LoginFrame = new JFrame("Log In");
        //The frame exits the program on close and contains the panel made above
        LoginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        LoginFrame.add(LoginPanel);
        LoginFrame.setSize(300, 150);
        LoginFrame.setVisible(true);

        LoginButton.addActionListener(new ActionListener() {
            @Override
            //If the user pressed the login button, then take their username and
            //password and register them as a returning user.
            public void actionPerformed(ActionEvent e) {
                user.setUsername(UsernameBox.getText());
                user.setPassword(PasswordBox.getText());
                user.setNewOrOld("Old");
                //Test the validity of the username and password. If they're good, then
                //connect the user to the server.
                if(TestValidity(user.getUsername()) && TestValidity(user.getPassword()))
                    Networking.Connect(user);
                else
                    JOptionPane.showMessageDialog(null,
                            "Your user name cannot contain, ', '");
            }
        });
        RegisterButton.addActionListener(new ActionListener() {
            @Override
            //If the user pressed the register button, take their
            //username and password and register them as a new user.
            public void actionPerformed(ActionEvent e) {
                user.setUsername(UsernameBox.getText());
                user.setPassword(PasswordBox.getText());
                user.setNewOrOld("New");
                //Test the validity of the username and password. If they're good, then
                //connect the user to the server.
                if(TestValidity(user.getUsername()) && TestValidity(user.getPassword()))
                    Networking.Connect(user);
                else
                    JOptionPane.showMessageDialog(null,
                            "Your user name cannot contain, ', '");
            }
        });
    }
    /*
    Name: TestValidity
    Purpose: Tests whether the user input is valid
    Author: Samuel McManus
    Uses: N/A
    Used By: TestValidity
    Date: September 15, 2020
     */
    static boolean TestValidity(String Input){
        //The comma space is used to separate different outputs, and may not be used in
        //usernames or passwords
        if(Input.contains(", ")){
            return false;
        }
        return true;
    }
}
