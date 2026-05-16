package com.navrotskyi.trippyapp.ui.screens.journeys

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.navrotskyi.trippyapp.models.TripPostDto
import com.navrotskyi.trippyapp.ui.components.OfflineBanner
import com.navrotskyi.trippyapp.ui.components.ShimmerBox
import com.navrotskyi.trippyapp.ui.components.TripNodeSkeleton
import com.navrotskyi.trippyapp.ui.viewmodels.CreatePostState
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNodeDetailsScreen(
    tripId: String,
    nodeId: String,
    viewModel: TripViewModel,
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val node by viewModel.selectedNode.collectAsState()
    val currentUser by profileViewModel.user.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()

    val context = LocalContext.current

    var showAddPostDialog by remember { mutableStateOf(false) }
    var postNote by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> selectedPhotoUris = uris }

    LaunchedEffect(nodeId) {
        viewModel.loadNode(tripId, nodeId)
        viewModel.loadParticipants(tripId)
        viewModel.loadPosts(nodeId)
    }

    LaunchedEffect(createPostState) {
        when (val state = createPostState) {
            is CreatePostState.Success -> {
                Toast.makeText(context, "Post dodany!", Toast.LENGTH_SHORT).show()
                showAddPostDialog = false
                postNote = ""
                selectedPhotoUris = emptyList()
                viewModel.resetCreatePostState()
            }
            is CreatePostState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreatePostState()
            }
            else -> {}
        }
    }

    val currentUserName = currentUser?.name ?: ""
    val reporterName = node?.reporterName ?: "Nieznany"
    val amIOrganizer = participants.find { it.userName == currentUserName }?.tripRole?.equals("ORGANIZER", true) == true
    val canModify = (currentUserName == reporterName) || amIOrganizer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły wydarzenia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (canModify && node != null) {
                        IconButton(onClick = { onEditClick(node!!.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                        IconButton(onClick = {
                            viewModel.deleteTripNode(tripId, nodeId)
                            onBackClick()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPostDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj post")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OfflineBanner(
                isOffline = !isOnline,
                isSyncing = isSyncing,
                pendingCount = pendingCount
            )

            val safeNode = node
            if (safeNode == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(72.dp), cornerRadius = 16)
                    Spacer(Modifier.height(24.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(32.dp))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShimmerBox(modifier = Modifier.weight(1f).height(80.dp), cornerRadius = 12)
                        ShimmerBox(modifier = Modifier.weight(1f).height(80.dp), cornerRadius = 12)
                    }
                    Spacer(Modifier.height(12.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp), cornerRadius = 12)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Reporter", style = MaterialTheme.typography.labelSmall)
                                Text(reporterName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val role = participants.find { it.userName == reporterName }?.tripRole ?: "Uczestnik"
                                Text(
                                    text = role.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = safeNode.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NodeInfoTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Schedule,
                            label = "Czas",
                            value = formatTimeRangeDetails(safeNode.startTime, safeNode.endTime)
                        )
                        NodeInfoTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AttachMoney,
                            label = "Koszt",
                            value = "${safeNode.price} PLN"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val categoryText = if (safeNode.category.isNullOrBlank()) "Brak podanego typu" else safeNode.category!!
                    NodeInfoTile(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Label,
                        label = "Typ / Lokalizacja",
                        value = categoryText
                    )

                    if (safeNode.separate) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.width(8.dp))
                                Text("Koszty Rozdzielne - każdy płaci za siebie", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    if (!safeNode.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Notatka", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = safeNode.note!!,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Posty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (posts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Brak postów. Bądź pierwszym, który skomentuje!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            posts.forEach { post -> PostCard(post = post) }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddPostDialog) {
        AlertDialog(
            onDismissRequest = {
                if (createPostState !is CreatePostState.Loading) {
                    showAddPostDialog = false
                }
            },
            title = { Text("Nowy post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = postNote,
                        onValueChange = { postNote = it },
                        label = { Text("Treść posta") },
                        placeholder = { Text("Co się wydarzyło?") },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (selectedPhotoUris.isEmpty()) "Wybierz zdjęcia"
                            else "Wybrano: ${selectedPhotoUris.size} zdjęcie(a)"
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postNote.isNotBlank()) {
                            viewModel.createPost(nodeId, postNote, selectedPhotoUris, context)
                        }
                    },
                    enabled = postNote.isNotBlank() && createPostState !is CreatePostState.Loading
                ) {
                    if (createPostState is CreatePostState.Loading) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Dodaj")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddPostDialog = false },
                    enabled = createPostState !is CreatePostState.Loading
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
private fun PostCard(post: TripPostDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = post.reporterName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(post.note, style = MaterialTheme.typography.bodyMedium)
            if (post.photos.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(post.photos) { photo ->
                        val imageUrl = photo.photoUrl?.replace("localhost", "10.0.2.2")
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NodeInfoTile(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

fun formatTimeRangeDetails(start: String?, end: String?): String {
    if (start.isNullOrBlank() || end.isNullOrBlank()) return "Brak czasu"
    return try {
        val sTime = start.substring(11, 16)
        val sDate = "${start.substring(8, 10)}.${start.substring(5, 7)}"
        val eTime = end.substring(11, 16)
        val eDate = "${end.substring(8, 10)}.${end.substring(5, 7)}"

        if (sDate == eDate) "$sDate\n$sTime - $eTime"
        else "$sDate $sTime\n$eDate $eTime"
    } catch (e: Exception) {
        val cleanStart = start.replace("T", " ").take(16)
        val cleanEnd = end.replace("T", " ").take(16)
        "$cleanStart\n$cleanEnd"
    }
}