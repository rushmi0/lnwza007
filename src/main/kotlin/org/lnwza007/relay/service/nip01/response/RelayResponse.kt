package org.lnwza007.relay.service.nip01.response

import io.micronaut.websocket.WebSocketSession
import org.lnwza007.relay.modules.Event
import org.lnwza007.util.ShiftTo.toJsonString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * RelayResponse เป็นคลาสหลักที่ใช้ในการจัดการการตอบกลับของ Relay
 * สามารถมีหลายประเภทของการตอบกลับได้ โดยแต่ละประเภทจะถูกกำหนดเป็น subclass ของ RelayResponse
 */
sealed class RelayResponse<out T> {

    /**
     * EVENT เป็นการตอบกลับประเภทเหตุการณ์
     * @param subscriptionId ไอดีของการสมัครสมาชิกที่เกิดเหตุการณ์
     * @param event เหตุการณ์ที่เกิดขึ้น
     * ใช้ในการส่งเหตุการณ์ที่ได้รับการร้องขอจากไคลเอนต์
     */
    data class EVENT(val subscriptionId: String, val event: Event) : RelayResponse<Unit>()

    /**
     * OK เป็นการตอบกลับประเภทการยืนยันความสำเร็จของการดำเนินการ
     * @param eventId ไอดีของเหตุการณ์ที่ได้รับการยืนยัน
     * @param isSuccess ผลลัพธ์ว่าการดำเนินการสำเร็จหรือไม่
     * @param message ข้อความเพิ่มเติม
     * ใช้ในการบอกสถานะการยอมรับหรือปฏิเสธข้อความ EVENT จากไคลเอนต์
     * จะมีพารามิเตอร์ที่ 2 เป็น true เมื่อเหตุการณ์ได้รับการยอมรับจาก relay และ false ในกรณีอื่นๆ เช่นการปฏิเสธ EVENT จากไคลเอนต์
     * พารามิเตอร์ที่ 3 จะต้องมีเสมอ อาจจะเป็นสตริงว่างเมื่อพารามิเตอร์ที่ 2 เป็น true หรือเป็น false และแจ้งเหตุผลที่ปฏิเสธ EVENT นั้นๆ
     */
    data class OK(val eventId: String, val isSuccess: Boolean, val message: String = "") : RelayResponse<Unit>()

    /**
     * EOSE เป็นการตอบกลับเมื่อสิ้นสุดการส่งข้อมูลของการสมัครสมาชิก
     * @param subscriptionId ไอดีของการสมัครสมาชิก
     * ใช้ในการบอกว่าจบการส่งเหตุการณ์ที่เก็บไว้แล้ว และจะเริ่มส่งเหตุการณ์ใหม่ๆ ที่ได้รับในเวลาจริง
     */
    data class EOSE(val subscriptionId: String) : RelayResponse<Unit>()

    /**
     * CLOSED เป็นการตอบกลับเมื่อการสมัครสมาชิกถูกปิด
     * @param subscriptionId ไอดีของการสมัครสมาชิก
     * @param message ข้อความเพิ่มเติม
     * ใช้ในการบอกว่าการสมัครสมาชิกถูกปิดจากฝั่งเซิร์ฟเวอร์
     * สามารถส่งได้เมื่อ relay ปฏิเสธการตอบรับการสมัครสมาชิกหรือเมื่อ relay ตัดสินใจยกเลิกการสมัครสมาชิกก่อนที่ไคลเอนต์จะยกเลิกหรือส่ง CLOSE
     */
    data class CLOSED(val subscriptionId: String, val message: String = "") : RelayResponse<Unit>()

    /**
     * NOTICE เป็นการตอบกลับประเภทการแจ้งเตือน
     * @param message ข้อความแจ้งเตือน
     * ใช้ในการส่งข้อความแจ้งเตือนที่อ่านได้โดยมนุษย์หรือข้อความอื่นๆ ไปยังไคลเอนต์
     */
    data class NOTICE(val message: String = "") : RelayResponse<Unit>()

    /**
     * ฟังก์ชัน toJson ใช้ในการแปลงการตอบกลับเป็น JSON string
     * @return JSON string ที่แทนการตอบกลับ
     */
    fun toJson(): String {
        return when (this) {
            is EVENT -> listOf("EVENT", subscriptionId, event).toJsonString()
            is OK -> listOf("OK", eventId, isSuccess, message).toJsonString()
            is EOSE -> listOf("EOSE", subscriptionId).toJsonString()
            is CLOSED -> listOf("CLOSED", subscriptionId, message).toJsonString()
            is NOTICE -> listOf("NOTICE", message).toJsonString()
        }
    }

    /**
     * ฟังก์ชัน toClient ใช้ในการส่งการตอบกลับไปยังไคลเอนต์ผ่าน WebSocket
     * @param session WebSocketSession ที่ใช้ในการสื่อสารกับไคลเอนต์
     */
    fun toClient(session: WebSocketSession) {
        if (session.isOpen) {
            val payload = this.toJson() // แปลงการตอบกลับเป็น JSON string
            try {
                session.sendSync(payload) // ส่งข้อมูลไปยังไคลเอนต์
                if (this is CLOSED) {
                    session.close() // ปิดการเชื่อมต่อถ้าการตอบกลับเป็น CLOSED
                }
            } catch (e: Exception) {
                LOG.error("Error sending WebSocket message: ${e.message}") // แจ้งเตือนเมื่อมีข้อผิดพลาดในการส่งข้อมูล
            }
        } else {
            LOG.warn("Attempted to send message to closed WebSocket session.") // แจ้งเตือนเมื่อพยายามส่งข้อความไปยัง session ที่ปิดแล้ว
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(RelayResponse::class.java)
    }

}
