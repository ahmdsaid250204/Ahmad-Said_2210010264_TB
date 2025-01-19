
import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */
public class FormMaskapai extends javax.swing.JFrame {

    DefaultTableModel tableModel;

    public FormMaskapai() {
        initComponents();

        // Inisialisasi model tabel
        tableModel = new DefaultTableModel(
                new Object[]{"ID Maskapai", "Nama", "Kode"}, 0
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
            String nama = tableModel.getValueAt(selectedRow, 1).toString(); // Kolom "Nama"
            String kode = tableModel.getValueAt(selectedRow, 2).toString(); // Kolom "Kode"

            // Mengisi data ke field input
            Nama.setText(nama);
            Kode.setText(kode);
        }
    }

    private void loadData() {
        // Hapus semua baris di tabel sebelum memuat data baru
        tableModel.setRowCount(0);

        Connection conn = null;
        PreparedStatement stmt = null;
        java.sql.ResultSet rs = null;

        try {
            // Dapatkan koneksi ke database
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk mengambil data dari tabel "maskapai"
            String sql = "SELECT * FROM maskapai";
            stmt = conn.prepareStatement(sql);

            // Eksekusi query
            rs = stmt.executeQuery();

            // Loop melalui hasil query dan tambahkan ke model tabel
            while (rs.next()) {
                int id = rs.getInt("id_maskapai");
                String nama = rs.getString("Nama");
                String kode = rs.getString("Kode");

                // Tambahkan data ke model tabel
                tableModel.addRow(new Object[]{id, nama, kode});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Tutup resource
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

    // 1. Kodingan Tombol Tambah
    private void tambahData() {
        // Mengambil data dari input pengguna
        String namaMaskapai = Nama.getText();
        String kodeMaskapai = Kode.getText();

        // Validasi input
        if (namaMaskapai.isEmpty() || kodeMaskapai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Menghentikan eksekusi jika validasi gagal
        }

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // Dapatkan koneksi dari kelas DatabaseConnection
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk menambahkan data
            String sql = "INSERT INTO maskapai (Nama, Kode) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            // Mengisi parameter query
            stmt.setString(1, namaMaskapai);
            stmt.setString(2, kodeMaskapai);

            // Menjalankan query
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                // Ambil ID yang baru saja dimasukkan
                java.sql.ResultSet generatedKeys = stmt.getGeneratedKeys();
                int idMaskapai = 0;
                if (generatedKeys.next()) {
                    idMaskapai = generatedKeys.getInt(1);
                }

                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan ke database!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Tambahkan data ke tabel
                tableModel.addRow(new Object[]{idMaskapai, namaMaskapai, kodeMaskapai});

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

    // 2. Kodingan Tombol Edit
    private void editData() {
        int selectedRow = jTable1.getSelectedRow(); // Mendapatkan baris yang dipilih
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mendapatkan data dari field input
        String nama = Nama.getText().trim();
        String kode = Kode.getText().trim();

        // Validasi input
        if (nama.isEmpty() || kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mendapatkan ID Maskapai dari tabel (kolom 0)
        int idMaskapai = (int) tableModel.getValueAt(selectedRow, 0);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Dapatkan koneksi ke database
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk memperbarui data berdasarkan ID Maskapai
            String sql = "UPDATE maskapai SET Nama = ?, Kode = ? WHERE id_maskapai = ?";
            stmt = conn.prepareStatement(sql);

            // Mengisi parameter query
            stmt.setString(1, nama);
            stmt.setString(2, kode);
            stmt.setInt(3, idMaskapai);

            // Menjalankan query
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Memperbarui data di tabel
                tableModel.setValueAt(nama, selectedRow, 1);
                tableModel.setValueAt(kode, selectedRow, 2);

                // Membersihkan field input
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
            int idMaskapai = (int) tableModel.getValueAt(selectedRow, 0);

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();
                String sql = "DELETE FROM maskapai WHERE id_maskapai = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idMaskapai);

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

        Nama.setText("");
        Kode.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        Nama = new javax.swing.JTextField();
        Kode = new javax.swing.JTextField();
        tambahData = new javax.swing.JButton();
        editData = new javax.swing.JButton();
        hapusData = new javax.swing.JButton();
        kembali = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Master Maskapai");

        jLabel3.setText("Nama :");

        jLabel4.setText("Kode :");

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

        jButton1.setText("Export");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 526, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Nama, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                            .addComponent(Kode)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(tambahData)
                        .addGap(18, 18, 18)
                        .addComponent(editData, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(hapusData, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(kembali)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE))
                .addGap(71, 71, 71))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(323, 323, 323))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(79, 79, 79)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(Nama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(Kode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tambahData)
                    .addComponent(editData)
                    .addComponent(kembali)
                    .addComponent(hapusData)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(105, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void hapusDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusDataActionPerformed
        hapusData();// TODO add your handling code here:
    }//GEN-LAST:event_hapusDataActionPerformed

    private void kembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kembaliActionPerformed
        kembali(); // TODO add your handling code here:
    }//GEN-LAST:event_kembaliActionPerformed

    private void tambahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tambahDataActionPerformed
        tambahData();// TODO add your handling code here:
    }//GEN-LAST:event_tambahDataActionPerformed

    private void editDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDataActionPerformed
        editData();// TODO add your handling code here:
    }//GEN-LAST:event_editDataActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File CSV");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            exportData(jTable1, file.getAbsolutePath());
        }// TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new FormMaskapai().setVisible(true));

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Kode;
    private javax.swing.JTextField Nama;
    private javax.swing.JButton editData;
    private javax.swing.JButton hapusData;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton kembali;
    private javax.swing.JButton tambahData;
    // End of variables declaration//GEN-END:variables
}
