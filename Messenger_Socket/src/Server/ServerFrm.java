package Server;

import Entity.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import Service.*;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.Properties;
import java.util.Random;
import java.util.function.Consumer;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ServerFrm extends javax.swing.JFrame {

    List<User> _lstUsers;
    Connection _conn;
    ServerSocket _server;
    ClientService _service;
    Socket _socket;
    String request = "";
    List<User> _lstOnline;
    String feedBack;

    public ServerFrm() {
        _lstOnline = new ArrayList<>();
        _lstUsers = new ArrayList<>();
        _conn = getConnection();
        initComponents();
        startingConnect();
        getDataUsers();
        loadTableUsers();
        setLBL();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setSize(1200, 710);
    }

    Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=MESSENGER_SK", "sa", "123456");
        } catch (Exception e) {
        }
        return null;
    }

    void loadTableUsers() {
        DefaultTableModel def = (DefaultTableModel) tbl_listUsers.getModel();
        new Thread() {
            @Override
            public void run() {
                updateListUserOnline();
                while (!_server.isClosed()) {
                    def.setRowCount(0);
                    int i = 1;
                    for (User user : _lstUsers) {
                        def.addRow(new Object[]{
                            i++, user.getId(), user.getFullName(), checkOnline(user.getUserName()) ? "Online" : "Offline"
                        });
                    }
                    try {
                        sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerFrm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            private boolean checkOnline(String userName) {
                for (ClientService clientService : ClientService.lstSocket) {
                    if (clientService.getName().equals(userName)) {
                        return true;
                    }
                }
                return false;
            }

        }.start();
        tbl_listUsers.setFocusable(false);
        tbl_listUsers.setEnabled(false);
    }

    void closeServer() {
        try {
            if (_conn != null) {
                _conn.close();
            }
            if (_socket != null) {
                _socket.close();
            }
            if (_server != null) {
                _server.close();
            }
        } catch (Exception ex) {
        }
    }

    void startingConnect() {
        try {
            _server = new ServerSocket(6666);
        } catch (Exception e) {
            System.out.println("Server is not running!");
        }
        Thread thr = new Thread() {
            @Override
            public void run() {
                while (!_server.isClosed()) {
                    try {
                        _socket = _server.accept();
                        _service = new ClientService(_socket, "client" + ClientService.lstSocket.size()) {
                            @Override
                            public void run() {
                                try {
                                    while (_service.getSocket().isConnected()) {
                                        String request = this.getIn().readLine();
                                        processRequest(request);
                                        sleep(500);
                                    }
                                } catch (Exception ex) {
                                    this.closeEverything();
                                }
                            }
                        };
                        Thread thread = new Thread(_service);
                        thread.start();
                    } catch (Exception ex) {
                        closeServer();
                    }
                }
                try {
                    _server.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        thr.start();
    }

    void sendListOnline() {
        new Thread() {
            @Override
            public void run() {
                while (_service.getSocket().isConnected()) {
                    for (int i = 0; i < ClientService.lstSocket.size(); i++) {
                        ClientService client = ClientService.lstSocket.get(i);
                        if (_lstOnline.size() > 0 && !client.getSocket().isClosed() && client.isIsValidUser()) {
                            String feedBack = "listOnline ";
                            for (User user : _lstOnline) {
                                feedBack += user.getUserName() + " " + user.getFullName() + ",";
                            }
                            feedBack = feedBack + " " + client.getName();
                            client.getOut().println(feedBack);
                            client.getOut().flush();
                        }
                    }
                }
                try {
                    sleep(1000);
                } catch (Exception e) {
                }
            }

        }.start();
    }

    void processRequest(String request) {
        if (request.startsWith("login")) {
            logging(request);
        } else if (request.startsWith("checkUserName")) {
            checkUserName(request);
        } else if (request.startsWith("changePass")) {
            changePassword(request);
        } else if (request.startsWith("getDialogue")) {
            sendDialogue(request);
        } else if (request.startsWith("send")) {
            updateNewMessage(request);
        }
    }

    private void logging(String request) {
        String[] ar = request.split("\\s");
        String userName = ar[1];
        String passWord = ar[2];
        String clientName = request.substring(request.lastIndexOf(" ") + 1);
        String result = (checkLogin(userName, passWord) ? "loginOK " : "loginNotOk ") + userName + " " + clientName;
        if (checkLogin(userName, passWord)) {
            _service.setIsValidUser(true);
            _service.setName(userName);
        }
        _service.getOut().println(result);
        _service.getOut().flush();

    }

    private boolean checkUserName(String request) {
        String ar[] = request.split("\\s");
        String input = ar[1];
        for (User user : _lstUsers) {
            if (user.getUserName().equals(input)) {
                return true;
            }
        }
        return false;
    }

    boolean checkLogin(String userName, String passWord) {
        for (User user : _lstUsers) {
            if (user.getUserName().equals(userName) && user.getPassWord().equals(passWord)) {
                return true;
            }
        }
        return false;
    }

    void updateListUserOnline() {
        new Thread() {
            @Override
            public void run() {
                while (!_server.isClosed()) {
                    List<User> lst = new ArrayList<>();
                    int count = 0;
                    for (ClientService client : ClientService.lstSocket) {
                        if (client.isIsValidUser()) {
                            count++;
                            lst.add(getUserByUserName(client.getName()));
                        }
                    }
                    lbl_numUsers1.setText(String.valueOf(count));
                    _lstOnline = lst;
                    for (int i = 0; i < ClientService.lstSocket.size(); i++) {
                        ClientService client = ClientService.lstSocket.get(i);
                        if (client.isIsValidUser()) {
                            String feedBack = "listOnline ";
                            for (User user : _lstOnline) {
                                feedBack += user.getUserName() + " " + user.getFullName() + ",";
                            }
                            feedBack = feedBack + " " + client.getName();
                            client.getOut().println(feedBack);
                            client.getOut().flush();
                        }
                    }

                    try {
                        sleep(1000);
                    } catch (Exception e) {
                    }
                }
            }

        }.start();

    }

    private User getUserByUserName(String userName) {
        for (User user : _lstUsers) {
            if (user.getUserName().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    void setLBL() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            lbl_localHost.setText(ia.getHostAddress());
            lbl_port.setText("6666");
        } catch (Exception ex) {
            Logger.getLogger(ServerFrm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void getDataUsers() {
        try {
            _lstUsers.clear();
            ResultSet rs = _conn.prepareStatement("SELECT * FROM USERS").executeQuery();
            while (rs.next()) {
                _lstUsers.add(new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)));
            }
            rs.close();
        } catch (Exception e) {
            closeServer();
        }
    }

    private String generateOTP() {
        String otp = "";
        Random random = new Random();
        while (otp.length() < 6) {
            otp += (random.nextInt(8) + 1);
        }
        return otp;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_listUsers = new javax.swing.JTable();
        pnl_infor = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lbl_port = new javax.swing.JLabel();
        lbl_localHost = new javax.swing.JLabel();
        lbl_numUsers1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Server");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, 220, 40));

        tbl_listUsers.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbl_listUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "SI", "ID", "FullName", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_listUsers.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tbl_listUsers);
        if (tbl_listUsers.getColumnModel().getColumnCount() > 0) {
            tbl_listUsers.getColumnModel().getColumn(0).setResizable(false);
            tbl_listUsers.getColumnModel().getColumn(0).setPreferredWidth(20);
            tbl_listUsers.getColumnModel().getColumn(1).setResizable(false);
            tbl_listUsers.getColumnModel().getColumn(1).setPreferredWidth(50);
            tbl_listUsers.getColumnModel().getColumn(2).setResizable(false);
            tbl_listUsers.getColumnModel().getColumn(2).setPreferredWidth(200);
            tbl_listUsers.getColumnModel().getColumn(3).setResizable(false);
            tbl_listUsers.getColumnModel().getColumn(3).setPreferredWidth(200);
        }

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 1170, -1));

        pnl_infor.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));
        pnl_infor.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Port");
        pnl_infor.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 90, 30));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Localhost:");
        pnl_infor.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 90, 30));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Online:");
        pnl_infor.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 90, 30));

        lbl_port.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        pnl_infor.add(lbl_port, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 160, 180, 30));

        lbl_localHost.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        pnl_infor.add(lbl_localHost, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, 170, 30));

        lbl_numUsers1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        pnl_infor.add(lbl_numUsers1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 100, 180, 30));

        getContentPane().add(pnl_infor, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 410, 220));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerFrm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerFrm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_localHost;
    private javax.swing.JLabel lbl_numUsers1;
    private javax.swing.JLabel lbl_port;
    private javax.swing.JPanel pnl_infor;
    private javax.swing.JTable tbl_listUsers;
    // End of variables declaration//GEN-END:variables

    private void changePassword(String request) {
        String[] ar = request.split("\\s");
        String userName = ar[1];
        String newPass = ar[2];
        String clientName = request.substring(request.lastIndexOf(" ") + 1);
        if (checkUserName(request)) {
            try {
                PreparedStatement ps = getConnection().prepareStatement("UPDATE USERS SET passW = ? WHERE userName = ?");
                ps.setObject(1, newPass);
                ps.setObject(2, userName);
                ps.executeUpdate();
                _service.sendMessage("PSchanged " + clientName);
                getDataUsers();
                ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(ServerFrm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            _service.sendMessage("NoUser" + clientName);
        }
    }

    private void sendDialogue(String request) {
        String feedBack = "Dialogue ";
        String[] a = request.split(" ");
        String recieve = a[2];
        String sender = a[1];
        try {
            PreparedStatement ps = getConnection().prepareStatement("Exec getDialogue ?, ?");
            ps.setObject(1, a[1]);
            ps.setObject(2, a[2]);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                feedBack += (rs.getString(1) + "," + rs.getNString(2) + "," + rs.getString(3) + "," + rs.getString(4) + ";");
            }
            feedBack += recieve + " " + sender;
            this.feedBack = feedBack;
            int i = 3;
            while (i-- > 0) {
                ClientService.lstSocket.stream().forEach(t -> t.sendMessage(this.feedBack));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(ServerFrm.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void updateNewMessage(String request) {
        request = request.substring(request.indexOf(" ") + 1);
        String a[] = request.split(",");
        try {
            PreparedStatement ps = getConnection().prepareStatement("EXEC ADDMESSAGE ?, ?, ?");
            ps.setObject(1, a[0]);
            ps.setObject(2, a[1]);
            ps.setObject(3, a[2]);
            ps.executeUpdate();
            sendDialogue("getDialogue " + a[2] + " " + a[1]);
        } catch (SQLException ex) {
            Logger.getLogger(ServerFrm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendOTP(String email, String codeOTP) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", 587);

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("namtest2303@gmail.com", "I1m2s3o4f5h6");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Mã OTP khôi phục mật khẩu");
            message.setText("Mã OTP khôi phục mật khẩu: " + codeOTP);
            // send message
            Transport.send(message);

        } catch (MessagingException e) {
        }
    }

}
