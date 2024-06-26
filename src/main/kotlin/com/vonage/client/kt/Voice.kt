package com.vonage.client.kt

import com.vonage.client.voice.CallInfo
import com.vonage.client.voice.VoiceClient
import java.util.*

class Voice(private val voiceClient: VoiceClient) {

    fun getCall(callId: UUID): CallInfo = getCall(callId.toString())

    fun getCall(callId: String): CallInfo = voiceClient.getCallDetails(callId)

    
}