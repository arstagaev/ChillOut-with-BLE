package com.arstagaev.liteble

class BleConductor(private val items: List<String>) {

    var tapeInstructions = arrayListOf<String>()

    constructor(vararg items: String) : this(items.toList())

    init {
        tapeInstructions = items as ArrayList<String>
    }
}