package com.recycle.core.model

import kotlinx.serialization.Serializable

// Material category codes shared across the platform.
object Category {
    const val PET = "pet"        // 寶特瓶
    const val CAN = "can"        // 鐵鋁罐
    const val CARTON = "carton"  // 鋁箔包
    val ALL = listOf(PET, CAN, CARTON)
    fun label(code: String): String = when (code) {
        PET -> "寶特瓶"; CAN -> "鐵鋁罐"; CARTON -> "鋁箔包"; else -> code
    }
}

@Serializable
data class Profile(
    val id: String,
    val role: String = "user",
    val display_name: String = "",
    val points: Int = 0,
    val created_at: String? = null,
)

@Serializable
data class Barcode(
    val barcode: String,
    val name: String = "",
    val category: String = "",
    val points: Int = 0,
    val is_junk: Boolean = false,
    val created_at: String? = null,
)

@Serializable
data class StoreItem(
    val id: String,
    val name: String,
    val cost_points: Int,
    val stock: Int = 0,
    val image_url: String? = null,
    val is_active: Boolean = true,
)

@Serializable
data class RecycleRecord(
    val id: String,
    val user_id: String,
    val vendor_id: String,
    val barcode: String? = null,
    val category: String? = null,
    val method: String,
    val ai_confidence: Double? = null,
    val points_awarded: Int = 0,
    val status: String,
    val created_at: String,
)

@Serializable
data class Redemption(
    val id: String,
    val user_id: String,
    val item_id: String,
    val code: String,
    val cost_points: Int,
    val status: String,
    val created_at: String,
    val used_at: String? = null,
)

// ---- RPC results -----------------------------------------------------------

@Serializable
data class AwardResult(
    val status: String,            // "ok" | "rejected_junk"
    val category: String? = null,
    val points_awarded: Int = 0,
    val new_balance: Int = 0,
    val record_id: String? = null,
)

@Serializable
data class RedeemResult(
    val code: String,
    val item: String,
    val cost: Int,
    val new_balance: Int,
    val redemption_id: String,
)

@Serializable
data class MarkUsedResult(
    val status: String,
    val item: String? = null,
    val user_id: String? = null,
)

// ---- AI classification -----------------------------------------------------

@Serializable
data class AiClassifyResult(val category: String, val confidence: Double)

// ---- Auth ------------------------------------------------------------------

@Serializable
data class AuthUser(val id: String, val email: String? = null)

@Serializable
data class AuthResponse(
    val access_token: String,
    val refresh_token: String? = null,
    val expires_in: Int = 3600,
    val user: AuthUser,
)
