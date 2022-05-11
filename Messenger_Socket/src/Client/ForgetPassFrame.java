package Client;


import java.io.*;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ForgetPassFrame extends javax.swing.JFrame {

    Thread thread;
    int stopWatch;
    LoginFrame _mainThread;
    public ForgetPassFrame(LoginFrame loginFrame) {
        initComponents();
        stopWatch = 0;
        _mainThread = loginFrame;
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setSize(644, 351);
    }
    

    public void resetText() {
        txt_pass1.setText("");
        txt_pass2.setText("");
        txt_userName.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txt_userName = new javax.swing.JTextField();
        txt_pass2 = new javax.swing.JPasswordField();
        txt_pass1 = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        lbl_time = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("New confirm password:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 190, 150, 30));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Username:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 20, 80, 30));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("New password :");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 100, 110, 30));
        getContentPane().add(txt_userName, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, 290, 40));
        getContentPane().add(txt_pass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 180, 290, 40));
        getContentPane().add(txt_pass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 90, 290, 40));

        jButton1.setText("Change");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 270, 100, 40));
        getContentPane().add(lbl_time, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 230, 170, 30));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (txt_pass1.getText().equals(txt_pass2.getText())) {
                        stopWatch = 0;
            if (stopWatch == 0) coutingTime();
            if (stopWatch > 120) {         
                JOptionPane.showMessageDialog(this, "Time exceeded! Please try again!");
                stopWatch = 0;
                this.dispose();
                _mainThread.setVisible(true);
                return;
            }
            _mainThread.getS().sendMessage("changePass " + txt_userName.getText() + " " + txt_pass1.getText() + " " + _mainThread.getS().getName());
            String otpCode ="", input;
            int i = 1;
            
//            while (i++ < 3) {
//                input = JOptionPane.showInputDialog("Enter the otp code:");
//                if (i <= 3 && otpCode.equals(input)) {
// 
//                    return;
//                }
//            }
           // JOptionPane.showMessageDialog(this, "you have entered more than 3 of times! Please try again!");
        } else {
            JOptionPane.showMessageDialog(this, "Two passwords are not the same!");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(ForgetPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ForgetPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ForgetPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ForgetPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

    }

    private void coutingTime() {
        new Thread() {
            @Override
            public void run() {
                while (stopWatch++ <= 60) {
                    try {
                        lbl_time.setText("Time remaining: " + (60 - stopWatch) + "s");
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ForgetPassFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel lbl_time;
    private javax.swing.JPasswordField txt_pass1;
    private javax.swing.JPasswordField txt_pass2;
    private javax.swing.JTextField txt_userName;
    // End of variables declaration//GEN-END:variables

    public void stop() {
        thread.stop();
    }
}
