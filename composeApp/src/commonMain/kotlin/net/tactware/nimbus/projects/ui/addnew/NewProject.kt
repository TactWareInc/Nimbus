package net.tactware.nimbus.projects.ui.addnew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProject() {
    val viewModel = koinViewModel<NewProjectViewModel>()
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(.75f)
        ) {
            TextField(
                value = viewModel.projectUrl,
                onValueChange = { viewModel.onInteraction(NewProjectInteractions.UrlProject(it)) },
                label = { Text("Project URL") },
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.default)
            )
            Row(Modifier.fillMaxWidth().padding(MaterialTheme.spacing.default)) {
                TextField(
                    value = viewModel.projectLocalName,
                    onValueChange = { viewModel.onInteraction(NewProjectInteractions.NameProject(it)) },
                    label = { Text("Project Name") },
                    modifier = Modifier.weight(1f).padding(end = MaterialTheme.spacing.default)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Is Azure DevOps Server?", style = MaterialTheme.typography.labelSmall)
                    // This is a simplified version of the actual code
                    // The actual code uses a radio button to select between server and service
                    // This is just a placeholder
                    Switch(
                        checked = viewModel.isDevOpsServerOrService == DevOpsServerOrService.SERVER,
                        onCheckedChange = {
                            viewModel.onInteraction(NewProjectInteractions.SetIsServerOrService(if (it) DevOpsServerOrService.SERVER else DevOpsServerOrService.SERVICE))
                        }
                    )
                }
            }
            TextField(
                value = viewModel.personalAccessToken,
                onValueChange = { viewModel.onInteraction(NewProjectInteractions.PAT(it)) },
                label = { Text("Personal Access Token") },
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.default)
            )
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {  },
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.default),
            ) {
                TextField(
                    // The `menuAnchor` modifier must be passed to the text field for correctness.
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                    readOnly = true,
                    value = viewModel.process.displayName,
                    onValueChange = {},
                    label = { Text("Process Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    enabled = false,
                    supportingText = {
                        Text("Only Agile is supported at this time")
                    }
                )
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = {  },
                ) {
                    NewProjectViewModel.ProcessType.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(it.displayName) },
                            onClick = {  },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.onInteraction(NewProjectInteractions.SaveProject) },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.saveAccessible.value
            ) {
                Text("Save")
            }
        }
    }
}