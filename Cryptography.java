import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Cryptography {
    /*
    Name: GenerateKeys
    Purpose: Generates a set of keys used to create a symmetric key pair with
             Diffie-Hellman
    Author: Doctor Burris
    Return: The public-private key-pair used for Diffie-Hellman
    Uses: N/A
    Used By: DiffieHellman
    Date: September 15, 2020
     */
    public static KeyPair GenerateKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator ServerKPG = KeyPairGenerator.getInstance("DH");
        ServerKPG.initialize(SKIP.sDHParameterSpec);
        return ServerKPG.genKeyPair();
    }
    /*
    Name: DiffieHellman
    Purpose: Hash a byte array using the sha-1 algorithm
    Author: Doctor Burris and Samuel McManus
    Parameter DHBytes: The session key received from the diffie-hellman algorithm
    Return: A hash of the session key
    Uses: N/A
    Used By: Listen
    Date: September 18, 2020
     */
    public static byte[] SHA1Hash(byte[] DHBytes) throws NoSuchAlgorithmException {
        //Creates an instance of the sha-1 message digest
        MessageDigest Sha = MessageDigest.getInstance("SHA");
        //Feeds the input to the Sha algorithm and returns the digest
        Sha.update(DHBytes);
        return Sha.digest();
    }
    /*
    Name: DiffieHellman
    Purpose: Create a session key for encryption
    Author: Doctor Burris
    Parameter Input: The server's input
    Parameter Output: Output to the server
    Return: The session key
    Uses: GenerateKeys
    Used By: Listen
    Date: September 16, 2020
     */
    public static byte[] DiffieHellman(ObjectInputStream Input, ObjectOutputStream Output) throws
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException,
            ClassNotFoundException, InvalidKeySpecException, InvalidKeyException {
        //Generate the keys
        KeyPair DHKeys = Cryptography.GenerateKeys();
        //Send the client's public key to the server as a base 64 encoded string
        Output.writeObject(Base64.getEncoder().encodeToString(DHKeys.getPublic().getEncoded())
                + "\n");
        Output.flush();
        //Takes in the server's public key
        byte[] ServerPublicBytes =
                Base64.getDecoder().decode((((String)Input.readObject()).trim()));
        //Create a Diffie-Hellman key factory
        KeyFactory factory = KeyFactory.getInstance("DH");
        //Create an x509 key spec using the byte array of the client's public key
        X509EncodedKeySpec x509Spec =
                new X509EncodedKeySpec(ServerPublicBytes);
        //Generate a public key from the factory using the x509 key specification
        PublicKey ServerPublicKey = factory.generatePublic(x509Spec);
        //Generate the secret session key
        KeyAgreement SecretKeyAgreement = KeyAgreement.getInstance("DH");
        SecretKeyAgreement.init(DHKeys.getPrivate());
        SecretKeyAgreement.doPhase(ServerPublicKey, true);
        return SecretKeyAgreement.generateSecret();
    }
    /*
    Name: DESKeyGen
    Purpose: Generate a DES key using the session key as the password
    Author: Doctor Burris
    Parameter Parameter: A hash of the session key received from the diffie-hellman algorithm
    Return: The secret DES key
    Uses: N/A
    Used By: DESEncrypt
    Date: September 18, 2020
     */
    public static SecretKey DESKeyGen(byte[] Password) throws InvalidKeyException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        //Create a key specification from the password
        DESKeySpec desKeySpec = new DESKeySpec(Password);
        //Get an instance of the DES algorithm and return the secret key made from it
        SecretKeyFactory KeyFactory = SecretKeyFactory.getInstance("DES");
        return KeyFactory.generateSecret(desKeySpec);
    }
    public static void DESDecrypt(byte[] SessionBytes, byte[] IV, byte[] CipherText) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
            IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        //Initializes a file input stream
        FileInputStream fin = new FileInputStream("ServerFile.des");
        StringBuilder Plaintext = new StringBuilder();
        //Gets a DES secret key
        SecretKey DESKey = DESKeyGen(SessionBytes);
        IvParameterSpec ivps = new IvParameterSpec(IV);
        //Gets an instance of the cipher class using the DES algorithm  in encrypt mode
        Cipher des = Cipher.getInstance("DES/CBC/PKCS5Padding");
        des.init(Cipher.DECRYPT_MODE, DESKey, ivps);
        /*
        //Writes in 128 byte blocks
        byte[] Input = new byte[128];
        //Infinitely loops reading 128 bytes from the file, encrypting those bytes,
        //and writing them to the output stream
        while(true){
            int BytesRead = fin.read(Input);
            if(BytesRead == -1)
                break;
            byte[] OutputBytes = des.update(Input, 0, BytesRead);
            if(OutputBytes != null)
                Plaintext.append(Arrays.toString(des.update(OutputBytes)));
        }
         */
        //Write the final bytes to the output stream and close the file.
        byte[] OutputBytes = des.doFinal(CipherText);
        if(OutputBytes != null)
            Plaintext.append(Arrays.toString(des.update(OutputBytes)));
        fin.close();
        System.out.println(Plaintext.toString());
    }
}
