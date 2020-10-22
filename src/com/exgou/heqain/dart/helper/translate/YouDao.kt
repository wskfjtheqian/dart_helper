package com.exgou.heqain.dart.helper.translate

import com.exgou.heqain.dart.helper.generate.translate.Translate
import com.google.gson.JsonParser
import java.io.StringReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class YouDao : Translate() {
    var httpClient: HttpClient = HttpClient.newBuilder().build();
    override fun toEnglish(text: String): String {
        val httpRequest = HttpRequest.newBuilder().uri(URI("http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=${text}"));
        try {
            var text = httpClient.send(httpRequest.build(), HttpResponse.BodyHandlers.ofString()).body()
            var json = JsonParser.parseReader(StringReader(text)).asJsonObject
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
            return text
        } catch (e: Exception) {
            return text;
        }
    }
}

