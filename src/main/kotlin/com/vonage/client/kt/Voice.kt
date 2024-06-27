package com.vonage.client.kt

import com.vonage.client.voice.*
import com.vonage.client.voice.ncco.Ncco
import java.net.URI
import java.time.Instant
import java.util.*

class Voice(private val voiceClient: VoiceClient) {

    fun call(callId: String): ExistingCall = ExistingCall(callId)

    fun call(callId: UUID): ExistingCall = call(callId.toString())

    inner class ExistingCall(val callId: String) {

        fun get(): CallInfo = voiceClient.getCallDetails(callId)

        fun hangup() = voiceClient.terminateCall(callId)

        fun mute() = voiceClient.muteCall(callId)

        fun unmute() = voiceClient.unmuteCall(callId)

        fun earmuff() = voiceClient.earmuffCall(callId)

        fun unearmuff() = voiceClient.unearmuffCall(callId)

        fun transfer(ncco: Ncco) = voiceClient.transferCall(callId, ncco)

        fun transfer(nccoUrl: String) = voiceClient.transferCall(callId, nccoUrl)

        fun transfer(nccoUrl: URI) = transfer(nccoUrl.toString())

        fun sendDtmf(digits: String): DtmfResponse = voiceClient.sendDtmf(callId, digits)
    }

    fun listCalls(filter: (CallsFilter.Builder.() -> Unit)? = null): CallInfoPage =
        if (filter == null) voiceClient.listCalls()
        else voiceClient.listCalls(CallsFilter.builder().apply(filter).build())

    fun createCall(call: (Call.Builder.() -> Unit)): CallEvent =
        voiceClient.createCall(Call.builder().apply(call).build())
}

fun CallsFilter.Builder.dateStart(dateStart: String): CallsFilter.Builder =
    dateStart(Date.from(Instant.parse(dateStart)))

fun CallsFilter.Builder.dateEnd(dateEnd: String): CallsFilter.Builder =
    dateEnd(Date.from(Instant.parse(dateEnd)))

fun Call.Builder.advancedMachineDetection(amd: AdvancedMachineDetection.Builder.() -> Unit): Call.Builder =
    advancedMachineDetection(AdvancedMachineDetection.builder().apply(amd).build());