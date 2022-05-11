package Service;

import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientService implements Runnable {

    private Socket _socket;
    private BufferedReader _in;
    private PrintWriter _out;
    public static List<ClientService> lstSocket = new ArrayList<>();
    private boolean isValidUser = false;
    private String name;

    public ClientService(Socket _socket, String name) {
        try {
            this.name = name;
            this._socket = _socket;
            lstSocket.add(this);
            _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            _out = new PrintWriter(_socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
            closeEverything();
        }
    }

    public boolean isIsValidUser() {
        return isValidUser;
    }

    public void setIsValidUser(boolean isValidUser) {
        this.isValidUser = isValidUser;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Socket getSocket() {
        return _socket;
    }

    public void sendMessage(String message) {
        _out.println(message);
        _out.flush();
    }

    public PrintWriter getOut() {
        return _out;
    }

    private void removeThisSession() {
        lstSocket.remove(this);
    }

    public void closeEverything() {
        removeThisSession();
        try {
            _in.close();
            _out.close();
            _socket.close();
        } catch (Exception e) {
        }
    }

    public BufferedReader getIn() {
        return _in;
    }

    @Override
    public void run() {
        try {
            while (_socket.isConnected()) {
                String message = _in.readLine();
                System.out.println(message);
            }
        } catch (IOException ex) {
            closeEverything();
        }
    }
}
