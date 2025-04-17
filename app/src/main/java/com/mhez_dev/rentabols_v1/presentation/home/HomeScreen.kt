package com.mhez_dev.rentabols_v1.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhez_dev.rentabols_v1.R
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.ui.components.CategoryDropdown
import com.mhez_dev.rentabols_v1.ui.components.ItemCategory
import com.mhez_dev.rentabols_v1.ui.components.RentabolsBottomNavigation
import com.mhez_dev.rentabols_v1.ui.components.RentalItemCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToItemDetails: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAddItem: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToItems: () -> Unit,
    currentRoute: String,
    onSignOut: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val items: List<RentalItem> by viewModel.items.collectAsState()
    var selectedCategory by remember { mutableStateOf("All Categories") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_rentabols),
                            contentDescription = "Rentabols Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(36.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "Rentabols",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Add Item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bottomBar = {
            RentabolsBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        "home" -> { /* Already on home */ }
                        "items" -> onNavigateToItems()
                        "map" -> onNavigateToMap()
                        "profile" -> onNavigateToProfile()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and Filter Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchItems(query = it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                viewModel.searchItems(null)
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            // Category Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryDropdown(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        viewModel.filterByCategory(category)
                    },
                    modifier = Modifier.weight(1f)
                )
                
                if (selectedCategory != "All Categories" || searchQuery.isNotEmpty()) {
                    Button(
                        onClick = {
                            selectedCategory = "All Categories"
                            searchQuery = ""
                            viewModel.clearFilters()
                        },
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Clear Filters")
                    }
                }
            }

            // Content
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No items found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = items,
                        key = { item: RentalItem -> item.id }
                    ) { item ->
                        RentalItemCard(
                            item = item,
                            onClick = { onNavigateToItemDetails(item.id) }
                        )
                    }
                }
            }
        }
    }
}
