# Iris VK API

Ещё одна библиотека по работе с VK API на **Kotlin** 💖

Гибкая система получения данных от VK. После обновления VK API вам не придётся ждать 
обновления в ваших прежних используемых библиотеках. Все данные будут доступны сразу после обновлений самого VK API.

## Как скачать и установить?

##### Прямая ссылка:

- Вы можете скачать [подготовленные релизы](https://github.com/iris2iris/iris-vk-api/releases), чтобы скачать JAR файл напрямую.
- Также вам необходимо скачать зависимость — JAR файл [Iris JSON Parser](https://github.com/iris2iris/iris-vk-api/releases/download/v0.3/iris-json-parser.jar)

## Как это использовать

### Простой VkApi

```kotlin
val vk = VkApi(token)
val res = vk.messages.send(userToId, "Привет. Это сообщение с Kotlin")
println(res?.obj())
```

### VkApi методом Future

```kotlin
val vk = VkApiFuture(token)
vk.messages.send(userToId, "Привет. Это сообщение с Kotlin").thenAccept {
    println("Это сообщение появится вторым")
    println(it?.obj())
}
println("Это сообщение появится первым, т.к. метод Future неблокирующий")

// А можно сделать последовательное исполнение
val future = vk.messages.send(userToId, "Привет. Это сообщение с Kotlin")
val result = future.get() // дожидаемся ответа
println(result?.obj()) // выводим результат
```

### VkApi, упаковывающий запросы в execute
```kotlin
val vk = VkApiPack(token)
val futuresList = vk.messages.sendMulti(listOf(
        Options("peer_id" to userToId, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"),
        Options("peer_id" to 2, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"),
)
)
println("Прошёл сюда без задержек")
val secondFutures = vk.execute(listOf(
        VkRequestData("messages.send", Options("peer_id" to userToId, "random_id" to (0..2_000_000).random(), "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"))
        , VkRequestData("messages.edit", Options("peer_id" to userToId, "conversation_message_id" to 1, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"))
))

println("И сюда тоже без задержек. Но вот ниже нужно подождать\n")
println("Первый пакет:")
for (it in futuresList.futures)
    println(it.get()?.obj())

println()
println("Второй пакет скорее всего без задержек:")
for (it in secondFutures.futures)
    println(it.get()?.obj())
println()
println("Завершились")
```

### VkEngineGroup — слушатель событий методом Long Poll

```kotlin
// Создаём класс для отправки сообщений
val vk = VkApiPack(token)

// Определяем простой обработчик событий
val simpleMessageHandler = object : VkHandlerAdapter() {

    override fun processMessage(message: Message) {
        // message содержит информацию о полученном JsonItem (message.source) и вспомогательную информацию, которую
        // добавит сам программист по мере продвижения события (message.options)

        // message.text — это метод, подготавливает текст для дальнейшей работы
        val text = message.text
        if (text.equals("пинг", true))
            vk.messages.send(message.peerId, "ПОНГ")
    }
}

// Передаём в параметрах слушателя событий токен и созданный обработчик событий
val listener = VkEngineGroup(token, simpleMessageHandler)
listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

exitProcess(0)
```

### VkEngineUser — слушатель событий пользовательского Long Poll
Всё то же самое, что и у `VkEngineGroup`, только вместо этого класса используется `VkEngineUser`
```kotlin
//...
val listener = VkEngineUser(token, simpleMessageHandler)
//...
```

### VkEngineCallback — слушатель событий методом VK Callback API

```kotlin
val cbEngine = VkEngineGroupCallback(
        gbSource = SimpleGroupSource(Groupbot(groupId, confirmation, secret))
        , path = "/kotlin/callback"
)
cbEngine.start() // Запускаем сервер. Открываем порт для входящих. Неблокирующий вызов

while (true) {
    val events = cbEngine.retrieve(wait = true) // ожидаем получения хотя бы одного события
    for (event in events) {
        println("Событие получено: " + event.obj())
    }
}
```
Также смотрите более развёрнутый пример использования `VkEngineCallback` [iris.vk.test/group_cb_multibot.kt](https://github.com/iris2iris/iris-vk-api/blob/master/test/iris/vk/test/group_cb_multibot.kt)

### VkCommandHandler

Возможность добавлять обработчики каждой текстовой команды отдельным обработчиком
```kotlin
val commandsHandler = VkCommandHandler()

commandsHandler += CommandMatcherSimple("пинг") {
    vk.messages.send(it.peerId, "ПОНГ!")
}

commandsHandler += CommandMatcherRegex("рандом (\\d+) (\\d+)") { vkMessage, params ->

    var first = params[1].toInt()
    var second = params[2].toInt()
    if (second < first)
        first = second.also { second = first }

    vk.messages.send(vkMessage.peerId, "🎲 Случайное значение в диапазоне [$first..$second] выпало на ${(first..second).random()}")
}

// Передаём в параметрах слушателя событий токен и созданный обработчик команд
val listener = VkEngineGroup(token, commandsHandler)
listener.run()
```

### Настройка карты команд с помощью DSL
```kotlin
val commandsHandler = VkCommandHandler()

// Конфигурирование команд в стиле DSL
commandsHandler += commands {
    "пинг" to {
        api.messages.send(it.peerId, "ПОНГ!")
    }

    "мой ид" to {
        api.messages.send(it.peerId, "Ваш ID равен: ${it.fromId}")
    }

    regex("""рандом (\d+) (\d+)""") to { vkMessage, params ->

        var first = params[1].toInt()
        var second = params[2].toInt()
        if (second < first)
            first = second.also { second = first }

        api.messages.send(vkMessage.peerId, "🎲 Случайное значение в диапазоне [$first..$second] выпало на ${(first..second).random()}")
    }
}

// Передаём в параметрах слушателя событий токен и созданный обработчик команд
val listener = VkEngineGroup(token, commandsHandler)
listener.run()
```

### Настройка обработки событий методом onXxx
```kotlin
// Определяем обработчик триггеров
val triggerHandler = VkEventHandlerTrigger()

// можно настраивать лямбдами
triggerHandler.onMessage {
    for (message in it)
        println("Получено сообщение от ${message.peerId}: ${message.text}")
}

triggerHandler.onMessageEdit {
    for (message in it)
        println("Сообщение исправлено ${message.id}: ${message.text}")
}

// можно настраивать классами триггеров
triggerHandler += VkCommandHandler(
    commands = listOf(
        CommandMatcherSimple("пинг") {
            vk.messages.send(it.peerId, "ПОНГ!")
        },

        CommandMatcherSimple("мой ид") {
            vk.messages.send(it.peerId, "Ваш ID равен: ${it.fromId}")
        },

        CommandMatcherRegex("""рандом (\d+) (\d+)""") { vkMessage, params ->

            var first = params[1].toInt()
            var second = params[2].toInt()
            if (second < first)
                first = second.also { second = first }

            vk.messages.send(vkMessage.peerId, "🎲 Случайное значение в диапазоне [$first..$second] выпало на ${(first..second).random()}")
        }
    )
)
```

Все приведённые выше примеры доступны в пакете [iris.vk.test](https://github.com/iris2iris/iris-vk-api/blob/master/test/iris/vk/test)

## Дополнительная информация

**[Iris VK API](https://github.com/iris2iris/iris-vk-api)** использует библиотеку **[Iris JSON Parser](https://github.com/iris2iris/iris-json-parser-kotlin)** для обработки ответов от сервера VK. Загляните ознакомиться =)

#### Не забывайте поставить звёзды, если этот инструмент оказался вам полезен ⭐
