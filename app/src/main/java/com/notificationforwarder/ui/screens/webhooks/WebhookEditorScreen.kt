package com.notificationforwarder.ui.screens.webhooks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.model.NotificationData
import com.notificationforwarder.ui.viewmodel.WebhookEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebhookEditorScreen(
    webhookId: Long?,
    navController: NavController,
    viewModel: WebhookEditorViewModel = hiltViewModel()
) {
    val url by viewModel.url.collectAsStateWithLifecycle()
    val headers by viewModel.headers.collectAsStateWithLifecycle()
    val triggerRules by viewModel.triggerRules.collectAsStateWithLifecycle()
    val currentRule by viewModel.currentRule.collectAsStateWithLifecycle()
    val activeNotifications by viewModel.activeNotifications.collectAsStateWithLifecycle()
    val matchingNotifications by viewModel.matchingNotifications.collectAsStateWithLifecycle()

    var showHeaderDialog by remember { mutableStateOf(false) }
    var showRuleDialog by remember { mutableStateOf(false) }
    var ruleToDelete by remember { mutableStateOf<TriggerRule?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (webhookId == null) stringResource(R.string.add_webhook)
                        else stringResource(R.string.edit_webhook)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (viewModel.isValidUrl()) {
                                viewModel.saveWebhook {
                                    navController.navigateUp()
                                }
                            } else {
                                urlError = "Invalid URL"
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
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
            // Webhook URL
            OutlinedTextField(
                value = url,
                onValueChange = {
                    viewModel.updateUrl(it)
                    urlError = null
                },
                label = { Text(stringResource(R.string.webhook_url)) },
                placeholder = { Text(stringResource(R.string.webhook_url_hint)) },
                isError = urlError != null,
                supportingText = urlError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Custom Headers
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Headers",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { showHeaderDialog = true }) {
                            Text(stringResource(R.string.add_header))
                        }
                    }

                    if (headers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        headers.forEach { (name, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.removeHeader(name) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Trigger Rules Section
            TriggerRulesListSection(
                triggerRules = triggerRules,
                onAddRule = { 
                    viewModel.addTriggerRule()
                    showRuleDialog = true
                },
                onEditRule = { rule ->
                    viewModel.updateCurrentRule(rule)
                    showRuleDialog = true
                },
                onToggleRule = { viewModel.toggleTriggerRule(it) },
                onDeleteRule = { ruleToDelete = it }
            )

            // Live Preview Section
            LivePreviewSection(
                activeNotifications = activeNotifications,
                matchingNotifications = matchingNotifications,
                onRefresh = { viewModel.refreshActiveNotifications() }
            )
        }
    }

    // Add Header Dialog
    if (showHeaderDialog) {
        AddHeaderDialog(
            onDismiss = { showHeaderDialog = false },
            onAdd = { name, value ->
                viewModel.addHeader(name, value)
                showHeaderDialog = false
            }
        )
    }

    // Add/Edit Rule Dialog
    if (showRuleDialog) {
        currentRule?.let { rule ->
            EditTriggerRuleDialog(
                rule = rule,
                onDismiss = {
                    viewModel.cancelEditRule()
                    showRuleDialog = false
                },
                onSave = { updatedRule ->
                    viewModel.updateCurrentRule(updatedRule)
                    viewModel.saveCurrentRule()
                    showRuleDialog = false
                }
            )
        }
    }

    // Delete Rule Confirmation
    ruleToDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text("Delete this trigger rule?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTriggerRule(rule)
                        ruleToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { ruleToDelete = null }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}

@Composable
fun TriggerRulesListSection(
    triggerRules: List<TriggerRule>,
    onAddRule: () -> Unit,
    onEditRule: (TriggerRule) -> Unit,
    onToggleRule: (TriggerRule) -> Unit,
    onDeleteRule: (TriggerRule) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.trigger_rules),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onAddRule) {
                    Text("Add Rule")
                }
            }

            if (triggerRules.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No trigger rules. Add one to filter notifications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                triggerRules.forEach { rule ->
                    TriggerRuleCard(
                        rule = rule,
                        onEdit = { onEditRule(rule) },
                        onToggle = { onToggleRule(rule) },
                        onDelete = { onDeleteRule(rule) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TriggerRuleCard(
    rule: TriggerRule,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (rule.enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (rule.packageNamePattern != null) {
                    Text(
                        text = "Package: ${rule.packageNamePattern}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (rule.contentPattern != null) {
                    Text(
                        text = "Content: ${rule.contentPattern} ${if (rule.useRegex) "(regex)" else "(text)"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "Priority: ${rule.minPriority} to ${rule.maxPriority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Refresh, contentDescription = "Edit")
                }
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EditTriggerRuleDialog(
    rule: TriggerRule,
    onDismiss: () -> Unit,
    onSave: (TriggerRule) -> Unit
) {
    var editedRule by remember { mutableStateOf(rule) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rule.id == 0L) "Add Trigger Rule" else "Edit Trigger Rule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = editedRule.packageNamePattern ?: "",
                    onValueChange = {
                        editedRule = editedRule.copy(packageNamePattern = it.takeIf { s -> s.isNotBlank() })
                    },
                    label = { Text(stringResource(R.string.package_filter)) },
                    placeholder = { Text("e.g., com.example") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = editedRule.contentPattern ?: "",
                    onValueChange = {
                        editedRule = editedRule.copy(contentPattern = it.takeIf { s -> s.isNotBlank() })
                    },
                    label = { Text(stringResource(R.string.content_filter)) },
                    placeholder = { Text(if (editedRule.useRegex) "e.g., .*urgent.*" else "e.g., urgent") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.use_regex),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = editedRule.useRegex,
                        onCheckedChange = { editedRule = editedRule.copy(useRegex = it) }
                    )
                }

                Text(
                    text = "Priority Range",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedRule.minPriority.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { priority ->
                                editedRule = editedRule.copy(minPriority = priority)
                            }
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = editedRule.maxPriority.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { priority ->
                                editedRule = editedRule.copy(maxPriority = priority)
                            }
                        },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(editedRule) }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LivePreviewSection(
    activeNotifications: List<NotificationData>,
    matchingNotifications: List<NotificationData>,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.live_preview),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${matchingNotifications.size} of ${activeNotifications.size} notifications match",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (matchingNotifications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                matchingNotifications.take(5).forEach { notification ->
                    NotificationPreviewCard(notification)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (matchingNotifications.size > 5) {
                    Text(
                        text = "... and ${matchingNotifications.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (activeNotifications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.no_matching_notifications),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No active notifications",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NotificationPreviewCard(notification: NotificationData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = notification.appName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (notification.title != null) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (notification.text != null) {
                Text(
                    text = notification.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddHeaderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var headerName by remember { mutableStateOf("") }
    var headerValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_header)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = headerName,
                    onValueChange = { headerName = it },
                    label = { Text(stringResource(R.string.header_name)) },
                    placeholder = { Text("Authorization") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = headerValue,
                    onValueChange = { headerValue = it },
                    label = { Text(stringResource(R.string.header_value)) },
                    placeholder = { Text("Bearer token...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (headerName.isNotBlank() && headerValue.isNotBlank()) {
                        onAdd(headerName, headerValue)
                    }
                },
                enabled = headerName.isNotBlank() && headerValue.isNotBlank()
            ) {
                Text(stringResource(R.string.add_header))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

