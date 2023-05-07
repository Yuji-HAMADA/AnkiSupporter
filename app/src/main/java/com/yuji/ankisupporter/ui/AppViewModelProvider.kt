package com.yuji.ankisupporter.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuji.ankisupporter.AnkiSupporterApplication
import com.yuji.ankisupporter.ui.home.HomeViewModel
import com.yuji.ankisupporter.ui.item.ItemDetailsViewModel
import com.yuji.ankisupporter.ui.item.ItemEditViewModel
import com.yuji.ankisupporter.ui.item.ItemEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            ItemEditViewModel(
                this.createSavedStateHandle(),
                AnkiSupporterApplication().container.itemsRepository
            )
        }
        // Initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(AnkiSupporterApplication().container.itemsRepository)
        }

        // Initializer for ItemDetailsViewModel
        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                AnkiSupporterApplication().container.itemsRepository
            )
        }

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(AnkiSupporterApplication().container.itemsRepository)
        }
    }
}


/**
 * Extension function to queries for [Application] object and returns an instance of
 * [AnkiSupporterApplication].
 */
fun CreationExtras.AnkiSupporterApplication(): AnkiSupporterApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AnkiSupporterApplication)
