package moe.yuuta.encoderbot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.nio.charset.Charset
import java.util.*

class EncoderBot(private val mToken: String,
                 private val mID: String) : TelegramLongPollingBot() {
    override fun getBotUsername(): String = mID

    override fun getBotToken(): String = mToken

    override fun onUpdateReceived(update: Update) {
        System.out.println(update)
        val user =
            when {
                update.hasMessage() -> update.message.from
                update.hasCallbackQuery() -> update.callbackQuery.from
                update.hasInlineQuery() -> update.inlineQuery.from
                else -> {
                    System.err.println("Cannot gather user information for $update")
                    return
                }
            }
        val userLangCode = (user.languageCode ?: "en").replace("-", "_")
        val strings = ResourceBundle.getBundle("strings", Locale(userLangCode))
        if (update.hasMessage()) {
            if (update.message.hasText()) {
                val txt = update.message.text
                val entityStream = update.message.entities?.stream()
                    ?.filter {
                        return@filter it.type == "bot_command"
                    }
                    ?.findFirst()
                val botCommandEntity = if (entityStream != null &&
                    entityStream.isPresent) entityStream.get() else null
                when {
                    txt == "/start" -> {
                        val replyInline = InlineKeyboardMarkup()
                        val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                        val rowInline = ArrayList<InlineKeyboardButton>()
                        rowInline.add(InlineKeyboardButton()
                            .setText(String(strings.getString("welcome_good").toByteArray(Charset.forName("UTF-8")), Charset.forName("UTF-8")))
                            .setCallbackData("callback_welcome_good"))
                        rowsInline.add(rowInline)
                        replyInline.keyboard = rowsInline
                        val reply = SendMessage()
                            .setChatId(update.message.chatId)
                            .setText(strings.getString("welcome"))
                            .setReplyMarkup(replyInline)
                        execute(reply)
                    }
                    txt == "/help" -> {
                        execute(SendMessage()
                            .setChatId(update.message.chatId)
                            .setText(strings.getString("help"))
                            .setReplyToMessageId(update.message.messageId))
                    }
                    txt.startsWith("/qr") -> {
                        if (botCommandEntity == null) {
                            execute(SendMessage()
                                .setChatId(update.message.chatId)
                                .enableMarkdown(true)
                                .setReplyToMessageId(update.message.messageId)
                                .setText(strings.getString("qr_start")))
                        } else {
                            val text = txt.substring(botCommandEntity.length)
                            if (text.isBlank()) {
                                execute(SendMessage()
                                    .setChatId(update.message.chatId)
                                    .enableMarkdown(true)
                                    .setReplyToMessageId(update.message.messageId)
                                    .setText(strings.getString("qr_start")))
                            } else {
                                val stream = Encoder.qr(text)
                                execute(SendPhoto()
                                    .setChatId(update.message.chatId)
                                    .setReplyToMessageId(update.message.messageId)
                                    .setPhoto("@${mID}_qr_${System.nanoTime()}.png", stream))
                                stream.close()
                            }
                        }
                    }
                    txt.startsWith("/b6") -> {
                        if (botCommandEntity == null) {
                            execute(SendMessage()
                                .setChatId(update.message.chatId)
                                .enableMarkdown(true)
                                .setReplyToMessageId(update.message.messageId)
                                .setText(strings.getString("b6_start")))
                        } else {
                            val text = txt.substring(botCommandEntity.length)
                            if (text.isBlank()) {
                                execute(SendMessage()
                                    .setChatId(update.message.chatId)
                                    .enableMarkdown(true)
                                    .setReplyToMessageId(update.message.messageId)
                                    .setText(strings.getString("b6_start")))
                            } else {
                                execute(SendMessage()
                                    .setChatId(update.message.chatId)
                                    .setReplyToMessageId(update.message.messageId)
                                    .enableMarkdown(true)
                                    .setText("`${Encoder.b6(text)}`"))
                            }
                        }
                    }
                    txt.startsWith("/url") -> {
                        if (botCommandEntity == null) {
                            execute(SendMessage()
                                .setChatId(update.message.chatId)
                                .enableMarkdown(true)
                                .setReplyToMessageId(update.message.messageId)
                                .setText(strings.getString("url_start")))
                        } else {
                            val text = txt.substring(botCommandEntity.length + 1)
                            if (text.isBlank()) {
                                execute(SendMessage()
                                    .setChatId(update.message.chatId)
                                    .enableMarkdown(true)
                                    .setReplyToMessageId(update.message.messageId)
                                    .setText(strings.getString("url_start")))
                            } else {
                                execute(SendMessage()
                                    .setChatId(update.message.chatId)
                                    .setReplyToMessageId(update.message.messageId)
                                    .enableMarkdown(true)
                                    .setText("`${Encoder.url(text)}`"))
                            }
                        }
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            when (update.callbackQuery.data) {
                "callback_welcome_good" -> {
                    execute(AnswerCallbackQuery()
                        .setCallbackQueryId(update.callbackQuery.id)
                        .setText(strings.getString("welcome_good_thanks")))
                    execute(EditMessageText()
                        .setChatId(update.callbackQuery.message.chatId)
                        .setMessageId(update.callbackQuery.message.messageId)
                        .setText(strings.getString("welcome_good_after_thanks")))
                }
            }
        } else if (update.hasInlineQuery()) {
            if (update.inlineQuery.hasQuery()) {
                val data = update.inlineQuery.query
                execute(AnswerInlineQuery()
                    .setInlineQueryId(update.inlineQuery.id)
                    .setResults(listOf(
                        InlineQueryResultArticle()
                            .setTitle(strings.getString("b6_name"))
                            .setId("b6_${user.id}_${update.inlineQuery.id}_${data.hashCode()}")
                            .setInputMessageContent(InputTextMessageContent()
                                .setMessageText("`${Encoder.b6(data)}`")
                                .enableMarkdown(true)),
                        InlineQueryResultArticle()
                            .setTitle(strings.getString("url_name"))
                            .setId("url_${user.id}_${update.inlineQuery.id}_${data.hashCode()}")
                            .setInputMessageContent(InputTextMessageContent()
                                .setMessageText("`${Encoder.url(data)}`")
                                .enableMarkdown(true))
                    )))
            }
        }
    }
}