package com.example.dailydoodle.ui.screen.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.util.Analytics

/**
 * Shop product data class per PRD IAP items.
 */
data class ShopProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceInr: String,
    val priceUsd: String,
    val icon: String,
    val isPurchased: Boolean = false
)

/**
 * Shop screen with IAP product listings.
 * Per PRD IAP items:
 * - Brush pack 1 (Basic brushes) — INR 49 / $0.99
 * - Brush pack 2 (Themed brushes) — INR 149 / $2.99
 * - Remove Ads (1 month) — INR 199 / $2.99
 * - Premium feature: high-res export — INR 249 / $3.99
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBackClick: () -> Unit
) {
    // In real implementation, these would come from Google Play Billing
    val products = remember {
        listOf(
            ShopProduct(
                id = "brush_pack_basic",
                name = "Basic Brush Pack",
                description = "Unlock 5 new brush styles for all your doodles",
                priceInr = "₹49",
                priceUsd = "$0.99",
                icon = "🖌️"
            ),
            ShopProduct(
                id = "brush_pack_themed",
                name = "Themed Brush Pack",
                description = "Special brushes: Neon, Watercolor, Chalk, and more!",
                priceInr = "₹149",
                priceUsd = "$2.99",
                icon = "🎨"
            ),
            ShopProduct(
                id = "remove_ads_monthly",
                name = "Remove Ads (1 Month)",
                description = "Enjoy an ad-free experience for 30 days",
                priceInr = "₹199",
                priceUsd = "$2.99",
                icon = "🚫"
            ),
            ShopProduct(
                id = "high_res_export",
                name = "High-Res Export",
                description = "Export your chains in high resolution for printing",
                priceInr = "₹249",
                priceUsd = "$3.99",
                icon = "📸"
            )
        )
    }
    
    var purchasedProducts by remember { mutableStateOf(setOf<String>()) }
    var isProcessing by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf<ShopProduct?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Premium Features",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(products) { product ->
                ProductCard(
                    product = product.copy(isPurchased = purchasedProducts.contains(product.id)),
                    onPurchaseClick = {
                        showPurchaseDialog = product
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "💡 Tip: Watch rewarded ads to unlock temporary premium features for free!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
    
    // Purchase confirmation dialog
    showPurchaseDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = null },
            title = { Text("Purchase ${product.name}") },
            text = {
                Column {
                    Text("${product.description}\n\nPrice: ${product.priceUsd} (${product.priceInr})")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: This is a demo. In production, this would connect to Google Play Billing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Simulate purchase
                        purchasedProducts = purchasedProducts + product.id
                        Analytics.logPurchaseMade(product.id)
                        
                        // If removing ads, update AdMobManager
                        if (product.id == "remove_ads_monthly") {
                            AdMobManager.setAdFreeUser(true)
                        }
                        
                        showPurchaseDialog = null
                    }
                ) {
                    Text("Buy Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductCard(
    product: ShopProduct,
    onPurchaseClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = product.icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Price/Button
            if (product.isPurchased) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Purchased",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Owned",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Button(
                    onClick = onPurchaseClick
                ) {
                    Text(product.priceUsd)
                }
            }
        }
    }
}
