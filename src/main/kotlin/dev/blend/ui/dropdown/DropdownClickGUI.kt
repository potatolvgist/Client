package dev.blend.ui.dropdown

import dev.blend.Client
import dev.blend.module.api.Category
import dev.blend.module.impl.client.ClickGUIModule
import dev.blend.ui.dropdown.components.CategoryComponent
import dev.blend.util.render.DrawUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object DropdownClickGUI: Screen(Text.of("Dropdown Click GUI")) {

    private val components = mutableListOf<CategoryComponent>()

    init {
        var x = 20.0
        Category.entries.forEach {
            val component = CategoryComponent(it)
            component.x = x
            component.y = 20.0
            components.add(component)
            x += component.width + 10.0
        }
    }

    override fun init() {
        components.forEach{
            it.init()
        }
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        DrawUtil.begin()
        components.forEach {
            it.render(mouseX, mouseY)
        }
        DrawUtil.end()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        Client.logger.info("Clicked at X: $mouseX Y: $mouseY")
        components.forEach {
            if (it.click(mouseX, mouseY, button)) {
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        components.forEach {
            if (it.release(mouseX, mouseY, button)) {
                return true
            }
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        components.forEach {
            if (it.key(keyCode, scanCode, modifiers)) {
                return true
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun close() {
        components.forEach{
            it.close()
        }
        ClickGUIModule.set(false)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun shouldCloseOnEsc(): Boolean {
        return true
    }

}