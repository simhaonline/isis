package org.ro.core.event

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.ro.core.TransferObject
import org.ro.view.table.ActionMenu
import kotlin.js.Date

enum class EventState(val id: String, val iconName: String) {
    INITIAL("INITIAL", "fa-power-off"),
    RUNNING("RUNNING", "fa-play-circle"),
    ERROR("ERROR", "fa-times-circle"),
    SUCCESS("SUCCESS", "fa-check-circle"),
    VIEW("VIEW", "fa-info-circle"),
    CLOSED("CLOSED", "fa-times-circle") //TODO should be different from ERROR?
}

@Serializable
data class LogEntry(
        val url: String,
        val method: String? = "",
        val request: String = "") {
    var state = EventState.INITIAL
    var menu: ActionMenu? = null  //TODO Deprecated, use iconName
    var title: String = ""

    init {
        state = EventState.RUNNING
        menu = ActionMenu("fa-ellipsis-h")
        title = stripHostPort(url)
    }

    @ContextualSerialization
    var createdAt = Date()
    var start: Int = createdAt.getMilliseconds()
    @ContextualSerialization
    var updatedAt: Date? = null
    @ContextualSerialization
    private var lastAccessedAt: Date? = null

    private var fault: String? = null
    val requestLength: Int
        get() = request.length

    var response = ""
    val responseLength: Int
        get() = response.length

    var offset = 0
    var duration = 0
    var cacheHits = 0
    var observer: IObserver? = null
    var obj: TransferObject? = null

    // alternative constructor for UI events (eg. from user interaction)
    constructor(title: String) : this("", "", "") {
        this.title = title
        state = EventState.VIEW
    }

    private fun calculate() {
        duration = updatedAt!!.getMilliseconds() - start
        val logStartTime: Int? = EventStore.getLogStartTime()
        if (logStartTime != null) {
            offset = start - logStartTime
        }
    }

    fun setError(error: String) {
        updatedAt = Date()
        calculate()
        fault = error
        state = EventState.ERROR
    }

    fun setClose() {
        updatedAt = Date()
        state = EventState.CLOSED
    }

    fun setSuccess(response: String) {
        updatedAt = Date()
        calculate()
        this.response = response //TODO pretty print json 
        state = EventState.SUCCESS
    }

    override fun toString(): String {
        var s = "\n"
        s += "url:= $url\n"
        if (getObj() == null) {
            s += "obj:= null\n"
        } else {
            s += "obj:= ${getObj()!!::class.simpleName}\n"
        }
        if (observer == null) {
            s += "observer:= null\n"
        } else {
            s += "observer:= ${observer!!::class.simpleName}\n"
        }
        s += "response:= $response\n"
        return s
    }

    fun getObj(): TransferObject? {
        return obj
    }

    fun setObj(obj: TransferObject?) {
        this.obj = obj
    }

    // region response
    /**
     * This is for access from the views only.
     * DomainObjects have to use retrieveResponse,
     * since we want to have access statistics
     * and a cache function.
     * @return
     */
    fun getResponse(): String {
        return response
    }

    fun hasResponse(): Boolean {
        return response != ""
    }

    fun retrieveResponse(): String {
        lastAccessedAt = Date()
        cacheHits++
        return response
    }

    //end region response

    private fun stripHostPort(url: String): String {
        var result = url
        //TODO use value from Session
        result = result.replace("http://localhost:8080/restful/", "")
        result = removeHexCode(result)
        return result
    }

    private fun removeHexCode(input: String): String {
        var output = ""
        val list: List<String> = input.split("/")
        //split string by "/" and remove parts longer than 40chars
        for (s in list) {
            output += "/"
            output += if (s.length < 40) {
                s
            } else {
                "..."
            }
        }
        return output
    }

    fun isView(): Boolean {
        return !isUrl()
    }

    fun isUrl(): Boolean {
        return url.startsWith("http")
    }

    fun isClosedView(): Boolean {
        return state == EventState.CLOSED
    }

    fun isError(): Boolean {
        return fault != null
    }

    fun match(search: String?): Boolean {
        // TODO  dismantle train.wreck
        return search?.let {
            url.contains(it, true) ?: false ||
                    response.contains(it, true) ?: false ||
                    method?.contains(it, true) ?: false
        } ?: true
    }

}