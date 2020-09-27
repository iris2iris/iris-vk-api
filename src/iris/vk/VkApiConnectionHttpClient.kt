package iris.vk

import iris.vk.VkApiConnection.VkApiConnectResponse
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

/**
 * @created 22.10.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkApiConnectionHttpClient(client: HttpClient? = null) : VkApiConnection {

	private val client: HttpClient = client?: HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.followRedirects(HttpClient.Redirect.NEVER)
		.connectTimeout(Duration.ofSeconds(5))
		.build()

	override fun request(url: String, data: Map<String, Any>?): VkApiConnectResponse? {
		return request(url, encodeOptions(data))
	}

	override fun request(url: String, data:  String?): VkApiConnectResponse? {

		var builder = HttpRequest.newBuilder()
			.uri(URI.create(url));
		if (data != null)
			builder = builder.POST(HttpRequest.BodyPublishers.ofString(data))
		val request = builder.build()
		val tries = 3
		for (i in 1..tries) {
			try {
				val response = client.send(request, BodyHandlers.ofString())
				return VkApiConnectResponse(response.statusCode(), response.body())
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
		return null
	}

	override fun requestUpload(url: String, files: Map<String, Options>, data: Map<String, Any>?): VkApiConnectResponse? {
		val map = if (data == null) mutableMapOf() else if (data is MutableMap<*, *>) data as MutableMap<String, Any> else HashMap(data)
		map.putAll(files)
		return requestMultipart(url, map)
	}

	fun requestMultipart(url: String, data: Map<String, Any>?, headers: Map<String, String>? = null): VkApiConnectResponse? {
		val map = data?: emptyMap()
		val boundary = "-------------573cf973d5228"
		val builder = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.POST(ofMimeMultipartData(map, boundary))
		builder.header("Content-Type", "multipart/form-data; boundary=\"$boundary\"")
		headers?.forEach { k, value -> builder.header(k, value) }

		val request	= builder.build()
		for (i in 1..3) {
			try {
				val response = client.send(request, BodyHandlers.ofString())
				return VkApiConnectResponse(response.statusCode(), response.body())
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
		return null
	}

	private fun encode(o: String): String? {
		return URLEncoder.encode(o, StandardCharsets.UTF_8)
	}

	private fun encodeOptions(obj: Map<String, Any>?): String? {
		if (obj == null) return ""
		val sb = StringBuilder()
		for (o in obj.entries) {
			sb.append(encode(o.key)).append('=')
				.append(encode(o.value.toString())).append("&")
		}
		return sb.toString()
	}

	private fun ofMimeMultipartData(dataItem: Map<String, Any>, boundary: String): HttpRequest.BodyPublisher {
		val byteArrays = ArrayList<ByteArray>();
		val separator = ("--$boundary\r\nContent-Disposition: form-data; name=").toByteArray();

		for (entry in dataItem.entries) {
			byteArrays.add(separator);
			val value = entry.value
			if (value is Options) {
				if (value["file"] != null) {
					val path = if (value["file"] is File) Path.of((value["file"] as File).toURI()) else Path.of(value["file"] as String)
					val mimeType = if (value["Content-Type"] != null) value.getString("Content-Type") else Files.probeContentType(path)
					val filename = value["filename"] ?: path.fileName
					byteArrays.add(("\"" + encode(entry.key) + "\"; filename=\"" + filename + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").toByteArray());
					byteArrays.add(Files.readAllBytes(path))
					byteArrays.add("\r\n".toByteArray())
				} else if (value["data"] is ByteArray) {
					val mimeType = if (value["Content-Type"] != null) value.getString("Content-Type") else "application/octet-stream"
					val filename = value.getStringOrNull("filename")?: "Untitled"
					byteArrays.add(("\"" + encode(entry.key) + "\"; filename=\"" + filename + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").toByteArray());
					byteArrays.add(value["data"] as ByteArray)
					byteArrays.add("\r\n".toByteArray())
				} else {
					throw IllegalArgumentException(value.toString())
				}
			} else if (value is File || value is Path) {
				val path = if (value is File) Path.of(value.toURI()) else value as Path
				val mimeType = Files.probeContentType(path)
				byteArrays.add(("\"" + encode(entry.key as String) + "\"; filename=\"" + path.fileName + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").toByteArray());
				byteArrays.add(Files.readAllBytes(path))
				byteArrays.add("\r\n".toByteArray());
			} else {
				byteArrays.add(("\"" + encode(entry.key as String) + "\"\r\n\r\n" + entry.value + "\r\n").toByteArray());
			}
		}
		byteArrays.add(("--$boundary--").toByteArray());
		return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
	}
}