package dev.versatia.example.reload

import dev.versatia.FriendlyName
import dev.versatia.Phase
import dev.versatia.VersatiaPlugin
import dev.versatia.config.Value
import dev.versatia.lifecycle.AutoInvoke
import dev.versatia.util.scheduler.runTaskTimer

/** Registered as `announcer`, so `/versatia reload announcer` reloads it; with `auto-reload` it also reloads itself from code. */
@FriendlyName("announcer")
class AnnouncerPlugin : VersatiaPlugin() {
    /** Read at every check rather than copied into the task, so a reload switching it takes effect at once. */
    @Value("auto-reload")
    var autoReload = false

    private val config get() = dataFolder.resolve("config.yml")
    private var seen = 0L

    /** Every reload, whoever asked for it, starts from the file as it is now. */
    @AutoInvoke(Phase.PLUGIN_RELOAD_OR_START)
    private fun remember() {
        seen = config.lastModified()
    }

    /** A task outlives reloads, so it is started once. */
    @AutoInvoke(Phase.PLUGIN_START_ONLY)
    private fun watch() {
        runTaskTimer(periodTicks = 40) {
            if (autoReload && config.lastModified() != seen) reloadFromCode()
        }
    }

    private fun reloadFromCode() {
        seen = config.lastModified()
        logger.info("config.yml changed, reloading")
        try {
            versatiaReload()
        } catch (e: Exception) {
            logger.warning("Reload failed, the previous values stay: ${e.message}")
        }
    }
}
