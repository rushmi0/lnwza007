package org.lnwza007.util

import java.security.MessageDigest

object ShiftTo {




    fun ByteArray.SHA256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }


    fun String.SHA256(): ByteArray {
        return toByteArray().SHA256()
    }

}