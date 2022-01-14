package com.example.mri_pepperdiscussion
import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.builder.*
import com.aldebaran.qi.sdk.design.activity.RobotActivity

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    // Store the Chat action.
    private var chat: Chat? = null
    private lateinit var btnExit: Button
    private lateinit var helloText: TextView
    private lateinit var imgUnderstood: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnExit = findViewById(R.id.btnExit)
        helloText = findViewById(R.id.helloText)
        helloText.text = " "
        imgUnderstood = findViewById(R.id.understood)

        btnExit.setOnClickListener {
            finish()
        }
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun onRobotFocusGained(qiContext: QiContext) {

        // Create a topic.
        val topic: Topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
            .withResource(R.raw.discussion) // Set the topic resource.
            .build() // Build the topic.

        // Create a QiChatbot
        val qichatbot: QiChatbot = QiChatbotBuilder.with(qiContext)
            .withTopic(topic)
            .build()

        val chat: Chat = ChatBuilder.with(qiContext)
            .withChatbot(qichatbot)
            .build()

        val executors = HashMap<String, QiChatExecutor>()

        // Map the executor name from the topic to our qiChatbotExecutor
        executors["goodbye"] = GoodbyeExecuter(qiContext)
        executors["yeah"] = YeahExecuter(qiContext)
        executors["sad"] = SadExecuter(qiContext)
        executors["reaction"] = ReactionExecuter(qiContext)
        // Set the executors to the qiChatbot
        qichatbot.executors = executors

        // Add an on started listener to the Chat action.
        chat?.addOnStartedListener { Log.i(TAG, "Discussion started.") }
        // Execute the chat asynchronously
        val fchat: com.aldebaran.qi.Future<Void> = chat.async().run()

        // Stop the chat when the qichatbot is done
        qichatbot.addOnEndedListener { endReason ->
            Log.i(TAG, "qichatbot end reason = $endReason")
            fchat.requestCancellation()
        }
        chat.addOnHeardListener {
            imgUnderstood.setImageResource(R.drawable.green)
            helloText.text = " "
        }

        chat.addOnFallbackReplyFoundForListener { input ->
            imgUnderstood.setImageResource(R.drawable.red)
            helloText.text = " "
        }
    }

    // Store the proposal bookmark.
    private var proposalBookmark: Bookmark? = null

    override fun onRobotFocusLost() {
        // The robot focus is lost.
        // Remove on started listeners from the Chat action.
        chat?.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // The robot focus is refused.
    }

    internal inner class GoodbyeExecuter(qiContext: QiContext) : BaseQiChatExecutor(qiContext) {

        override fun runWith(params: List<String>) {
            changeScreen(qiContext)
        }

        override fun stop() {
            // This is called when chat is canceled or stopped.
        }

        private fun changeScreen(qiContext: QiContext) {
            imgUnderstood.setImageResource(R.drawable.goodbye)
        }
    }
    internal inner class YeahExecuter(qiContext: QiContext) : BaseQiChatExecutor(qiContext) {

        override fun runWith(params: List<String>) {
            changeScreen(qiContext)
        }

        override fun stop() {
            // This is called when chat is canceled or stopped.
        }

        private fun changeScreen(qiContext: QiContext) {
            imgUnderstood.setImageResource(R.drawable.peppertastisch)
        }
    }
    internal inner class SadExecuter(qiContext: QiContext) : BaseQiChatExecutor(qiContext) {

        override fun runWith(params: List<String>) {
            changeScreen(qiContext)
        }

        override fun stop() {
            // This is called when chat is canceled or stopped.
        }

        private fun changeScreen(qiContext: QiContext) {
            imgUnderstood.setImageResource(R.drawable.sad)
        }
    }
    internal inner class ReactionExecuter(qiContext: QiContext) : BaseQiChatExecutor(qiContext) {

        override fun runWith(params: List<String>) {
            animate(qiContext)
        }

        override fun stop() {
            // This is called when chat is canceled or stopped.
        }
        private fun animate(qiContext: QiContext) {
            // Create an animation.
            val animation: com.aldebaran.qi.sdk.`object`.actuation.Animation? = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.thinking_a001) // Set the animation resource.
                .build() // Build the animation.

            // Create an animate action.
            val animate: Animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build() // Build the animate action.
            animate.run()
        }
    }
}