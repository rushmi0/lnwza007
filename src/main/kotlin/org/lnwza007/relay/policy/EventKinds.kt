package org.lnwza007.relay.policy


object EventKinds {

    public const val SET_METADATA: Int = 0
    public const val TEXT_NOTE: Int = 1
    public const val RECOMMEND_SERVER: Int = 2

}

fun main() {


    // เรียกใช้ enum โดยใช้ชื่อของ enum ตามที่เป็นปกติ
    val eventType = EventKinds.TEXT_NOTE
    println("Event type: $eventType")

    // วิธีการเรียกใช้ enum ในเงื่อนไข (if statement)
    if (eventType == EventKinds.TEXT_NOTE) {
        println("This is a text note event.")
    } else {
        println("This is not a text note event.")
    }
}
