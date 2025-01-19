
import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */
public class FormDestinasi extends javax.swing.JFrame {

    DefaultTableModel tableModel;

    /**
     * Creates new form FormDestinasi
     */
    public FormDestinasi() {
        initComponents();

    

        // Inisialisasi model tabel
        tableModel = new DefaultTableModel(
                new Object[]{"ID Destinasi", "Kota", "Negara"}, 0
        );
        jTable1.setModel(tableModel);

        // Panggil loadData untuk memuat data ke tabel
        loadData();

        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
    }

    public void exportData(JTable table, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Menulis header tabel
            for (int i = 0; i < table.getColumnCount(); i++) {
                writer.write(table.getColumnName(i));
                if (i < table.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");

            // Menulis data baris
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    Object value = table.getValueAt(i, j);
                    writer.write(value != null ? value.toString() : "");
                    if (j < table.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }

            writer.flush();
            JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke: " + filePath, "Export CSV", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat ekspor data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCSV(String value) {
        String escapedValue = value.replace("\"", "\"\""); // Escape tanda kutip
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            escapedValue = "\"" + escapedValue + "\""; // Tambahkan tanda kutip jika ada karakter khusus
        }
        return escapedValue;
    }

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
        // Mendapatkan baris yang diklik
        int selectedRow = jTable1.getSelectedRow();

        if (selectedRow != -1) {
            // Mengambil data dari baris yang dipilih
            String kota = tableModel.getValueAt(selectedRow, 1).toString(); // Kolom "KOta"
            String negara = tableModel.getValueAt(selectedRow, 2).toString(); // Kolom "Negara"

            // Mengisi data ke field input
            Kota.setText(kota);
            Negara.setText(negara);
        }
    }

    private void loadData() {
        // Hapus semua baris di tabel sebelum memuat data baru
        tableModel.setRowCount(0);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Dapatkan koneksi ke database
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk mengambil data dari tabel "destinasi"
            String sql = "SELECT * FROM destinasi";
            stmt = conn.prepareStatement(sql);

            // Eksekusi query
            rs = stmt.executeQuery();

            // Loop melalui hasil query dan tambahkan ke model tabel
            while (rs.next()) {
                int id = rs.getInt("id_destinasi"); // Kolom id_destinasi
                String kota = rs.getString("Kota"); // Kolom Kota
                String negara = rs.getString("Negara"); // Kolom Negara

                // Tambahkan data ke model tabel
                tableModel.addRow(new Object[]{id, kota, negara});
            }
        } catch (SQLException e) {
            // Tampilkan pesan error jika terjadi kesalahan saat memuat data
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Tutup semua resource (ResultSet, PreparedStatement, Connection)
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menutup koneksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //1. Kodingan Tombol Tambah
    private void tambahData() {
        // Mengambil data dari input pengguna
        String kotaDestinasi = Kota.getText();
        String negaraDestinasi = Negara.getText();

        // Validasi input
        if (kotaDestinasi.isEmpty() || negaraDestinasi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Menghentikan eksekusi jika validasi gagal
        }

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // Dapatkan koneksi dari kelas DatabaseConnection
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk menambahkan data
            String sql = "INSERT INTO destinasi (Kota, Negara) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            // Mengisi parameter query
            stmt.setString(1, kotaDestinasi);
            stmt.setString(2, negaraDestinasi);

            // Menjalankan query
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                // Ambil ID yang baru saja dimasukkan
                java.sql.ResultSet generatedKeys = stmt.getGeneratedKeys();
                int idDestinasi = 0;
                if (generatedKeys.next()) {
                    idDestinasi = generatedKeys.getInt(1);
                }

                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan ke database!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Tambahkan data ke tabel
                tableModel.addRow(new Object[]{idDestinasi, kotaDestinasi, negaraDestinasi});

                // Membersihkan field input
                clearFields();
            }
        } catch (SQLException e) {
            // Menangani error yang terjadi saat bekerja dengan database
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                // Menutup statement dan koneksi
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menutup koneksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //2. Kodingan Tombol Edit
    private void editData() {
        int selectedRow = jTable1.getSelectedRow(); // Mendapatkan baris yang dipilih di tabel GUI
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mendapatkan data dari field input
        String idDestinasi = tableModel.getValueAt(selectedRow, 0).toString(); // ID Destinasi (kolom ke-0 di tabel GUI)
        String kota = Kota.getText(); // Input Kota
        String negara = Negara.getText(); // Input Negara

        if (kota.isEmpty() || negara.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Koneksi ke database
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk update data
            String sql = "UPDATE destinasi SET Kota = ?, Negara = ? WHERE id_destinasi = ?";
            stmt = conn.prepareStatement(sql);

            // Mengisi parameter query
            stmt.setString(1, kota);
            stmt.setString(2, negara);
            stmt.setInt(3, Integer.parseInt(idDestinasi));

            // Menjalankan query
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Update data di tabel GUI
                tableModel.setValueAt(kota, selectedRow, 1); // Update kolom Kota
                tableModel.setValueAt(negara, selectedRow, 2); // Update kolom Negara

                // Bersihkan input field
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memperbarui data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                // Menutup statement dan koneksi
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menutup koneksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 3. Kodingan Tombol Hapus
    private void hapusData() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int idDestinasi = (int) tableModel.getValueAt(selectedRow, 0);

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();
                String sql = "DELETE FROM destinasi WHERE id_destinasi = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idDestinasi);

                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    tableModel.removeRow(selectedRow);
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menutup koneksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // 4. Kodingan Tombol Kembali
    private void kembali() {
        JOptionPane.showMessageDialog(this, "Kembali ke menu utama");
        dispose(); // Menutup jendela saat ini
    }

    // Fungsi untuk membersihkan field
    private void clearFields() {

        Kota.setText("");
        Negara.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        Kota = new javax.swing.JTextField();
        Negara = new javax.swing.JTextField();
        tambahData = new javax.swing.JButton();
        editData = new javax.swing.JButton();
        hapusData = new javax.swing.JButton();
        kembali = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Master Destinasi");

        jLabel3.setText("Kota :");

        jLabel4.setText("Negara :");

        tambahData.setText("Tambah");
        tambahData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tambahDataActionPerformed(evt);
            }
        });

        editData.setText("Edit");
        editData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDataActionPerformed(evt);
            }
        });

        hapusData.setText("Hapus");
        hapusData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusDataActionPerformed(evt);
            }
        });

        kembali.setText("Kembali");
        kembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kembaliActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(333, 333, 333)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tambahData)
                                .addGap(27, 27, 27)
                                .addComponent(editData, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(hapusData, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(kembali)
                                .addGap(18, 18, 18)
                                .addComponent(btnExport)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 460, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Kota, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Negara, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(83, 83, 83))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(76, 76, 76)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(Kota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(Negara, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tambahData)
                    .addComponent(editData)
                    .addComponent(kembali)
                    .addComponent(hapusData)
                    .addComponent(btnExport))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void kembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kembaliActionPerformed
        kembali();// TODO add your handling code here:
    }//GEN-LAST:event_kembaliActionPerformed

    private void editDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDataActionPerformed
        editData();// TODO add your handling code here:
    }//GEN-LAST:event_editDataActionPerformed

    private void hapusDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusDataActionPerformed
        hapusData();// TODO add your handling code here:
    }//GEN-LAST:event_hapusDataActionPerformed

    private void tambahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tambahDataActionPerformed
        tambahData();// TODO add your handling code here:
    }//GEN-LAST:event_tambahDataActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File CSV");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            exportData(jTable1, file.getAbsolutePath());
        }// TODO add your handling code here:
    }//GEN-LAST:event_btnExportActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new FormDestinasi().setVisible(true));

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Kota;
    private javax.swing.JTextField Negara;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton editData;
    private javax.swing.JButton hapusData;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton kembali;
    private javax.swing.JButton tambahData;
    // End of variables declaration//GEN-END:variables

}
