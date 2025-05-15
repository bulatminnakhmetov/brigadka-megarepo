package com.brigadka.app.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brigadka.app.data.api.models.City

@Composable
fun CityPicker(cities: List<City>, selectedCityID: Int?, onCitySelected: (Int) -> Unit) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }
    val currentCityName = remember(selectedCityID, cities) {
        cities.find { it.id == selectedCityID }?.name ?: ""
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentCityName.ifEmpty { citySearchQuery },
            onValueChange = {
                citySearchQuery = it
                isDropdownExpanded = true
            },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                    Icon(
                        imageVector = if (isDropdownExpanded)
                            androidx.compose.material.icons.Icons.Filled.KeyboardArrowUp
                        else
                            androidx.compose.material.icons.Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Toggle dropdown"
                    )
                }
            },
            shape = MaterialTheme.shapes.medium
        )

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            val filteredCities = cities.filter {
                it.name.contains(citySearchQuery, ignoreCase = true)
            }

            filteredCities.forEach { city ->
                DropdownMenuItem(
                    onClick = {
                        onCitySelected(city.id)
                        isDropdownExpanded = false
                    },
                    text = { Text(city.name) }
                )
            }

            if (filteredCities.isEmpty() && cities.isNotEmpty()) {
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Нет подходящих городов") },
                    enabled = false
                )
            }

            if (cities.isEmpty()) {
                DropdownMenuItem(
                    onClick = { },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Загрузка городов...")
                        }
                    },
                    enabled = false
                )
            }
        }
    }
}