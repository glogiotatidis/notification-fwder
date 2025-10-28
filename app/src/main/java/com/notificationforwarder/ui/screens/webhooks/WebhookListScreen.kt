package com.notificationforwarder.ui.screens.webhooks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.notificationforwarder.R
import com.notificationforwarder.data.entity.WebhookConfig
import com.notificationforwarder.ui.viewmodel.WebhookListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebhookListScreen(
    navController: NavController,
    viewModel: WebhookListViewModel = hiltViewModel()
) {
    val webhooks by viewModel.webhooks.collectAsStateWithLifecycle()
    var webhookToDelete by remember { mutableStateOf<WebhookConfig?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Webhooks") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("webhook/new") }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_webhook))
            }
        }
    ) { paddingValues ->
        if (webhooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No webhooks configured\nTap + to add one",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(webhooks, key = { it.id }) { webhook ->
                    WebhookCard(
                        webhook = webhook,
                        onClick = { navController.navigate("webhook/edit/${webhook.id}") },
                        onToggle = { viewModel.toggleWebhook(webhook) },
                        onDelete = { webhookToDelete = webhook }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    webhookToDelete?.let { webhook ->
        AlertDialog(
            onDismissRequest = { webhookToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.confirm_delete)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWebhook(webhook)
                        webhookToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { webhookToDelete = null }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}

@Composable
fun WebhookCard(
    webhook: WebhookConfig,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = webhook.url,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (webhook.headers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${webhook.headers.size} custom headers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = webhook.enabled,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

