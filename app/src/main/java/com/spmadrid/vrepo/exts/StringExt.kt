package com.spmadrid.vrepo.exts

import java.text.Normalizer


fun String.removeDiacritics(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
}

fun String.removeSpecialCharacters(): String {
    return this.replace(Regex("[^a-zA-Z0-9]"), "")
}