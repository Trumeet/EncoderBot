package moe.yuuta.encoderbot

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.util.*

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 2) {
            System.err.println("Unexpected arguments. ${args.size}")
            System.err.println("Usage: Launcher <@ user name> <token>")
            System.err.println("For example: Launcher @amazing_encoder_bot 123456:abcdef")
            System.exit(1)
            return
        }
        System.out.println("DEBUG: Starting with ID ${args[0]} and token begins with ${args[1].toCharArray()[0]}")
        Locale.setDefault(Locale("en"))
        ApiContextInitializer.init()
        val api = TelegramBotsApi()
        api.registerBot(EncoderBot(args[1], args[0]))
        System.out.println("DEBUG: All setup")
    }
}