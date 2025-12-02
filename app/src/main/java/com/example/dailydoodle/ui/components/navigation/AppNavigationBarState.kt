package com.example.dailydoodle.ui.components.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Singleton object to manage the visibility state of the bottom navigation bar.
 * 
 * Use this to show/hide the navigation bar programmatically:
 * - show(): Show the bar (if not locked)
 * - hide(): Hide the bar (if not locked)
 * - showWithUnlock(): Unlock and show
 * - hideWithLock(): Hide and lock (prevents other show calls)
 */
object AppNavigationBarState {
    private var _visible: MutableState<Boolean> = mutableStateOf(true)
    private var _locked: MutableState<Boolean> = mutableStateOf(false)

    val isVisible: State<Boolean> = _visible
    val isLocked: State<Boolean> = _locked

    /**
     * Show the navigation bar (if not locked)
     */
    fun show() {
        if (isLocked.value.not())
            _visible.value = true
    }

    /**
     * Hide the navigation bar (if not locked)
     */
    fun hide() {
        if (isLocked.value.not())
            _visible.value = false
    }

    /**
     * Unlock and show the navigation bar
     */
    fun showWithUnlock() {
        unlock()
        show()
    }

    /**
     * Hide and lock the navigation bar
     * Useful when navigating to secondary screens
     */
    fun hideWithLock() {
        hide()
        lock()
    }

    /**
     * Lock the navigation bar (prevents show/hide)
     */
    fun lock() {
        _locked.value = true
    }

    /**
     * Unlock the navigation bar
     */
    fun unlock() {
        _locked.value = false
    }
}
