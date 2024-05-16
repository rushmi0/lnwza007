package org.lnwza007.relay.ws


import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import java.nio.charset.Charset

class ResourceService {

    @Get(uri = "/", produces = [MediaType.APPLICATION_JSON])
    fun getIndexHtml(): String? {
        return javaClass.getResourceAsStream("/public/index.html")
            ?.readBytes()
            ?.toString(Charset.defaultCharset())
    }


    fun getNip11Json(): String {
        return javaClass.getResource("/nip-11.json").readText()
    }

}
