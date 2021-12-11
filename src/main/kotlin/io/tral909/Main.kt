package io.tral909

import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.docs.DocService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Main {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Main::class.java)

        fun newServer(port: Int): Server {
            val sb: ServerBuilder = Server.builder()
            val docService = DocService.builder()
                .exampleRequests(BlogService::class.java,
                "createBlogPost",
                    "{\"title\":\"My first blog\", \"content\":\"Hello Armeria!\"}")
                .build()

            return sb.http(port)
                .annotatedService(BlogService())
                .serviceUnder("/docs", docService)
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
    Main.logger.info("Server has been started. Serving dummy service at http://127.0.0.1:{}/docs",
        server.activeLocalPort())
}