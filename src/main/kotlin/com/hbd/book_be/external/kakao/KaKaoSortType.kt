package com.hbd.book_be.external.kakao

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class SortType(val value: String) {
    ACCURACY("ACCURACY"),
    LATEST("LATEST");
}
