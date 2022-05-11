package Client;

import Service.*;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class ClientFrame extends javax.swing.JFrame {

    Socket _socket;
    String userName;
    ClientService _s;
    BufferedReader _in;
    PrintWriter _out;
    List<String[]> _lst;          // index0 : userName, index1: fullName
    Thread thread;
    List<ChatFrame> _lstChat;
    LoginFrame _mainFrame;
    public ClientFrame(LoginFrame loginFrm) {
        _mainFrame = loginFrm;
        initComponents();
        _lstChat = new ArrayList<>();
        setDefaultTable();
        _lst = new ArrayList<>();
        this.setLocationRelativeTo(null);
        this.setSize(640, 600);
        this.setResizable(false);
        try {
            _socket = loginFrm.getS().getSocket();
            userName = loginFrm.getS().getName();
            _s = loginFrm.getS();
            connect();
            _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            _out = new PrintWriter(_socket.getOutputStream());
            lbl_userName.setText("Welcome " + userName);
        } catch (Exception e) {
            closeEveryThing();
        }
        connect();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    private void setDefaultTable() {
        DefaultTableModel de = (DefaultTableModel) tbl_listOnline.getModel();
        de.setRowCount(0);
    }

    public void connect() {
        new Thread() {
            @Override
            public void run() {
                while (_socket.isConnected()) {
                    try {
                        String feedback = _in.readLine();
                        String clientName = feedback.substring(feedback.lastIndexOf(" ") + 1);
                        if (clientName.equals(_s.getName())) {
                            processFeedback(feedback);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }

        }.start();

    }

    private void processFeedback(String feedback) {
        if (feedback.startsWith("listOnline")) {
            updateTableOnline(feedback);
        }
    }
    public void loadMessage(String feedBack) {
         String recieve = feedBack.substring(feedBack.lastIndexOf(";") + 1).split(" ")[0];
            _lstChat.stream().filter(t -> t.getRecipient().compareTo(recieve) == 0).filter(t -> t.getSender().compareTo(_s.getName()) == 0).forEach(t -> t.getLstMessage(feedBack));
    }
    private void updateTableOnline(String feedback) {
        feedback = feedback.substring(feedback.indexOf(" ") + 1, feedback.lastIndexOf(" "));
        List<String[]> lst = new ArrayList<>();
        String[] ar = feedback.split(",");
        if (ar.length > 0) {
            for (String string : ar) {
                if (!string.substring(0, string.indexOf(" ")).equals(userName)) {
                    lst.add(new String[]{
                        string.substring(0, string.indexOf(" ")), string.substring(string.indexOf(" ") + 1)
                    });
                }
            }
        }
        if (this._lst.size() != lst.size()) {
            DefaultTableModel def = (DefaultTableModel) tbl_listOnline.getModel();
            def.setRowCount(0);
            int i = 1;
            for (String[] strings : lst) {
                def.addRow(new Object[]{
                    i++, strings[1]
                });
            }
            tbl_listOnline.setModel(def);
            _lst = lst;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_listOnline = new javax.swing.JTable();
        lbl_userName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tbl_listOnline.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "SI", "User"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_listOnline.getTableHeader().setReorderingAllowed(false);
        tbl_listOnline.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_listOnlineMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_listOnline);
        if (tbl_listOnline.getColumnModel().getColumnCount() > 0) {
            tbl_listOnline.getColumnModel().getColumn(0).setResizable(false);
            tbl_listOnline.getColumnModel().getColumn(0).setPreferredWidth(10);
            tbl_listOnline.getColumnModel().getColumn(1).setResizable(false);
            tbl_listOnline.getColumnModel().getColumn(1).setPreferredWidth(300);
        }

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 600, 390));

        lbl_userName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lbl_userName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        getContentPane().add(lbl_userName, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 320, 40));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Online");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 110, 140, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tbl_listOnlineMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_listOnlineMouseClicked
        int index = tbl_listOnline.getSelectedRow();
        String recieve = String.valueOf(_lst.get(index)[0]);
        _out.println("getDialogue " + userName + " " + _lst.get(index)[0]);
        _out.flush();
        for (ChatFrame chatFrame : _lstChat) {
            if (chatFrame.getRecipient().equals(recieve)) {
                chatFrame.setVisible(true);
                return;
            }
        }
        _lstChat.add(new ChatFrame(_mainFrame, userName, _lst.get(index)[0], _lst.get(index)[1]));
        _lstChat.get(_lstChat.size() - 1).setVisible(true);

    }//GEN-LAST:event_tbl_listOnlineMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_userName;
    private javax.swing.JTable tbl_listOnline;
    // End of variables declaration//GEN-END:variables

    private void closeEveryThing() {
        try {
            if (_in != null) {
                _in.close();
            }
            if (_out != null) {
                _out.close();
            }
            if (_socket != null) {
                _socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
