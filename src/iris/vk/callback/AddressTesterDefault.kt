package iris.vk.callback

import iris.vk.callback.VkCallbackRequestHandler.Request
import java.math.BigInteger
import java.net.InetAddress


/**
 * Проверяет подлинность источника входящего запроса
 *
 * @param ipSubnets Список доверенных подсетей. По умолчанию `95.142.192.0/21` и `2a00:bdc0::/32`
 * @param realIpHeader Если указан, извлекает информацию из заголовка запроса с указанным в `realIpHeader` названием.
 * Например, `X-Real-IP`, `CF-Connecting-IP` и подобные
 * @created 29.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class AddressTesterDefault(
	ipSubnets: Array<String> = arrayOf("95.142.192.0/21", "2a00:bdc0::/32"),
	private val realIpHeader: String? = null
) : AddressTester {

	private val ipSubnets = ipSubnets.map { Subnet.getInstance(it) }

	override fun getRealHost(request: Request): String {
		return getRealHostInternal(request) ?: request.remoteAddress.address.hostAddress
	}

	override fun isGoodHost(request: Request): Boolean {
		val address = (if (realIpHeader == null) {
			request.remoteAddress.address
		} else { // нужно вытащить реальный IP адрес
			val host = getRealHostInternal(request)
			if (host == null)
				request.remoteAddress.address
			else
				InetAddress.getByName(host)
		}).address.let { BigInteger(it) }

		return ipSubnets.any { it.isInNet(address) }
	}

	private fun getRealHostInternal(request: Request): String? {
		if (realIpHeader == null) return null
		val fwd = request.findHeader(realIpHeader)
		return fwd ?: request.remoteAddress.address.hostAddress
	}

	/************************************/
	private class Subnet {
		private val bytesSubnetCount: Int
		private val bigMask: BigInteger
		private val bigSubnetMasked: BigInteger

		/** For use via format "192.168.0.0/24" or "2001:db8:85a3:880:0:0:0:0/57"  */
		constructor(subnetAddress: InetAddress, bits: Int) {
			bytesSubnetCount = subnetAddress.address.size // 4 or 16
			bigMask = BigInteger.valueOf(-1).shiftLeft(bytesSubnetCount * 8 - bits) // mask = -1 << 32 - bits
			bigSubnetMasked = BigInteger(subnetAddress.address).and(bigMask)
		}

		/** For use via format "192.168.0.0/255.255.255.0" or single address  */
		constructor(subnetAddress: InetAddress, mask: InetAddress?) {
			bytesSubnetCount = subnetAddress.address.size
			bigMask = if (null == mask) BigInteger.valueOf(-1) else BigInteger(mask.address) // no mask given case is handled here.
			bigSubnetMasked = BigInteger(subnetAddress.address).and(bigMask)
		}

		fun isInNet(address: InetAddress): Boolean {
			val bytesAddress = address.address
			if (bytesSubnetCount != bytesAddress.size) return false
			val bigAddress = BigInteger(bytesAddress)
			return bigAddress.and(bigMask) == bigSubnetMasked
		}

		fun isInNet(bytesAddress: ByteArray): Boolean {
			if (bytesSubnetCount != bytesAddress.size) return false
			val bigAddress = BigInteger(bytesAddress)
			return bigAddress.and(bigMask) == bigSubnetMasked
		}

		fun isInNet(bigAddress: BigInteger): Boolean {
			return bigAddress.and(bigMask) == bigSubnetMasked
		}

		override fun hashCode(): Int {
			return bytesSubnetCount
		}

		override fun toString(): String {
			val buf = StringBuilder()
			bigInteger2IpString(buf, bigSubnetMasked, bytesSubnetCount)
			buf.append('/')
			bigInteger2IpString(buf, bigMask, bytesSubnetCount)
			return buf.toString()
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false
			other as Subnet

			if (bytesSubnetCount != other.bytesSubnetCount) return false
			if (bigMask != other.bigMask) return false
			if (bigSubnetMasked != other.bigSubnetMasked) return false

			return true
		}

		companion object {
			/**
			 * Subnet factory method.
			 * @param subnetMask format: "192.168.0.0/24" or "192.168.0.0/255.255.255.0"
			 * or single address or "2001:db8:85a3:880:0:0:0:0/57"
			 * @return a new instance
			 * @throws UnknownHostException thrown if unsupported subnet mask.
			 */
			fun getInstance(subnetMask: String): Subnet {
				val stringArr = subnetMask.split("/").toTypedArray()
				return if (2 > stringArr.size) Subnet(InetAddress.getByName(stringArr[0]), null as InetAddress?) else if (stringArr[1].contains(".") || stringArr[1].contains(":")) Subnet(InetAddress.getByName(stringArr[0]), InetAddress.getByName(stringArr[1])) else Subnet(InetAddress.getByName(stringArr[0]), stringArr[1].toInt())
			}

			private fun bigInteger2IpString(buf: StringBuilder, bigInteger: BigInteger, displayBytes: Int) {
				val isIPv4 = 4 == displayBytes
				val bytes = bigInteger.toByteArray()
				val diffLen = displayBytes - bytes.size
				val fillByte = if (0 > bytes[0].toInt()) 0xFF.toByte() else 0x00.toByte()
				var integer: Int
				for (i in 0 until displayBytes) {
					if (0 < i && !isIPv4 && i % 2 == 0) buf.append(':') else if (0 < i && isIPv4) buf.append('.')
					integer = 0xFF and (if (i < diffLen) fillByte else bytes[i - diffLen]).toInt()
					if (!isIPv4 && 0x10 > integer) buf.append('0')
					buf.append(if (isIPv4) integer else Integer.toHexString(integer))
				}
			}
		}
	}
}