package iris.vk.api

import iris.vk.Options

class VkRequestData(val method: String, val options: Options? = null, val token: String? = null, val version: String? = null)