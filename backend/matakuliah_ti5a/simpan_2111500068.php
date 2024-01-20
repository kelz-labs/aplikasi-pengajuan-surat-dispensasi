<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "atmaluhur";

$conn = new mysqli($servername, $username, $password, $database);

if($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}

$nama_lengkap = $_POST['nama_lengkap'];
$mata_kuliah = $_POST['mata_kuliah'];
$kelompok = $_POST['kelompok'];
$pdf_file_name = $_POST['pdf_file_name'];
$image_file_name = $_POST['image_file_name'];

$sql = "INSERT INTO dispensasi (nama_lengkap, mata_kuliah, kelompok, pdf_file_name, image_file_name) VALUES ('$nama_lengkap', '$mata_kuliah', '$kelompok', '$pdf_file_name', '$image_file_name')";

if($conn->query($sql) === TRUE) {
  echo "Data sukses dimasukkan!";
} else {
  echo "Error:" . $sql . "<br>" . $conn->error;
}

$conn->close();
?>
