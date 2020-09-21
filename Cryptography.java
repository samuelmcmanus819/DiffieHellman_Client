import javax.crypto.KeyAgreement;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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
}
