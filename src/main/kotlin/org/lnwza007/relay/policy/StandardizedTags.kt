package org.lnwza007.relay.policy

import io.micronaut.core.annotation.Internal
import io.micronaut.core.annotation.NonNull

enum class StandardizedTags(
    val tage: String,
    val description: String,
    val otherParameters: String?,
    val nip: String
) : CharSequence {
    TAG_E("e", "event id (hex)", "relay URL, marker, pubkey (hex)", "01, 10"),
    TAG_P("p", "pubkey (hex)", "relay URL, petname", "01, 02"),
    TAG_A("a", "coordinates to an event", "relay URL", "01"),
    TAG_D("d", "identifier", "--", "01"),
    TAG_G("g", "geohash", "--", "52"),
    TAG_I("i", "identity", "proof", "39"),
    TAG_K("k", "kind number (string)", "--", "18, 25, 72"),
    TAG_L("l", "label, label namespace", "--", "32"),
    TAG_L_("L", "label namespace", "--", "32"),
    TAG_M("m", "MIME type", "--", "94"),
    TAG_Q("q", "event id (hex)", "relay URL", "18"),
    TAG_R("r", "a reference (URL, etc)", "petname | relay url, marker", "24 | 65"),
    TAG_T("t", "hashtag", "--", ""),
    ALT("alt", "summary", "--", "31"),
    AMOUNT("amount", "millisatoshis, stringified", "--", "57"),
    BOLT11("bolt11", "bolt11 invoice", "--", "57"),
    CHALLENGE("challenge", "challenge string", "--", "42"),
    CLIENT("client", "name, address", "relay URL", "89"),
    CLONE("clone", "git clone URL", "--", "34"),
    CONTENT_WARNING("content-warning", "reason", "--", "36"),
    DELEGATION("delegation", "pubkey, conditions, delegation token", "--", "26"),
    DESCRIPTION("description", "description", "--", "34, 57, 58"),
    EMOJI("emoji", "shortcode, image URL", "--", "30"),
    ENCRYPTED("encrypted", "--", "--", "90"),
    EXPIRATION("expiration", "unix timestamp (string)", "--", "40"),
    GOAL("goal", "event id (hex)", "relay URL", "75"),
    IMAGE("image", "image URL", "dimensions in pixels", "23, 58"),
    IMETA("imeta", "inline metadata", "--", "92"),
    LNURL("lnurl", "bech32 encoded lnurl", "--", "57"),
    LOCATION("location", "location string", "--", "52, 99"),
    NAME("name", "name", "--", "34, 58"),
    NONCE("nonce", "random", "difficulty", "13"),
    PREIMAGE("preimage", "hash of bolt11 invoice", "--", "57"),
    PRICE("price", "price", "currency, frequency", "99"),
    PROXY("proxy", "external ID", "protocol", "48"),
    PUBLISHED_AT("published_at", "unix timestamp (string)", "--", "23"),
    RELAY("relay", "relay url", "--", "42, 17"),
    RELAYS("relays", "relay list", "--", "57"),
    SERVER("server", "file storage server url", "--", "96"),
    SUBJECT("subject", "subject", "--", "14, 17"),
    SUMMARY("summary", "article summary", "--", "23"),
    THUMB("thumb", "badge thumbnail", "dimensions in pixels", "58"),
    TITLE("title", "article title", "--", "23"),
    WEB("web", "webpage URL", "--", "34"),
    ZAP("zap", "pubkey (hex), relay URL", "weight", "57");

    companion object {

        fun parse(script: List<StandardizedTags>): List<String> {
            return script.map { it.tage }
        }

        fun parse(script: StandardizedTags): String {
            return script.tage
        }

        fun reverseParse(script: List<String>): List<StandardizedTags> {
            return script.map { valueOf(it) }
        }

        fun valueOf(tage: String): StandardizedTags {
            return entries.find { it.tage == tage }
                ?: throw IllegalArgumentException("Invalid Standardized Tag: $tage")
        }



    }

    override val length: Int get() = tage.length

    override fun get(index: Int): Char = tage[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return tage.subSequence(startIndex, endIndex)
    }

}


fun main() {

    val eventName = StandardizedTags.TAG_E
    val pubkey = StandardizedTags.TAG_P
    val coordinates = StandardizedTags.TAG_A

    val parseSingle = StandardizedTags.parse(eventName)
    println(parseSingle)

    when (eventName) {
        StandardizedTags.TAG_E -> println("Event tag")
        else -> println("Other tag")
    }

    val script = listOf(eventName, pubkey, coordinates)
    println(eventName.tage)

    val analyzedResult = StandardizedTags.parse(script)
    println("Analyzed Result: $analyzedResult")
    val reverseParsedResult = StandardizedTags.reverseParse(analyzedResult)
    println("Reverse Parsed Result: $reverseParsedResult")

}
