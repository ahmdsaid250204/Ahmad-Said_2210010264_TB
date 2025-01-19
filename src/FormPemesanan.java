
import java.text.SimpleDateFormat;
import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */
public class FormPemesanan extends javax.swing.JFrame {

    DefaultTableModel tableModel;

    /**
     * Creates new form FormPemesanan
     */
    public FormPemesanan() {
        initComponents();

        // Inisialisasi model tabel
        tableModel = new DefaultTableModel(
                new Object[]{"ID Pemesanan", "Nama Pemesan", "Maskapai", "Destinasi", "Tanggal Pemesanan"}, 0
        );
        jTable1.setModel(tableModel);

        // Panggil loadData untuk memuat data ke tabel
        loadData();

        // Tambahkan event listener untuk mouse click di jTable1
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
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
            try {
                // Mengambil data dari baris yang dipilih sesuai kolom tabel
                int idPemesanan = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString()); // Kolom "id_pemesanan"
                String namaPemesan = tableModel.getValueAt(selectedRow, 1).toString(); // Kolom "Nama_pemesan"
                String maskapai = tableModel.getValueAt(selectedRow, 2).toString(); // Kolom "Maskapai"
                String destinasi = tableModel.getValueAt(selectedRow, 3).toString(); // Kolom "Destinasi"
                Object tanggalObj = tableModel.getValueAt(selectedRow, 4); // Kolom "Tanggal_pemesan"

                // Mengisi data ke field input
                Nama_pemesan.setText(namaPemesan); // Field untuk "Nama_pemesan"

                // Mengisi data ke JComboBox Maskapai
                Maskapai.setSelectedItem(maskapai); // Pilih item yang cocok di JComboBox Maskapai

                // Mengisi data ke JComboBox Destinasi
                Destinasi.setSelectedItem(destinasi); // Pilih item yang cocok di JComboBox Destinasi

                // Mengisi data ke JDateChooser Tanggal_pemesan
                if (tanggalObj != null) {
                    java.util.Date parsedDate = null;

                    if (tanggalObj instanceof java.sql.Date) {
                        // Jika data adalah java.sql.Date, konversi ke java.util.Date
                        parsedDate = new java.util.Date(((java.sql.Date) tanggalObj).getTime());
                    } else if (tanggalObj instanceof java.util.Date) {
                        // Jika data sudah berupa java.util.Date, gunakan langsung
                        parsedDate = (java.util.Date) tanggalObj;
                    } else if (tanggalObj instanceof String) {
                        // Jika data berupa String, parsing sesuai format yang diharapkan
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd"); // Format tanggal sesuai data di tabel
                        parsedDate = format.parse((String) tanggalObj);
                    }

                    // Set tanggal ke JDateChooser
                    if (parsedDate != null) {
                        Tanggal_pemesan.setDate(parsedDate);
                    } else {
                        Tanggal_pemesan.setDate(null); // Kosongkan jika parsing gagal
                    }
                } else {
                    Tanggal_pemesan.setDate(null); // Kosongkan jika tidak ada data tanggal
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID pemesanan tidak valid: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Format tanggal tidak valid: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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

            // Query SQL untuk mengambil data dari tabel "pemesanan"
            String sql = "SELECT * FROM pemesanan";
            stmt = conn.prepareStatement(sql);

            // Eksekusi query
            rs = stmt.executeQuery();

            // Format tanggal untuk tampilan di tabel
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy");

            // Loop melalui hasil query dan tambahkan ke model tabel
            while (rs.next()) {
                int idPemesanan = rs.getInt("id_pemesanan"); // Kolom id_pemesanan
                String namaPemesan = rs.getString("Nama_pemesan"); // Kolom Nama_pemesan
                String maskapai = rs.getString("maskapai"); // Kolom maskapai
                String destinasi = rs.getString("destinasi"); // Kolom destinasi

                // Ambil tanggal dari database dan tangani kemungkinan null
                java.sql.Date sqlDate = rs.getDate("Tanggal_pemesan");
                String formattedDate = ""; // Default kosong jika tanggal null
                java.util.Date utilDate = null;

                if (sqlDate != null) {
                    utilDate = new java.util.Date(sqlDate.getTime());
                    formattedDate = displayFormat.format(utilDate);
                }

                // Tambahkan data ke model tabel
                tableModel.addRow(new Object[]{idPemesanan, namaPemesan, maskapai, destinasi, formattedDate});
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
        String namaPemesan = Nama_pemesan.getText().trim();
        String maskapai = (String) Maskapai.getSelectedItem(); // Mengambil nilai dari JComboBox
        String destinasi = (String) Destinasi.getSelectedItem(); // Mengambil nilai dari JComboBox
        java.util.Date tanggalPemesanDate = Tanggal_pemesan.getDate(); // Mengambil tanggal dari JDateChooser

        // Validasi input
        if (namaPemesan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Pemesan tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (maskapai == null || maskapai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih Maskapai yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (destinasi == null || destinasi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih Destinasi yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (tanggalPemesanDate == null) {
            JOptionPane.showMessageDialog(this, "Tanggal Pemesanan tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Konversi tanggal ke format yang sesuai untuk database
        String tanggalPemesanan = new java.text.SimpleDateFormat("yyyy-MM-dd").format(tanggalPemesanDate);

        // Query SQL untuk menambahkan data
        String sql = "INSERT INTO pemesanan (Nama_pemesan, Maskapai, Destinasi, Tanggal_pemesan) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Mengisi parameter query
            stmt.setString(1, namaPemesan);
            stmt.setString(2, maskapai);
            stmt.setString(3, destinasi);
            stmt.setString(4, tanggalPemesanan);

            // Menjalankan query
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    int idPemesanan = 0;
                    if (generatedKeys.next()) {
                        idPemesanan = generatedKeys.getInt(1);
                    }

                    JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan ke database!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                    // Tambahkan data ke tabel
                    tableModel.addRow(new Object[]{idPemesanan, namaPemesan, maskapai, destinasi, tanggalPemesanan});

                    // Membersihkan field input
                    clearFields();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Tidak ada data yang ditambahkan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Maskapai atau Destinasi tidak valid: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        String idPemesanan = tableModel.getValueAt(selectedRow, 0).toString(); // ID Pemesanan (kolom ke-0 di tabel GUI)
        String namaPemesanan = Nama_pemesan.getText(); // Input Nama Pemesanan
        String maskapai = Maskapai.getSelectedItem().toString(); // Pilihan Maskapai dari JComboBox
        String destinasi = Destinasi.getSelectedItem().toString(); // Pilihan Destinasi dari JComboBox
        java.util.Date tanggalPemesanan = Tanggal_pemesan.getDate(); // Input Tanggal Pemesanan dari JDateChooser

        // Validasi input
        if (namaPemesanan.isEmpty() || maskapai.isEmpty() || destinasi.isEmpty() || tanggalPemesanan == null) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Koneksi ke database
            conn = DatabaseConnection.getConnection();

            // Query SQL untuk update data
            String sql = "UPDATE pemesanan SET Nama_pemesan = ?, Maskapai = ?, Destinasi = ?, Tanggal_pemesan = ? WHERE id_pemesanan = ?";
            stmt = conn.prepareStatement(sql);

            // Mengisi parameter query
            stmt.setString(1, namaPemesanan);
            stmt.setString(2, maskapai);
            stmt.setString(3, destinasi);

            // Konversi java.util.Date ke java.sql.Date
            java.sql.Date sqlDate = new java.sql.Date(tanggalPemesanan.getTime());
            stmt.setDate(4, sqlDate);
            stmt.setInt(5, Integer.parseInt(idPemesanan));

            // Menjalankan query
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Update data di tabel GUI
                tableModel.setValueAt(namaPemesanan, selectedRow, 1); // Update kolom Nama Pemesanan
                tableModel.setValueAt(maskapai, selectedRow, 2); // Update kolom Maskapai
                tableModel.setValueAt(destinasi, selectedRow, 3); // Update kolom Destinasi
                tableModel.setValueAt(sqlDate.toString(), selectedRow, 4); // Update kolom Tanggal Pemesanan

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

    //3. Kodingan Hapus Data
    private void hapusData() {
        // Mendapatkan baris yang dipilih
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Konfirmasi sebelum menghapus data
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Mengambil nilai id_pemesanan dari baris yang dipilih
            int idPemesanan = (int) tableModel.getValueAt(selectedRow, 0);

            // Koneksi database dan pernyataan SQL
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                // Membuka koneksi ke database
                conn = DatabaseConnection.getConnection();

                // Query SQL untuk menghapus data berdasarkan id_pemesanan
                String sql = "DELETE FROM pemesanan WHERE id_pemesanan = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idPemesanan);

                // Menjalankan perintah penghapusan
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                    // Menghapus baris dari model tabel
                    tableModel.removeRow(selectedRow);

                    // Membersihkan field input (opsional)
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Menutup resource (koneksi dan pernyataan)
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

    // Metode untuk membersihkan field input
    private void clearFields() {
        Nama_pemesan.setText("");
        Maskapai.setSelectedIndex(0); // Mengatur JComboBox ke item pertama
        Destinasi.setSelectedIndex(0); // Mengatur JComboBox ke item pertama
        Tanggal_pemesan.setDate(null); // Mengosongkan JDateChooser
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        Nama_pemesan = new javax.swing.JTextField();
        Maskapai = new javax.swing.JComboBox<>();
        Destinasi = new javax.swing.JComboBox<>();
        tambahData = new javax.swing.JButton();
        exportData = new javax.swing.JButton();
        hapusData = new javax.swing.JButton();
        kembali = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        Tanggal_pemesan = new com.toedter.calendar.JDateChooser();
        editData = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Form Pemesanan");

        jLabel2.setText("Nama Pemesan :");

        jLabel3.setText("Maskapai :");

        jLabel4.setText("Destinasi :");

        jLabel5.setText("Tanggal :");

        Nama_pemesan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Nama_pemesanActionPerformed(evt);
            }
        });

        Maskapai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Maskapai", "Garuda Indonesia", "Batik Air", "Sriwijaya Air", "Citilink", "Lion Air" }));

        Destinasi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Destinasi", "Tokyo (Jepang)", "Riyadh (Arab)", "Washington DC (Amerika)", "Roma (Italia)", "Seoul  (Korea Selatan)", "London (Inggris)" }));

        tambahData.setText("Tambah");
        tambahData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tambahDataActionPerformed(evt);
            }
        });

        exportData.setText("Export");
        exportData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDataActionPerformed(evt);
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

        editData.setText("Edit");
        editData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(333, 333, 333)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 333, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Nama_pemesan)
                            .addComponent(Destinasi, 0, 184, Short.MAX_VALUE)
                            .addComponent(Maskapai, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Tanggal_pemesan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(228, 228, 228))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(tambahData)
                        .addGap(13, 13, 13)
                        .addComponent(editData, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(hapusData, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addComponent(kembali)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(exportData, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(371, 371, 371))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addGap(142, 142, 142))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Nama_pemesan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(Maskapai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Destinasi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(Tanggal_pemesan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(79, 79, 79)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tambahData)
                    .addComponent(hapusData)
                    .addComponent(kembali)
                    .addComponent(editData)
                    .addComponent(exportData))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
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

    private void Nama_pemesanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Nama_pemesanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Nama_pemesanActionPerformed

    private void tambahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tambahDataActionPerformed
        tambahData();// TODO add your handling code here:
    }//GEN-LAST:event_tambahDataActionPerformed

    private void exportDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDataActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File CSV");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            exportData(jTable1, file.getAbsolutePath());
        } // TODO add your handling code here:
    }//GEN-LAST:event_exportDataActionPerformed

    private void hapusDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusDataActionPerformed
        hapusData();// TODO add your handling code here:
    }//GEN-LAST:event_hapusDataActionPerformed

    private void kembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kembaliActionPerformed
        kembali();// TODO add your handling code here:
    }//GEN-LAST:event_kembaliActionPerformed

    private void editDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDataActionPerformed
        editData();// TODO add your handling code here:
    }//GEN-LAST:event_editDataActionPerformed

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
            java.util.logging.Logger.getLogger(FormPemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormPemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormPemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormPemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormPemesanan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> Destinasi;
    private javax.swing.JComboBox<String> Maskapai;
    private javax.swing.JTextField Nama_pemesan;
    private com.toedter.calendar.JDateChooser Tanggal_pemesan;
    private javax.swing.JButton editData;
    private javax.swing.JButton exportData;
    private javax.swing.JButton hapusData;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton kembali;
    private javax.swing.JButton tambahData;
    // End of variables declaration//GEN-END:variables

    private void exportData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
