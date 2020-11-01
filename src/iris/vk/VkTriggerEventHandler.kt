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
	interface TriggerCallback { fun process(messages: List<CallbackEvent>) }

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

	class TriggerCallbackLambda(private val processor: (messages: List<CallbackEvent>) -> Unit) : TriggerCallback {
		override fun process(messages: List<CallbackEvent>) {
			processor(messages)
		}
	}

	private var messages: MutableList<TriggerMessage>? = null
	private var editMessages: MutableList<TriggerEditMessage>? = null
	private var invites: MutableList<TriggerInvite>? = null
	private var leaves: MutableList<TriggerLeave>? = null
	private var titles: MutableList<TriggerTitleUpdate>? = null
	private var pins: MutableList<TriggerPinUpdate>? = null
	private var callbacks: MutableList<TriggerCallback>? = null

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

	operator fun plusAssign(trigger: TriggerCallback) {
		if (callbacks == null) callbacks = mutableListOf()
		callbacks!! += trigger
	}

	//fun onMessage(processor: (messages: List<Message>) -> Unit) = plusAssign(TriggerMessageLambda(processor))
	fun onMessage(processor: (messages: List<Message>) -> Unit) = plusAssign(TriggerMessageLambda(processor))
	fun onMessage(trigger: TriggerMessage) = plusAssign(trigger)
	//fun onMessage(block: VkEventHandlerTrigger.() -> TriggerMessage) = plusAssign(block())
	fun onMessageEdit(processor: (messages: List<Message>) -> Unit) = plusAssign(TriggerMessageEditLambda(processor))
	fun onInvite(processor: (messages: List<ChatEvent>) -> Unit) = plusAssign(TriggerInviteLambda(processor))
	fun onLeave(processor: (messages: List<ChatEvent>) -> Unit) = plusAssign(TriggerLeaveLambda(processor))
	fun onTitleUpdate(processor: (messages: List<TitleUpdate>) -> Unit) = plusAssign(TriggerTitleUpdateLambda(processor))
	fun onPinUpdate(processor: (messages: List<PinUpdate>) -> Unit) = plusAssign(TriggerPinUpdateLambda(processor))
	fun onCallback(processor: (messages: List<CallbackEvent>) -> Unit) = plusAssign(TriggerCallbackLambda(processor))

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

	override fun processCallbacks(callbacks: List<CallbackEvent>) {
		this.callbacks?.forEach { it.process(callbacks) }
	}
}