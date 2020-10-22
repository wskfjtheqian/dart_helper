package com.exgou.heqain.dart.helper.translate

import com.exgou.heqain.dart.helper.generate.translate.Translate
import com.google.gson.JsonParser
import org.apache.http.HttpEntity
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
            var response = HttpClients.createDefault().execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val entity: HttpEntity = response.getEntity()
                if (entity != null) {
                    var value = EntityUtils.toString(response.getEntity(), "UTF-8")
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
            }
            return text
        } catch (e: Exception) {
            return text
        }
    }
}

