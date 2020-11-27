package iris.vk

import iris.vk.event.*

/**
 * @created 01.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkTriggerEventHandler() : VkEventHandler {

	constructor(initializer: VkTriggerEventHandler.() -> Unit) : this() {
		apply(initializer)
	}

	interface TriggerMessage { fun process(messages: List<Message>) }
	interface TriggerEditMessage { fun process(messages: List<Message>) }
	interface TriggerInvite { fun process(messages: List<ChatEvent>) }
	interface TriggerLeave { fun process(messages: List<ChatEvent>) }
	interface TriggerTitleUpdate { fun process(messages: List<TitleUpdate>) }
	interface TriggerPinUpdate { fun process(messages: List<PinUpdate>) }
	interface TriggerUnpinUpdate { fun process(messages: List<PinUpdate>) }
	interface TriggerCallback { fun process(messages: List<CallbackEvent>) }
	interface TriggerScreenshot { fun process(messages: List<ChatEvent>) }
	interface TriggerOther { fun process(messages: List<OtherEvent>) }

	class TriggerMessageLambda(private val processor: (messages: List<Message>) -> Unit) : TriggerMessage {
		override fun process(messages: List<Message>) {
			processor(messages)
		}
	}

	class TriggerMessageEditLambda(private val processor: (messages: List<Message>) -> Unit) : TriggerEditMessage {
		override fun process(messages: List<Message>) {
			processor(messages)
		}
	}

	class TriggerInviteLambda(private val processor: (messages: List<ChatEvent>) -> Unit) : TriggerInvite {
		override fun process(messages: List<ChatEvent>) {
			processor(messages)
		}
	}

	class TriggerLeaveLambda(private val processor: (messages: List<ChatEvent>) -> Unit) : TriggerLeave {
		override fun process(messages: List<ChatEvent>) {
			processor(messages)
		}
	}

	class TriggerTitleUpdateLambda(private val processor: (messages: List<TitleUpdate>) -> Unit) : TriggerTitleUpdate {
		override fun process(messages: List<TitleUpdate>) {
			processor(messages)
		}
	}

	class TriggerPinUpdateLambda(private val processor: (messages: List<PinUpdate>) -> Unit) : TriggerPinUpdate {
		override fun process(messages: List<PinUpdate>) {
			processor(messages)
		}
	}

	class TriggerUnpinUpdateLambda(private val processor: (messages: List<PinUpdate>) -> Unit) : TriggerUnpinUpdate {
		override fun process(messages: List<PinUpdate>) {
			processor(messages)
		}
	}

	class TriggerCallbackLambda(private val processor: (messages: List<CallbackEvent>) -> Unit) : TriggerCallback {
		override fun process(messages: List<CallbackEvent>) {
			processor(messages)
		}
	}

	class TriggerScreenshotLambda(private val processor: (messages: List<ChatEvent>) -> Unit) : TriggerScreenshot {
		override fun process(messages: List<ChatEvent>) {
			processor(messages)
		}
	}

	class TriggerOtherLambda(private val processor: (messages: List<OtherEvent>) -> Unit) : TriggerOther {
		override fun process(messages: List<OtherEvent>) {
			processor(messages)
		}
	}

	private var messages: MutableList<TriggerMessage>? = null
	private var editMessages: MutableList<TriggerEditMessage>? = null
	private var invites: MutableList<TriggerInvite>? = null
	private var leaves: MutableList<TriggerLeave>? = null
	private var titles: MutableList<TriggerTitleUpdate>? = null
	private var pins: MutableList<TriggerPinUpdate>? = null
	private var unpins: MutableList<TriggerUnpinUpdate>? = null
	private var callbacks: MutableList<TriggerCallback>? = null
	private var screenshots: MutableList<TriggerScreenshot>? = null
	private var others: MutableList<TriggerOther>? = null

	operator fun plusAssign(trigger: TriggerMessage) {
		if (messages == null) messages = mutableListOf()
		messages!! += trigger
	}

	operator fun plusAssign(trigger: TriggerEditMessage) {
		if (editMessages == null) editMessages = mutableListOf()
		editMessages!! += trigger
	}

	operator fun plusAssign(trigger: TriggerInvite) {
		if (invites == null) invites = mutableListOf()
		invites!! += trigger
	}

	operator fun plusAssign(trigger: TriggerLeave) {
		if (leaves == null) leaves = mutableListOf()
		leaves!! += trigger
	}

	operator fun plusAssign(trigger: TriggerTitleUpdate) {
		if (titles == null) titles = mutableListOf()
		titles!! += trigger
	}

	operator fun plusAssign(trigger: TriggerPinUpdate) {
		if (pins == null) pins = mutableListOf()
		pins!! += trigger
	}

	operator fun plusAssign(trigger: TriggerUnpinUpdate) {
		if (unpins == null) unpins = mutableListOf()
		unpins!! += trigger
	}

	operator fun plusAssign(trigger: TriggerCallback) {
		if (callbacks == null) callbacks = mutableListOf()
		callbacks!! += trigger
	}

	operator fun plusAssign(trigger: TriggerScreenshot) {
		if (screenshots == null) screenshots = mutableListOf()
		screenshots!! += trigger
	}

	operator fun plusAssign(trigger: TriggerOther) {
		if (others == null) others = mutableListOf()
		others!! += trigger
	}

	fun onMessage(processor: (messages: List<Message>) -> Unit) = plusAssign(TriggerMessageLambda(processor))
	fun onMessage(trigger: TriggerMessage) = plusAssign(trigger)
	fun onMessageEdit(processor: (messages: List<Message>) -> Unit) = plusAssign(TriggerMessageEditLambda(processor))
	fun onInvite(processor: (messages: List<ChatEvent>) -> Unit) = plusAssign(TriggerInviteLambda(processor))
	fun onLeave(processor: (messages: List<ChatEvent>) -> Unit) = plusAssign(TriggerLeaveLambda(processor))
	fun onTitleUpdate(processor: (messages: List<TitleUpdate>) -> Unit) = plusAssign(TriggerTitleUpdateLambda(processor))
	fun onPinUpdate(processor: (messages: List<PinUpdate>) -> Unit) = plusAssign(TriggerPinUpdateLambda(processor))
	fun onUnpinUpdate(processor: (messages: List<PinUpdate>) -> Unit) = plusAssign(TriggerUnpinUpdateLambda(processor))
	fun onCallback(processor: (messages: List<CallbackEvent>) -> Unit) = plusAssign(TriggerCallbackLambda(processor))
	fun onScreenshot(processor: (messages: List<ChatEvent>) -> Unit) = plusAssign(TriggerScreenshotLambda(processor))
	fun onOther(processor: (messages: List<OtherEvent>) -> Unit) = plusAssign(TriggerOtherLambda(processor))

	override fun processMessages(messages: List<Message>) {
		this.messages?.forEach { it.process(messages) }
	}

	override fun processEditedMessages(messages: List<Message>) {
		this.editMessages?.forEach { it.process(messages) }
	}

	override fun processInvites(invites: List<ChatEvent>) {
		this.invites?.forEach { it.process(invites) }
	}

	override fun processLeaves(leaves: List<ChatEvent>) {
		this.leaves?.forEach { it.process(leaves) }
	}

	override fun processTitleUpdates(updaters: List<TitleUpdate>) {
		this.titles?.forEach { it.process(updaters) }
	}

	override fun processPinUpdates(updaters: List<PinUpdate>) {
		this.pins?.forEach { it.process(updaters) }
	}

	override fun processUnpinUpdates(updates: List<PinUpdate>) {
		this.unpins?.forEach { it.process(updates) }
	}

	override fun processCallbacks(callbacks: List<CallbackEvent>) {
		this.callbacks?.forEach { it.process(callbacks) }
	}

	override fun processScreenshots(screenshots: List<ChatEvent>) {
		this.screenshots?.forEach { it.process(screenshots) }
	}

	override fun processOthers(others: List<OtherEvent>) {
		this.others?.forEach { it.process(others) }
	}
}