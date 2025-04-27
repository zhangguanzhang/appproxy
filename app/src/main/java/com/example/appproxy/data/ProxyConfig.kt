package com.example.appproxy.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proxy_configs")
data class ProxyConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @NonNull
    val name: String,
    @NonNull
    val type: ProxyType,
    val user: String,
    val pass: String,
    val server: String,
    val port: Int,
    @ColumnInfo(defaultValue = "0")
    val selected: Boolean,
)

fun ProxyConfig.toUri(): String {
    return this.type.toUri(this)
}

fun ProxyConfig.toDisplayUri(): String {
    return this.type.toDisplayUri(this)
}

enum class ProxyType(val requiredFields: List<String>) {
    HTTP(
        requiredFields = listOf("server", "port", "user", "pass")
    ) {
        override fun toUri(config: ProxyConfig): String {
            val auth = if (config.user.isNotBlank()) {
                "${config.user}:${config.pass}@"
            } else ""
            val port = if (config.port > 0) {
                ":${config.port}"
            } else ""
            return "http://${auth}${config.server}${port}"
        }
    },
    SOCKS4(
        requiredFields = listOf("server", "port", "user")
    ) {
        override fun toUri(config: ProxyConfig): String {
            val auth = if (config.user.isNotBlank()) {
                "${config.user}@"
            } else ""
            val port = if (config.port > 0) {
                ":${config.port}"
            } else ""
            return with(config) {
                "socks4://${auth}${server}${port}"
            }
        }
    },
    SOCKS5(
        requiredFields = listOf("server", "port", "user", "pass")
    ) {
        override fun toUri(config: ProxyConfig): String {
            val auth = if (config.user.isNotBlank()) {
                "${config.user}:${config.pass}@"
            } else ""
            val port = if (config.port > 0) {
                ":${config.port}"
            } else ""
            return with(config) {
                "socks5://${auth}${server}${port}"
            }
        }
    },
    DIRECT(
        requiredFields = emptyList()
    ) {
        override fun toUri(config: ProxyConfig): String {
            return "direct://"
        }
    },
    REJECT(
        requiredFields = emptyList()
    ) {
        override fun toUri(config: ProxyConfig): String {
            return "reject://"
        }
    },
    RAW(
        requiredFields = listOf("server")
    ) {
        override fun toUri(config: ProxyConfig): String {
            return config.server
        }
    };

    abstract fun toUri(config: ProxyConfig): String

    fun toDisplayUri(config: ProxyConfig): String {
        return this.toUri(config).split("@").last()
    }

    override fun toString(): String {
        return name.lowercase()
    }

    companion object {
        fun getNames(): List<String> {
            return entries.map { it.name.lowercase() }
        }
    }
}

