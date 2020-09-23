import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Networking {
    /*
    Name: Connect
    Purpose: Connects to the server then does all of the communication
    Author: Samuel McManus
    Parameter User: The user's login credentials
    Uses: Cryptography.Decrypt
    Used By: Login
    Date: September 16, 2020
     */
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
                byte[] SmallSessionKey = Cryptography.SHAHash(SessionKey);
                //Asks the user to choose which algorithm to encrypt with
                String AlgorithmChoice = JOptionPane.showInputDialog("Choose 1 to encrypt using " +
                        "DES, 2 to encrypt using Blowfish, or 3 to encrypt using 3DES");
                //Sends the user's choice of algorithm to the server
                Output.writeObject(AlgorithmChoice);
                Output.flush();
                //Initialize a key for the chosen algorithm, obtain an instance of it,
                //use it to encrypt a file and send to the server, then use is to decrypt
                //a file sent from the server
                switch(AlgorithmChoice){
                    case "1":
                        SecretKey DESKey = Cryptography.DESKeyGen(SmallSessionKey);
                        Cipher des = Cipher.getInstance("DES/CBC/PKCS5Padding");
                        des.init(Cipher.ENCRYPT_MODE, DESKey);
                        SendFile(des, Output);
                        //Gets the IV and IV length
                        byte[] IV = (byte[]) Input.readObject();
                        //Creates the IV parameter spec and re-initializes the cipher
                        IvParameterSpec ivps = new IvParameterSpec(IV);
                        des.init(Cipher.DECRYPT_MODE, DESKey, ivps);
                        ReceiveFile(Input, des);
                    case "2":
                        SecretKeySpec BlowfishKeySpec = Cryptography.BlowfishKeyGen(SmallSessionKey);
                        Cipher Blowfish = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
                        Blowfish.init(Cipher.ENCRYPT_MODE, BlowfishKeySpec);
                        SendFile(Blowfish, Output);
                        IV = (byte[]) Input.readObject();
                        ivps = new IvParameterSpec(IV);
                        Blowfish.init(Cipher.DECRYPT_MODE, BlowfishKeySpec, ivps);
                        ReceiveFile(Input, Blowfish);
                    case "3":
                        SecretKey DESedeKey = Cryptography.DESedeKeyGen(SmallSessionKey);
                        Cipher DESede = Cipher.getInstance("DESede/CBC/PKCS5Padding");
                        DESede.init(Cipher.ENCRYPT_MODE, DESedeKey);
                        SendFile(DESede, Output);
                        IV = (byte[]) Input.readObject();
                        ivps = new IvParameterSpec(IV);
                        DESede.init(Cipher.DECRYPT_MODE, DESedeKey, ivps);
                        ReceiveFile(Input, DESede);
                }
            }
            Input.close();
            Output.close();
            Client.close();
        } catch (IOException | ClassNotFoundException | InvalidAlgorithmParameterException |
                NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }
    /*
    Name: SendFile
    Purpose: Send a file to the server
    Author: Samuel McManus
    Parameter MyCipher: The cipher used to encrypt the message
    Parameter Output: The output socket
    Uses: Cryptography.Encrypt
    Used By: Connect
    Date: September 22, 2020
     */
    public static void SendFile(Cipher MyCipher, ObjectOutputStream Output) throws
            IOException, IllegalBlockSizeException, BadPaddingException {
        //Read the plaintext of the file
        IO.ReadPlaintext();
        //Encrypt the file using the DES method
        Cryptography.Encrypt(MyCipher, Output);
    }
    /*
    Name: ReceiveFile
    Purpose: Receive a file from the server
    Author: Samuel McManus
    Parameter MyCipher: The cipher used to encrypt the message
    Parameter Input: The input socket used to communicate with the server
    Uses: Cryptography.Decrypt
    Used By: Connect
    Date: September 22, 2020
     */
    public static void ReceiveFile(ObjectInputStream Input, Cipher MyCipher) throws
            IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException {
        //Gets the cipher text from the server
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        while(true){
            byte[] TempCipher = (byte[]) Input.readObject();
            if(Arrays.equals(TempCipher, "finished".getBytes()))
                break;
            bo.writeBytes(TempCipher);
        }
        //Converts the cipher text to an array of bytes
        byte[] CipherText = bo.toByteArray();
        System.out.println("\nCiphertext received from Server:");
        System.out.println(new String(CipherText));
        Cryptography.Decrypt(MyCipher, CipherText);
    }
}