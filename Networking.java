import javax.crypto.*;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Networking {
    static void Connect(User user) {
        Socket Client;
        try {
            //Creates a socket connecting to the server on the loopback interface at port
            //6622
            Client = new Socket(InetAddress.getByName("127.0.0.1"), 6622);
            //Creates an output stream
            ObjectOutputStream Output = new ObjectOutputStream(Client.getOutputStream());
            Output.writeObject("\n");
            Output.flush();
            //Writes the user info to the server
            Output.writeObject(user.getUsername() + ", " + user.getPassword() + ", " +
                    user.getReturning() + "\n");
            Output.flush();
            //Creates an input stream
            ObjectInputStream Input = new ObjectInputStream(Client.getInputStream());
            //Reads the server's response to a JOptionPane
            String ServerResponse = (String) Input.readObject();
            JOptionPane.showMessageDialog(null, ServerResponse);
            //If the user's login credentials are valid then begin the diffie hellman section
            if (ServerResponse.equalsIgnoreCase("Success!")) {
                //Generate the session key
                byte[] SessionKey = Cryptography.DiffieHellman(Input, Output);
                //Hash the session key
                byte[] SmallSessionKey = Cryptography.SHA1Hash(SessionKey);
                //Gets the IV, IV length, and cipher text
                int IVLength = (int) Input.readObject();
                byte[] IV = (byte[]) Input.readObject();
                byte[] CipherText = (byte[]) Input.readObject();
                System.out.println(Base64.getEncoder().encodeToString(CipherText));
                FileOutputStream fileOutputStream = new FileOutputStream("ServerFile.des");
                fileOutputStream.write(CipherText);
                fileOutputStream.close();
                Cryptography.DESDecrypt(SmallSessionKey, IV, CipherText);
            }
            Input.close();
            Output.close();
            Client.close();
        } catch (IOException | ClassNotFoundException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }
}