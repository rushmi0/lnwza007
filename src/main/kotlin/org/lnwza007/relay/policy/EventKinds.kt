package org.lnwza007.relay.policy

import io.micronaut.core.annotation.Internal
import io.micronaut.core.annotation.NonNull

enum class EventKinds(val code: Int, val description: String, val nip: String) : CharSequence {
    METADATA(0, "Metadata", "01"),
    TEXT_NOTE(1, "Short Text Note", "01"),
    RECOMMEND_RELAY(2, "Recommend Relay", "01 (deprecated)"),
    FOLLOWS(3, "Follows", "02"),
    ENCRYPTED_DIRECT_MESSAGES(4, "Encrypted Direct Messages", "04"),
    EVENT_DELETION(5, "Event Deletion", "09"),
    REPOST(6, "Repost", "18"),
    REACTION(7, "Reaction", "25"),
    BADGE_AWARD(8, "Badge Award", "58"),
    GROUP_CHAT_MESSAGE(9, "Group Chat Message", "29"),
    GROUP_CHAT_THREADED_REPLY(10, "Group Chat Threaded Reply", "29"),
    GROUP_THREAD(11, "Group Thread", "29"),
    GROUP_THREAD_REPLY(12, "Group Thread Reply", "29"),
    SEAL(13, "Seal", "59"),
    DIRECT_MESSAGE(14, "Direct Message", "17"),
    GENERIC_REPOST(16, "Generic Repost", "18"),
    CHANNEL_CREATION(40, "Channel Creation", "28"),
    CHANNEL_METADATA(41, "Channel Metadata", "28"),
    CHANNEL_MESSAGE(42, "Channel Message", "28"),
    CHANNEL_HIDE_MESSAGE(43, "Channel Hide Message", "28"),
    CHANNEL_MUTE_USER(44, "Channel Mute User", "28"),
    MERGE_REQUESTS(818, "Merge Requests", "54"),
    BID(1021, "Bid", "15"),
    BID_CONFIRMATION(1022, "Bid confirmation", "15"),
    OPEN_TIMESTAMPS(1040, "Open Timestamps", "03"),
    GIFT_WRAP(1059, "Gift Wrap", "59"),
    FILE_METADATA(1063, "File Metadata", "94"),
    LIVE_CHAT_MESSAGE(1311, "Live Chat Message", "53"),
    PATCHES(1617, "Patches", "34"),
    ISSUES(1621, "Issues", "34"),
    REPLIES(1622, "Replies", "34"),
    STATUS_1630_1633(1630, "Status", "34"),
    PROBLEM_TRACKER(1971, "Problem Tracker", "nostrocket"),
    REPORTING(1984, "Reporting", "56"),
    LABEL(1985, "Label", "32"),
    TORRENT(2003, "Torrent", "35"),
    TORRENT_COMMENT(2004, "Torrent Comment", "35"),
    COINJOIN_POOL(2022, "Coinjoin Pool", "joinstr"),
    COMMUNITY_POST_APPROVAL(4550, "Community Post Approval", "72"),
    JOB_REQUEST(5000, "Job Request", "90"),
    JOB_RESULT(6000, "Job Result", "90"),
    JOB_FEEDBACK(7000, "Job Feedback", "90"),
    GROUP_CONTROL_EVENTS(9000, "Group Control Events", "29"),
    ZAP_GOAL(9041, "Zap Goal", "75"),
    ZAP_REQUEST(9734, "Zap Request", "57"),
    ZAP(9735, "Zap", "57"),
    HIGHLIGHTS(9802, "Highlights", "84"),
    MUTE_LIST(10000, "Mute list", "51"),
    PIN_LIST(10001, "Pin list", "51"),
    RELAY_LIST_METADATA(10002, "Relay List Metadata", "65"),
    BOOKMARK_LIST(10003, "Bookmark list", "51"),
    COMMUNITIES_LIST(10004, "Communities list", "51"),
    PUBLIC_CHATS_LIST(10005, "Public chats list", "51"),
    BLOCKED_RELAYS_LIST(10006, "Blocked relays list", "51"),
    SEARCH_RELAYS_LIST(10007, "Search relays list", "51"),
    USER_GROUPS(10009, "User groups", "51, 29"),
    INTERESTS_LIST(10015, "Interests list", "51"),
    USER_EMOJI_LIST(10030, "User emoji list", "51"),
    RELAY_LIST_TO_RECEIVE_DMS(10050, "Relay list to receive DMs", "17"),
    FILE_STORAGE_SERVER_LIST(10096, "File storage server list", "96"),
    WALLET_INFO(13194, "Wallet Info", "47"),
    LIGHTNING_PUB_RPC(21000, "Lightning Pub RPC", "Lightning.Pub"),
    CLIENT_AUTHENTICATION(22242, "Client Authentication", "42"),
    WALLET_REQUEST(23194, "Wallet Request", "47"),
    WALLET_RESPONSE(23195, "Wallet Response", "47"),
    NOSTR_CONNECT(24133, "Nostr Connect", "46"),
    HTTP_AUTH(27235, "HTTP Auth", "98"),
    FOLLOW_SETS(30000, "Follow sets", "51"),
    GENERIC_LISTS(30001, "Generic lists", "51"),
    RELAY_SETS(30002, "Relay sets", "51"),
    BOOKMARK_SETS(30003, "Bookmark sets", "51"),
    CURATION_SETS(30004, "Curation sets", "51"),
    VIDEO_SETS(30005, "Video sets", "51"),
    PROFILE_BADGES(30008, "Profile Badges", "58"),
    BADGE_DEFINITION(30009, "Badge Definition", "58"),
    INTEREST_SETS(30015, "Interest sets", "51"),
    CREATE_OR_UPDATE_A_STALL(30017, "Create or update a stall", "15"),
    CREATE_OR_UPDATE_A_PRODUCT(30018, "Create or update a product", "15"),
    MARKETPLACE_UI_UX(30019, "Marketplace UI/UX", "15"),
    PRODUCT_SOLD_AS_AN_AUCTION(30020, "Product sold as an auction", "15"),
    LONG_FORM_CONTENT(30023, "Long-form Content", "23"),
    DRAFT_LONG_FORM_CONTENT(30024, "Draft Long-form Content", "23"),
    EMOJI_SETS(30030, "Emoji sets", "51"),
    RELEASE_ARTIFACT_SETS(30063, "Release artifact sets", "51"),
    APPLICATION_SPECIFIC_DATA(30078, "Application-specific Data", "78"),
    LIVE_EVENT(30311, "Live Event", "53"),
    USER_STATUSES(30315, "User Statuses", "38"),
    CLASSIFIED_LISTING(30402, "Classified Listing", "99"),
    DRAFT_CLASSIFIED_LISTING(30403, "Draft Classified Listing", "99"),
    REPOSITORY_ANNOUNCEMENTS(30617, "Repository announcements", "34"),
    WIKI_ARTICLE(30818, "Wiki article", "54"),
    REDIRECTS(30819, "Redirects", "54"),
    FEED(31890, "Feed", "NUD: Custom Feeds"),
    DATE_BASED_CALENDAR_EVENT(31922, "Date-Based Calendar Event", "52"),
    TIME_BASED_CALENDAR_EVENT(31923, "Time-Based Calendar Event", "52"),
    CALENDAR(31924, "Calendar", "52"),
    CALENDAR_EVENT_RSVP(31925, "Calendar Event RSVP", "52"),
    HANDLER_RECOMMENDATION(31989, "Handler recommendation", "89"),
    HANDLER_INFORMATION(31990, "Handler information", "89"),
    VIDEO_EVENT(34235, "Video Event", "71"),
    SHORT_FORM_PORTRAIT_VIDEO_EVENT(34236, "Short-form Portrait Video Event", "71"),
    VIDEO_VIEW_EVENT(34237, "Video View Event", "71"),
    COMMUNITY_DEFINITION(34550, "Community Definition", "72"),
    GROUP_METADATA_EVENTS(39000, "Group metadata events", "29");

    companion object {

        fun parse(script: List<EventKinds>): List<Int> {
            return script.map { it.code }
        }

        fun reverseParse(script: List<Int>): List<EventKinds> {
            return script.map { EventKinds.valueOf(it) }
        }


        fun valueOf(code: Int): EventKinds {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Invalid EventKinds code: $code")
        }

        @Internal
        @NonNull
        fun getDefaultDescription(code: Int): String {
            return try {
                valueOf(code).description
            } catch (e: IllegalArgumentException) {
                "CUSTOM"
            }
        }

    }

    override val length: Int get() = name.length

    override fun get(index: Int): Char = name[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return name.subSequence(startIndex, endIndex)
    }


}


fun main() {

    val metadata = EventKinds.METADATA
    val text = EventKinds.TEXT_NOTE
    val zap = EventKinds.ZAP

    val script = listOf(metadata, text, zap)

    val analyzedResult = EventKinds.parse(script)
    println("Analyzed Result: $analyzedResult")
    val reverseParsedResult = EventKinds.reverseParse(analyzedResult)
    println("Reverse Parsed Result: $reverseParsedResult")

    println(metadata)
    println("Event Kind: ${metadata.code} - ${metadata.description} (NIP: ${metadata.nip})")

    // 2. แปลงโค้ด Event Kinds เป็น Enum และแสดงผล
    val eventCode = 3
    val follows = EventKinds.valueOf(eventCode)
    println("Event Kind: ${follows.code} - ${follows.description} (NIP: ${follows.nip})")

    // 3. ใช้ค่าโค้ดเพื่อรับคำอธิบายเริ่มต้นของเหตุการณ์
    val description = EventKinds.getDefaultDescription(5)
    println("Default description for event code 5: $description")

}
