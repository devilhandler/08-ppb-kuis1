package com.example.studentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studentmanager.ui.theme.StudentManagerTheme


data class Student(
    val nrp: String,
    val nama: String,
    val prodi: String
)

val daftarProdi = listOf(
    "Teknik Informatika",
    "Rekayasa Perangkat Lunak",
    "Rekayasa Kecerdasan Artifisial",
    "Inovasi Digital",
    "Teknik Elektro",
    "Sistem Informasi",
    "Teknik Komputer",
    "Teknik Mesin",
    "Teknik Material dan Metalurgi",
    "Teknik Lingkungan",
    "Teknik Geofisika",
    "Teknik Kelautan",
    "Teknik Sistem Perkapalan",
    "Teknik Lepas Pantai",
    "Desain Produk",
    "Desain Komunikasi Visual",
    "Aktuaria",
    "Matematika",
    "Bisnis Digital",
    "Fisika",
    "Teknik Fisika",
    "Kimia",
    "Teknik Kimia"
)

val dataAwal = listOf(
    Student("5053251013", "Hilmi Fauzi Adha Simamora", "Rekayasa Perangkat Lunak"),
    Student("5053251008", "Luqman", "Rekayasa Perangkat Lunak"),
    Student("50252510309", "Rusdi", "Teknik Informatika")
)


// Warna-warna
val Biru = Color(0xFF1E6FE8)
val Merah = Color(0xFFE53935)
val MerahMuda = Color(0xFFFDE4E4)
val AbuTeks = Color(0xFF757575)
val AbuLatar = Color(0xFFE0E0E0)
val AbuTopBar = Color(0xFFE8EAF0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentManagerTheme {
                StudentManagerApp()
            }
        }
    }
}

@Composable
fun StudentManagerApp() {
    val navController = rememberNavController()
    var students by remember { mutableStateOf(dataAwal) }

    NavHost(navController = navController, startDestination = "home") {

        // Daftar Mahasiswa + Pencarian
        composable("home") {
            HomeScreen(
                students = students,
                onAddClick = { navController.navigate("tambah") },
                onEditClick = { student -> navController.navigate("edit/${student.nrp}") },
                onDelete = { student ->
                    students = students.filter { it.nrp != student.nrp }
                }
            )
        }

        // Form Tambah Mahasiswa
        composable("tambah") {
            StudentFormScreen(
                title = "Tambah Mahasiswa",
                initial = null,
                existingNims = students.map { it.nrp },
                onBack = { navController.popBackStack() },
                onSave = { newStudent ->
                    students = students + newStudent
                    navController.popBackStack()
                }
            )
        }

        // Form Edit Mahasiswa
        composable("edit/{nrp}") { backStackEntry ->
            val nrp = backStackEntry.arguments?.getString("nrp")
            val student = remember { students.find { it.nrp == nrp } }

            if (student != null) {
                StudentFormScreen(
                    title = "Edit Mahasiswa",
                    initial = student,
                    existingNims = students.map { it.nrp }.filter { it != student.nrp },
                    onBack = { navController.popBackStack() },
                    onSave = { updated ->
                        students = students.map { if (it.nrp == student.nrp) updated else it }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}


// Homescreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    students: List<Student>,
    onAddClick: () -> Unit,
    onEditClick: (Student) -> Unit,
    onDelete: (Student) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    // Filter berdasarkan nama, NRP, atau program studi
    val filtered = students.filter {
        it.nama.contains(keyword, ignoreCase = true) ||
                it.nrp.contains(keyword, ignoreCase = true) ||
                it.prodi.contains(keyword, ignoreCase = true)
    }

    // Box dipakai agar dialog konfirmasi bisa menumpuk di atas seluruh layar
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Student Manager", fontWeight = FontWeight.Medium) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AbuTopBar)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddClick,
                    shape = CircleShape,
                    containerColor = Biru,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Mahasiswa")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Kolom pencarian
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    placeholder = { Text("Cari mahasiswa...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    trailingIcon = {
                        if (keyword.isNotEmpty()) {
                            IconButton(onClick = { keyword = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                Text(
                    text = "Jumlah mahasiswa: ${filtered.size}",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                when {
                    students.isEmpty() -> EmptyState()
                    filtered.isEmpty() -> {
                        Text(
                            text = "Mahasiswa dengan kata kunci \"$keyword\" tidak ditemukan.",
                            color = AbuTeks,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(filtered) { student ->
                                StudentItem(
                                    student = student,
                                    onEdit = { onEditClick(student) },
                                    onDelete = { studentToDelete = student }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog konfirmasi hapus (muncul di atas layar)
        studentToDelete?.let { student ->
            DeleteConfirmDialog(
                student = student,
                onCancel = { studentToDelete = null },
                onConfirm = {
                    onDelete(student)
                    studentToDelete = null
                }
            )
        }
    }
}

// Satu kartu mahasiswa di dalam daftar
@Composable
fun StudentItem(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AbuLatar)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AbuTeks,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informasi mahasiswa
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "NRP: ${student.nrp}", fontSize = 13.sp, color = AbuTeks)
                Text(text = student.prodi, fontSize = 13.sp, color = AbuTeks)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Merah)
            }
        }
    }
}

// Tampilan ketika belum ada data
@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = AbuTeks,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Belum ada data mahasiswa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            text = "Tekan tombol + untuk\nmenambahkan mahasiswa pertama.",
            color = AbuTeks,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Konfirmasi hapus. Box gelap transparan + Card di tengah
@Composable
fun DeleteConfirmDialog(
    student: Student,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onCancel() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MerahMuda)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Merah,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Hapus Mahasiswa?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apakah Anda yakin ingin menghapus data mahasiswa ini?",
                    color = AbuTeks,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${student.nama} (${student.nrp})",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Merah),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hapus")
                    }
                }
            }
        }
    }
}

// Form Tambah/Edit Mahasiswa (+ Dropdown Program Studi)
// initial == null  -> mode Tambah
// initial != null  -> mode Edit (field terisi data lama)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    title: String,
    initial: Student?,
    existingNims: List<String>,
    onBack: () -> Unit,
    onSave: (Student) -> Unit
) {
    var nrp by remember { mutableStateOf(initial?.nrp ?: "") }
    var nama by remember { mutableStateOf(initial?.nama ?: "") }
    var prodi by remember { mutableStateOf(initial?.prodi ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AbuTopBar)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // NRP
            Text(text = "NRP", fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = nrp,
                onValueChange = { nrp = it },
                placeholder = { Text("Masukkan NRP") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nama
            Text(text = "Nama", fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                placeholder = { Text("Masukkan nama mahasiswa") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Program Studi (Dropdown)
            Text(text = "Program Studi", fontWeight = FontWeight.Medium)
            Box(modifier = Modifier.padding(top = 4.dp)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (prodi.isEmpty()) "Pilih program studi" else prodi,
                        color = if (prodi.isEmpty()) AbuTeks else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih",
                        tint = AbuTeks
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    daftarProdi.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                prodi = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Pesan error validasi
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Merah,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // Mendorong tombol ke bagian bawah
            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        errorMessage = when {
                            nrp.isBlank() || nama.isBlank() || prodi.isEmpty() ->
                                "Semua data wajib diisi."
                            nrp.trim() in existingNims ->
                                "NRP sudah terdaftar."
                            else -> ""
                        }
                        if (errorMessage.isEmpty()) {
                            onSave(Student(nrp.trim(), nama.trim(), prodi))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Biru),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}


// Previews
@Preview(showBackground = true, showSystemUi = true, name = "Daftar Mahasiswa")
@Composable
fun HomeScreenPreview() {
    StudentManagerTheme {
        HomeScreen(students = dataAwal, onAddClick = {}, onEditClick = {}, onDelete = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Empty State")
@Composable
fun EmptyStatePreview() {
    StudentManagerTheme {
        HomeScreen(students = emptyList(), onAddClick = {}, onEditClick = {}, onDelete = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Form Tambah")
@Composable
fun FormTambahPreview() {
    StudentManagerTheme {
        StudentFormScreen(
            title = "Tambah Mahasiswa",
            initial = null,
            existingNims = emptyList(),
            onBack = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Form Edit")
@Composable
fun FormEditPreview() {
    StudentManagerTheme {
        StudentFormScreen(
            title = "Edit Mahasiswa",
            initial = dataAwal[0],
            existingNims = emptyList(),
            onBack = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Konfirmasi Hapus")
@Composable
fun DeleteDialogPreview() {
    StudentManagerTheme {
        DeleteConfirmDialog(student = dataAwal[0], onCancel = {}, onConfirm = {})
    }
}