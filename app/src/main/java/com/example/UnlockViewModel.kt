package com.example

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChecklistStep
import com.example.data.DeviceTimer
import com.example.data.UnlockDatabase
import com.example.data.UnlockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class UnlockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UnlockRepository

    init {
        val database = UnlockDatabase.getDatabase(application)
        repository = UnlockRepository(database.unlockDao())
    }

    // Expose local countdown timers
    val timers: StateFlow<List<DeviceTimer>> = repository.allTimers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose checklist step flows
    private val dbSteps = repository.allChecklistSteps

    // Static checklist schema definitions
    val checklistSchema = listOf(
        ChecklistSchemaItem(
            key = "SIM_CARD",
            title = "Insert Active SIM Card",
            description = "You must use mobile data from your active SIM card to bind the account. Turn off Wi-Fi."
        ),
        ChecklistSchemaItem(
            key = "MI_ACCOUNT",
            title = "Log in to Mi Account",
            description = "Go to Settings -> Mi Account and sign in. On HyperOS, your account should be older than 30 days."
        ),
        ChecklistSchemaItem(
            key = "COMMUNITY_APP",
            title = "Apply in Mi Community App (HyperOS Only)",
            description = "Open official Mi Community app -> Profile -> Unlock Bootloader, and tap Apply to request unlock permission."
        ),
        ChecklistSchemaItem(
            key = "DEVELOPER_OPTIONS",
            title = "Enable Developer Options",
            description = "Go to Settings -> About Phone, and tap 'OS version' or 'MIUI version' 7 times until you see a success toast."
        ),
        ChecklistSchemaItem(
            key = "OEM_UNLOCK",
            title = "Turn on OEM Unlocking",
            description = "Go to Settings -> Additional Settings -> Developer Options, and toggle 'OEM Unlocking' to YES."
        ),
        ChecklistSchemaItem(
            key = "USB_DEBUG",
            title = "Enable USB Debugging",
            description = "Go to Additional Settings -> Developer Options, and toggle 'USB Debugging' ON to allow PC connection."
        ),
        ChecklistSchemaItem(
            key = "BIND_ACCOUNT",
            title = "Bind Account in Mi Unlock Status",
            description = "Go to Developer Options -> Mi Unlock Status. Turn off Wi-Fi, turn on SIM mobile data, and tap 'Add account and device'."
        ),
        ChecklistSchemaItem(
            key = "PC_WAIT",
            title = "Wait 168 Hours (7 Days)",
            description = "Keep your active timer below. Avoid logging out of Mi Account or factory resetting your device, or the timer resets."
        )
    )

    // Combined state for the checklist progress
    val checklistProgress: StateFlow<Map<String, Boolean>> = dbSteps
        .combine(MutableStateFlow(checklistSchema)) { list, schema ->
            val progressMap = schema.associate { it.key to false }.toMutableMap()
            list.forEach { step ->
                progressMap[step.stepKey] = step.isCompleted
            }
            progressMap
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun toggleChecklistStep(stepKey: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setStepCompleted(stepKey, isCompleted)
        }
    }

    fun addTimer(deviceName: String, miAccount: String, hours: Int, daysAgo: Double) {
        viewModelScope.launch {
            val offsetMillis = (daysAgo * 24 * 60 * 60 * 1000).toLong()
            val bindTime = System.currentTimeMillis() - offsetMillis
            val name = deviceName.ifBlank { "Redmi/POCO Device" }
            val account = miAccount.ifBlank { "Unspecified Account" }
            val newTimer = DeviceTimer(
                deviceName = name,
                miAccount = account,
                bindTimeMillis = bindTime,
                durationHours = hours
            )
            repository.insertTimer(newTimer)
        }
    }

    fun deleteTimer(timer: DeviceTimer) {
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }

    // Get Device Specifications to see if running on Xiaomi family
    fun getDeviceSpecs(): DeviceSpecs {
        val manufacturer = (Build.MANUFACTURER ?: "Unknown").trim()
        val isXiaomiFamily = manufacturer.lowercase(Locale.ROOT).contains("xiaomi") ||
                manufacturer.lowercase(Locale.ROOT).contains("redmi") ||
                manufacturer.lowercase(Locale.ROOT).contains("poco")

        val displayBrand = when {
            manufacturer.lowercase(Locale.ROOT).contains("poco") -> "POCO"
            manufacturer.lowercase(Locale.ROOT).contains("redmi") -> "Redmi"
            else -> manufacturer
        }

        val secPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH ?: "Unknown"
        } else {
            "Unknown"
        }

        return DeviceSpecs(
            manufacturer = manufacturer,
            brand = displayBrand,
            model = Build.MODEL ?: "Unknown",
            deviceName = Build.DEVICE ?: "Unknown",
            product = Build.PRODUCT ?: "Unknown",
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            sdkLevel = Build.VERSION.SDK_INT,
            securityPatch = secPatch,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            isXiaomiFamily = isXiaomiFamily
        )
    }
}

data class ChecklistSchemaItem(
    val key: String,
    val title: String,
    val description: String
)

data class DeviceSpecs(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val deviceName: String,
    val product: String,
    val androidVersion: String,
    val sdkLevel: Int,
    val securityPatch: String,
    val cpuAbi: String,
    val isXiaomiFamily: Boolean
)
