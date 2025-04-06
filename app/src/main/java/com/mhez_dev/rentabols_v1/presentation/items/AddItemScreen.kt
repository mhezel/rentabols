package com.mhez_dev.rentabols_v1.presentation.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.auth.FirebaseAuth
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.ui.components.CategoryDropdown
import com.mhez_dev.rentabols_v1.ui.components.MapSelectionDialog
import com.mhez_dev.rentabols_v1.ui.components.RentabolsButton
import com.mhez_dev.rentabols_v1.ui.components.RentabolsTextField
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onItemAdded: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ItemDetailsViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RentabolsTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title"
            )

            RentabolsTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description"
            )

            RentabolsTextField(
                value = pricePerDay,
                onValueChange = { pricePerDay = it },
                label = "Price per Day"
            )

            CategoryDropdown(
                selectedCategory = category,
                onCategorySelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )

            RentabolsTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address"
            )

            // Image Selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { imagePicker.launch("image/*") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedImages.isEmpty()) {
                            "Add Images"
                        } else {
                            "${selectedImages.size} images selected"
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Images"
                    )
                }
            }

            // Location Selection
            var showLocationDialog by remember { mutableStateOf(false) }
            
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showLocationDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedLocation == null) {
                            "Select Location"
                        } else {
                            "Location Selected (${selectedLocation?.latitude?.toFloat()}, ${selectedLocation?.longitude?.toFloat()})"
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Select Location"
                    )
                }
            }

            if (showLocationDialog) {
                MapSelectionDialog(
                    onLocationSelected = { location ->
                        selectedLocation = location
                    },
                    onDismiss = { showLocationDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RentabolsButton(
                text = "Add Item",
                onClick = {
                    selectedLocation?.let { location ->
                        val item = RentalItem(
                            id = "",  // Will be set by Firebase
                            ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            title = title,
                            description = description,
                            category = category,
                            pricePerDay = pricePerDay.toDoubleOrNull() ?: 0.0,
                            location = GeoPoint(location.latitude, location.longitude),
                            address = address,
                            imageUrls = emptyList() // Will be updated after upload
                        )
                        // TODO: Upload images and create item
                        onItemAdded()
                    }
                },
                enabled = title.isNotBlank() &&
                        description.isNotBlank() &&
                        pricePerDay.isNotBlank() &&
                        pricePerDay.toDoubleOrNull() != null &&
                        category.isNotBlank() &&
                        address.isNotBlank() &&
                        selectedLocation != null &&
                        selectedImages.isNotEmpty() &&
                        FirebaseAuth.getInstance().currentUser != null
            )
        }
    }
}
