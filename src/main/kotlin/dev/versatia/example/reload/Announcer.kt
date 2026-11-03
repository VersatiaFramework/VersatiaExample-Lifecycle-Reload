package dev.versatia.example.reload

import dev.versatia.Phase
import dev.versatia.VersatiaEngine
import dev.versatia.broadcast
import dev.versatia.config.PreviousValueTracking
import dev.versatia.config.Value
import dev.versatia.di.LocalBean
import dev.versatia.lifecycle.AutoInvoke
import dev.versatia.util.scheduler.runTaskTimer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.scheduler.BukkitTask

/** Broadcasts the configured announcement; the bean is created once and everything it shows follows each reload. */
@LocalBean
class Announcer(private val plugin: AnnouncerPlugin) {
    @Value("announcement.text")
    lateinit var text: String

    /** Tracked, so the task is rescheduled only when a reload really changes the interval. */
    @Value("announcement.interval-seconds")
    val intervalSeconds = PreviousValueTracking<Int>()

    /** Derived from `text`: parsed at every start and reload, not at every broadcast. */
    private var announcement: Component = Component.empty()
    private var task: BukkitTask? = null

    @AutoInvoke(Phase.PLUGIN_RELOAD_OR_START)
    fun apply() {
        announcement = MiniMessage.miniMessage().deserialize(text)
        plugin.logger.info("Announcement: ${PlainTextComponentSerializer.plainText().serialize(announcement)}")
        if (!intervalSeconds.changed) return plugin.logger.info("Interval still ${intervalSeconds.value} s, the task keeps running")
        task?.cancel()
        val ticks = intervalSeconds.value * 20L
        task = plugin.runTaskTimer(periodTicks = ticks, delayTicks = ticks) { VersatiaEngine.broadcast(announcement) }
        plugin.logger.info(
            if (intervalSeconds.hasPrevious) "Interval ${intervalSeconds.previous} s -> ${intervalSeconds.value} s, task rescheduled"
            else "Announcing every ${intervalSeconds.value} s",
        )
    }
}
