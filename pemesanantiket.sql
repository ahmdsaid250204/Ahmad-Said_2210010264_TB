-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 19 Jan 2025 pada 07.19
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pemesanantiket`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `destinasi`
--

CREATE TABLE `destinasi` (
  `id_destinasi` int(11) NOT NULL,
  `Kota` varchar(100) DEFAULT NULL,
  `Negara` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `destinasi`
--

INSERT INTO `destinasi` (`id_destinasi`, `Kota`, `Negara`) VALUES
(5, 'Tokyo', 'Jepang'),
(6, 'Riyadh', 'Arab'),
(7, 'London', 'Inggris'),
(8, 'Washington DC', 'Amerika'),
(9, 'Roma', 'Italia'),
(10, 'Seoul', 'Korea Selatan');

-- --------------------------------------------------------

--
-- Struktur dari tabel `maskapai`
--

CREATE TABLE `maskapai` (
  `id_maskapai` int(11) NOT NULL,
  `Nama` varchar(100) DEFAULT NULL,
  `Kode` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `maskapai`
--

INSERT INTO `maskapai` (`id_maskapai`, `Nama`, `Kode`) VALUES
(5, 'Garuda Indonesia', 'GA'),
(6, 'Lion Air', 'JT'),
(7, 'Citilink', 'QG'),
(16, 'Batik Air', 'ID'),
(17, 'Sriwijaya Air', 'SJ');

-- --------------------------------------------------------

--
-- Struktur dari tabel `pemesanan`
--

CREATE TABLE `pemesanan` (
  `id_pemesanan` int(20) NOT NULL,
  `Nama_pemesan` varchar(100) DEFAULT NULL,
  `Maskapai` varchar(100) DEFAULT NULL,
  `Destinasi` varchar(100) DEFAULT NULL,
  `Tanggal_pemesan` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `pemesanan`
--

INSERT INTO `pemesanan` (`id_pemesanan`, `Nama_pemesan`, `Maskapai`, `Destinasi`, `Tanggal_pemesan`) VALUES
(2, 'bayu', 'Garuda Indonesia', 'Pilih Destinasi', '2025-01-06'),
(3, 'rehan', 'Batik Air', 'Riyadh (Arab)', '2025-01-07'),
(4, 'fai ', 'Sriwijaya Air', 'Seoul  (Korea Selatan)', '2025-01-08');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `destinasi`
--
ALTER TABLE `destinasi`
  ADD PRIMARY KEY (`id_destinasi`);

--
-- Indeks untuk tabel `maskapai`
--
ALTER TABLE `maskapai`
  ADD PRIMARY KEY (`id_maskapai`);

--
-- Indeks untuk tabel `pemesanan`
--
ALTER TABLE `pemesanan`
  ADD PRIMARY KEY (`id_pemesanan`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `destinasi`
--
ALTER TABLE `destinasi`
  MODIFY `id_destinasi` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT untuk tabel `maskapai`
--
ALTER TABLE `maskapai`
  MODIFY `id_maskapai` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT untuk tabel `pemesanan`
--
ALTER TABLE `pemesanan`
  MODIFY `id_pemesanan` int(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
