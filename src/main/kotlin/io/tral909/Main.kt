package io.tral909

import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Main {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Main::class.java)

        fun newServer(port: Int): Server {
            val sb: ServerBuilder = Server.builder()
            return sb.http(port)
                .service("/") { ctx, req -> HttpResponse.of("Hello, Armeria!") }
                .build()
        }
    }

}

fun main(args: Array<String>) {
    val server: Server = Main.newServer(8080)
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop().join()
        Main.logger.info("Server has been stopped.")
    })

    server.start().join()
    Main.logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}",
        server.activeLocalPort())
}