package qrflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import qrflow.QRFlowApp

@Composable
inline fun <reified VM : ViewModel> appViewModel(noinline builder: (QRFlowApp) -> VM): VM {
    val app = LocalContext.current.applicationContext as QRFlowApp
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = builder(app) as T
        }
    )
}
