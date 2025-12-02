package com.example.dailydoodle.ui.components.onboarding

/**
 * Splits a list into a Triple of lists for rotating display.
 * Used to show features in a rotating carousel.
 * 
 * @param index The current index to start from
 * @return Triple containing three sublists for display
 */
fun <T> List<T>.splitIntoTriple(index: Int): Triple<List<T>, List<T>, List<T>> {
    if (this.size < 3) {
        return Triple(this, emptyList(), emptyList())
    }
    
    val normalizedIndex = index % this.size
    val firstList = listOfNotNull(this.getOrNull(normalizedIndex))
    val secondList = listOfNotNull(this.getOrNull((normalizedIndex + 1) % this.size))
    val thirdList = listOfNotNull(this.getOrNull((normalizedIndex + 2) % this.size))
    
    return Triple(firstList, secondList, thirdList)
}
