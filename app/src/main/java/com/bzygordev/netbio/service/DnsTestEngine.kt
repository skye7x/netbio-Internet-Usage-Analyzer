package com.bzygordev.netbio.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsTestEngine @Inject constructor() {

    data class DnsServer(
        val name: String,
        val address: String
    )

    data class DnsResult(
        val server: String,
        val address: String,
        val resolvedIp: String,
        val latencyMs: Double,
        val isSuccessful: Boolean
    )

    private val defaultServers = listOf(
        DnsServer(name = "Google DNS", address = "8.8.8.8"),
        DnsServer(name = "Cloudflare DNS", address = "1.1.1.1"),
        DnsServer(name = "Quad9 DNS", address = "9.9.9.9"),
        DnsServer(name = "OpenDNS", address = "208.67.222.222")
    )

    suspend fun testDns(
        servers: List<DnsServer> = defaultServers,
        domain: String = "google.com"
    ): List<DnsResult> = withContext(Dispatchers.IO) {
        servers.map { server ->
            testSingleDns(server, domain)
        }
    }

    suspend fun testSingleDns(
        server: DnsServer,
        domain: String
    ): DnsResult = withContext(Dispatchers.IO) {
        try {
            val dnsServerAddr = InetAddress.getByName(server.address)
            val start = System.nanoTime()
            val resolved = InetAddress.getAllByName(domain)
            val end = System.nanoTime()
            val latencyMs = (end - start) / 1_000_000.0
            val resolvedIp = resolved.firstOrNull()?.hostAddress ?: "N/A"

            DnsResult(
                server = server.name,
                address = server.address,
                resolvedIp = resolvedIp,
                latencyMs = latencyMs,
                isSuccessful = resolved.isNotEmpty()
            )
        } catch (e: Exception) {
            val end = System.nanoTime()
            DnsResult(
                server = server.name,
                address = server.address,
                resolvedIp = "Failed",
                latencyMs = 0.0,
                isSuccessful = false
            )
        }
    }

    suspend fun testAllDomains(): Map<String, List<DnsResult>> {
        val domains = listOf("google.com", "cloudflare.com", "amazon.com", "github.com", "microsoft.com")
        return domains.associateWith { domain -> testDns(domain = domain) }
    }
}
