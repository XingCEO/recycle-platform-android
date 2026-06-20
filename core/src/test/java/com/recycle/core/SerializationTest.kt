package com.recycle.core

import com.recycle.core.model.AwardResult
import com.recycle.core.model.Category
import com.recycle.core.model.Profile
import com.recycle.core.model.RedeemResult
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun parsesAwardResult() {
        val s = """{"status":"ok","category":"pet","points_awarded":10,"new_balance":110,"record_id":"abc"}"""
        val a = json.decodeFromString(AwardResult.serializer(), s)
        assertEquals("ok", a.status)
        assertEquals(10, a.points_awarded)
        assertEquals(110, a.new_balance)
        assertEquals("pet", a.category)
    }

    @Test fun parsesRejectedJunkAward() {
        val s = """{"status":"rejected_junk","category":"pet","points_awarded":0,"new_balance":100}"""
        val a = json.decodeFromString(AwardResult.serializer(), s)
        assertEquals("rejected_junk", a.status)
        assertEquals(0, a.points_awarded)
        assertNull(a.record_id)
    }

    @Test fun parsesRedeemResult() {
        val s = """{"code":"AB12CD34EF56","item":"原子筆","cost":50,"new_balance":50,"redemption_id":"r1"}"""
        val r = json.decodeFromString(RedeemResult.serializer(), s)
        assertEquals("AB12CD34EF56", r.code)
        assertEquals(50, r.cost)
    }

    @Test fun profileIgnoresUnknownFields() {
        val s = """{"id":"u1","role":"user","display_name":"小明","points":5,"server_extra":"x"}"""
        val p = json.decodeFromString(Profile.serializer(), s)
        assertEquals(5, p.points)
        assertEquals("user", p.role)
    }

    @Test fun categoryLabels() {
        assertEquals("寶特瓶", Category.label("pet"))
        assertEquals("鐵鋁罐", Category.label("can"))
        assertEquals("鋁箔包", Category.label("carton"))
    }
}
