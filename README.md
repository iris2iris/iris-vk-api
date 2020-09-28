# Iris VK API

Ещё одна библиотека по работе с VK API на **Kotlin** 💖

Гибкая система получения данных от VK. После обновления VK API вам не придётся ждать 
обновления в ваших прежних используемых библиотеках. Все данные будут доступны сразу после обновлений самого VK API.

## Как скачать и установить?

##### Прямая ссылка:

- Вы можете скачать [подготовленные релизы](https://github.com/iris2iris/iris-vk-api/releases), чтобы скачать JAR файл напрямую.
- Также вам необходимо скачать зависимость — JAR файл [Iris JSON Parser](https://github.com/iris2iris/iris-vk-api/releases/download/v0.1/iris-json-parser.jar)

## Как это использовать

### Простой VkApi

```kotlin
val vk = VkApi(token)
val res = vk.messages.send(userToId, "Привет. Это сообщение с Kotlin")
println(res?.obj())
```

### VkApi на Completable Future

```kotlin
val vk = VkApiFuture(token)
vk.messages.send(userToId, "Привет. Это сообщение с Kotlin").thenApply {
    println(it?.obj())
}
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
    VkRequestData("messages.send", Options("peer_id" to userToId, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"))
    , VkRequestData("messages.edit", Options("peer_id" to userToId, "conversation_message_id" to 1, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"))
))

println("И сюда тоже без задержек. Но вот ниже нужно подождать")
for (it in futuresList.futures)
    println(it.get()?.obj())

println("Получили данные, пошли дальше")
for (it in secondFutures.futures)
    println(it.get()?.obj())
println("Завершились")
// У нас была создана фабрика потоков, поэтому так просто программа не завершится. Нужно принудительно
exitProcess(0)
```

### VkEngineGroup — слушатель событий методом Long Poll

```kotlin
// Создаём класс для отправки сообщений
val vk = VkApiPack(token)

// Определяем простой обработчик событий
val simpleMessageHandler = object : VkHandlerAdapter() {

    override fun processMessage(message: VkMessage) {
        // message содержит информацию о полученном JsonItem (message.source) и вспомогательную информацию, которую
        // добавит сам программист по мере продвижения события (message.options)

        // message.text — это метод, подготавливает текст для дальнейшей работы
        val text = message.text
        val messageItem = message.source["message"]
        if (text.equals("пинг", true))
            vk.messages.send(messageItem["from_id"].asInt(), "ПОНГ")
    }
}

// Передаём в параметрах слушателя событий токен и созданный обработчик событий
val listener = VkEngineGroup(token, simpleMessageHandler)
listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

exitProcess(0)
```

### VkEngineCallback - слушатель событий методом VK Callback API

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

Все приведённые выше примеры доступны в пакете [iris.vk.test](https://github.com/iris2iris/iris-vk-api/blob/master/src/iris/vk/test)

## Дополнительная информация

**[Iris VK API](https://github.com/iris2iris/iris-vk-api)** использует библиотеку **[Iris JSON Parser](https://github.com/iris2iris/iris-json-parser-kotlin)** для обработки ответов от сервера VK. Загляните ознакомиться =)

#### Не забывайте поставить звёзды, если этот инструмент оказался вам полезен ⭐
