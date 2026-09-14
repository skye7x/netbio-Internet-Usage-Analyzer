package pl.netbio.internetusageanalyzer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsTestEngine @Inject constructor() {

    data class DnsResult(
        val server: String,
        val domain: String,
        val resolvedIp: String,
        val latencyMs: Double,
        val isSuccessful: Boolean
    )

    private val dnsServers = listOf(
        "8.8.8.8" to "Google DNS",
        "8.8.4.4" to "Google DNS 2",
        "1.1.1.1" to "Cloudflare DNS",
        "1.0.0.1" to "Cloudflare DNS 2",
        "208.67.222.222" to "OpenDNS",
        "9.9.9.9" to "Quad9 DNS"
    )

    private val testDomains = listOf(
        "google.com",
        "cloudflare.com",
        "amazon.com",
        "github.com",
        "microsoft.com"
    )

    suspend fun testDns(domain: String = "google.com"): List<DnsResult> = withContext(Dispatchers.IO) {
        dnsServers.map { (server, name) ->
            val start = System.nanoTime()
            try {
                val addr = InetAddress.getByName(domain)
                val end = System.nanoTime()
                DnsResult(
                    server = "$name ($server)",
                    domain = domain,
                    resolvedIp = addr.hostAddress ?: "N/A",
                    latencyMs = (end - start) / 1_000_000.0,
                    isSuccessful = true
                )
            } catch (e: Exception) {
                val end = System.nanoTime()
                DnsResult(
                    server = "$name ($server)",
                    domain = domain,
                    resolvedIp = "Failed",
                    latencyMs = (end - start) / 1_000_000.0,
                    isSuccessful = false
                )
            }
        }
    }

    suspend fun testAllDomains(): Map<String, List<DnsResult>> {
        return testDomains.associateWith { testDns(it) }
    }
}