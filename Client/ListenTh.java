package Client;

import AES.AES_Verschluesselung;

import java.io.*;
import java.net.*;

public class ListenTh extends Thread{
    Socket s;
    PrintWriter out;
    BufferedReader in;
    BufferedReader sysIn;
    String name;

    ListenTh(Socket s, PrintWriter out, BufferedReader in, BufferedReader sysIn){
        this.s = s;
        this.out = out;
        this.in = in;
        this.sysIn = sysIn;
    }

    @Override
    public void run() {
        try{
            while(true) {
                var line = in.readLine();

                if (line.startsWith("LOGIN")||line.startsWith("REGISTER")) {
                    if(line.startsWith("LOGIN")){
                        line = line.substring(5);
                    }else{
                        line = line.substring(8);
                    }
                    System.out.println(line);
                    name = sysIn.readLine();
                    out.println(name);
                } else if (line.startsWith("CONFIRMED")) {
                    System.out.println("Login successful");
                    SendTh sendThread = new SendTh(s,out,in,sysIn);
                    sendThread.start();
                } else if (line.startsWith("MESSAGE")) {
                    System.out.println(line.substring(7));
                } else if(line.startsWith("CHAT")){
                    String recipient = line.substring(4);
                    out.println("CHAT"+recipient);
                }
            }

        }catch(IOException ignored){}
    }
}
class SendTh extends Thread{
    Socket s;
    PrintWriter out;
    BufferedReader in;
    BufferedReader sysIn;
    SendTh(Socket s, PrintWriter out, BufferedReader in, BufferedReader sysIn){
        this.s = s;
        this.out = out;
        this.in = in;
        this.sysIn = sysIn;
    }

    @Override
    public void run() {
        try {

            //************************ Privaten Schlüssel festlegen***************************************
            final String secretKey = "ADGE154ADR87DSW6";
            System.out.println("Dieser Chat wird mit AES verschlüsselt\n");
            //*********************************************************************************************

            while(true) {
                String msg = sysIn.readLine();
                String encryptedInput;
                String decryptedInput;

                // Verschlüsselung der Nachricht bevor sie zum Server geschickt wird
                encryptedInput = AES_Verschluesselung.encrypt(msg,secretKey);

                // schickt zum Server
                out.println(encryptedInput);
                out.flush();

                // bekommt vom Server
                if(msg != null){
                    System.out.println("\nServer: " + msg); // -> verschlüsselte Nachricht
                }

                // entschlüsselt die Nachticht vom Server
                decryptedInput = AES_Verschluesselung.decrypt(msg, secretKey);
                System.out.println("Server: " + decryptedInput + "\n");

                if (msg.equals("GOODBYE")) {
                    out.println(msg);
                    System.out.println("See you soon!");
                    System.err.println("Logged off...");
                    break;
                }else{
                    out.println(msg);
                }
            }
            s.close();
        }catch (IOException ignored){}
    }
}