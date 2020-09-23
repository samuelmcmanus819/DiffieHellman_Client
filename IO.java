import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IO {
    /*
    Name: ReadPlaintext
    Purpose: Read the plaintext of the client file
    Author: Samuel McManus
    Uses: N/A
    Used By: Networking.SendFile
    Date: September 22, 2020
     */
    static void ReadPlaintext() throws IOException {
        System.out.println("\nMy file:");
        File CredentialFile = new File("ClientFile.txt");
        BufferedReader FileReader = new BufferedReader(new FileReader(CredentialFile));

        //Loops through the whole file looking to see if the username already exists. If so,
        //return the user's credentials.
        String FileLine;
        while((FileLine = FileReader.readLine())!= null){
            System.out.println(FileLine);
        }
        //If the user doesn't exist, return nothing.
        FileReader.close();
    }
}
