package com.notificationforwarder.webhook

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Test

class WebhookPayloadTest {

    private val gson = Gson()

    @Test
    fun `webhook payload serializes correctly`() {
        val payload = WebhookPayload(
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            text = "Test Text",
            subText = "Sub Text",
            bigText = "Big Text",
            priority = 1,
            timestamp = 1234567890000,
            iconBase64 = "base64encodedicon",
            device = DeviceInfo(
                androidVersion = "13",
                deviceModel = "Pixel 7",
                deviceManufacturer = "Google"
            )
        )

        val json = gson.toJson(payload)

        assertThat(json).contains("\"packageName\":\"com.example.app\"")
        assertThat(json).contains("\"appName\":\"Example App\"")
        assertThat(json).contains("\"title\":\"Test Title\"")
        assertThat(json).contains("\"iconBase64\":\"base64encodedicon\"")
        assertThat(json).contains("\"androidVersion\":\"13\"")
    }

    @Test
    fun `webhook payload deserializes correctly`() {
        val json = """
            {
                "packageName": "com.test",
                "appName": "Test",
                "title": "Title",
                "text": "Text",
                "subText": null,
                "bigText": null,
                "priority": 0,
                "timestamp": 1234567890,
                "iconBase64": "icon123",
                "device": {
                    "androidVersion": "11",
                    "deviceModel": "Test Device",
                    "deviceManufacturer": "Test"
                }
            }
        """.trimIndent()

        val payload = gson.fromJson(json, WebhookPayload::class.java)

        assertThat(payload.packageName).isEqualTo("com.test")
        assertThat(payload.appName).isEqualTo("Test")
        assertThat(payload.iconBase64).isEqualTo("icon123")
        assertThat(payload.device.androidVersion).isEqualTo("11")
    }

    @Test
    fun `webhook payload handles null values`() {
        val payload = WebhookPayload(
            packageName = "com.test",
            appName = "Test",
            title = null,
            text = null,
            subText = null,
            bigText = null,
            priority = 0,
            timestamp = 0,
            iconBase64 = null,
            device = DeviceInfo("11", "Device", "Manufacturer")
        )

        val json = gson.toJson(payload)
        val deserialized = gson.fromJson(json, WebhookPayload::class.java)

        assertThat(deserialized.title).isNull()
        assertThat(deserialized.text).isNull()
        assertThat(deserialized.iconBase64).isNull()
    }
}

