package com.example.autoscroller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler

class AutoScrollService : AccessibilityService() {
    private var scrollHandler: Handler = Handler()
    private var scrollRunnable: Runnable? = null
    private var scrollSpeed: Long = 1000
    private var scrollDistance: Int = 50
    private var scrollDirection: Int = 1

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        stopScrolling()
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100
        serviceInfo = info
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scrollSpeed = intent?.getLongExtra("scrollSpeed", 1000) ?: 1000
        scrollDistance = intent?.getIntExtra("scrollDistance", 50) ?: 50
        scrollDirection = intent?.getIntExtra("scrollDirection", 1) ?: 1

        startScrolling()

        return START_STICKY
    }

    private fun startScrolling() {
        stopScrolling()
        scrollRunnable = object : Runnable {
            override fun run() {
                scrollScreen()
                scrollHandler.postDelayed(this, scrollSpeed)
            }
        }
        scrollHandler.post(scrollRunnable!!)
    }

    fun stopScrolling() {
        scrollRunnable?.let {
            scrollHandler.removeCallbacks(it)
        }
    }

    private fun scrollScreen() {
        val rootNode = rootInActiveWindow ?: return

        fun performScroll(node: AccessibilityNodeInfo): Boolean {
            return when (scrollDirection) {
                1 -> node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) // Top to Bottom
                2 -> node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) // Bottom to Top
                3 -> node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) // Left to Right
                4 -> node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) // Right to Left
                else -> false
            }
        }

        traverseAndScroll(rootNode, ::performScroll)
    }

    private fun traverseAndScroll(node: AccessibilityNodeInfo, action: (AccessibilityNodeInfo) -> Boolean) {
        if (action(node)) return
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseAndScroll(child, action)
        }
    }
}