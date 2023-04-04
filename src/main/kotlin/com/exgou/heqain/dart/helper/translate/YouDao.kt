package com.exgou.heqain.dart.helper.translate

import com.google.gson.JsonParser
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.StringReader
import java.net.URI

class YouDao : Translate() {
    override fun toEnglish(text: String): String {
        try {
            val request = HttpGet(URI("http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=${text}"))
            val response = HttpClients.createDefault().execute(request);
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                val value = EntityUtils.toString(response.entity, "UTF-8")
                var json = JsonParser.parseReader(StringReader(value)).asJsonObject
                if (0 == json["errorCode"].asInt) {
                    var result = json["translateResult"].asJsonArray
                    if (0 < result.size()) {
                        result = result[0].asJsonArray
                        if (0 < result.size()) {
                            json = result[0].asJsonObject
                            if (!(json["tgt"]?.asString?.isEmpty()!!)) {
                                return toClassName(json["tgt"].asString)
                            }
                        }
                    }
                }
            }
            return text
        } catch (e: Exception) {
            return text
        }
    }
}

