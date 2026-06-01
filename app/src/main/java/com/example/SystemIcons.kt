/* * PROPRIETARY AND CONFIDENTIAL
 * PROPERTY OF ARY LABS CORP. SOFTWARE SYSTEMS
 * DEVELOPER: ALI RIZA YILMAZ
 * COPYRIGHT © 2026 ALL RIGHTS RESERVED.
 * UNAUTHORIZED COPYING OR DISTRIBUTION IS STRICTLY PROHIBITED.
 */
package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SystemIcons {
    val Camera: ImageVector
        get() = ImageVector.Builder(
            name = "Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                // simple camera shape
                moveTo(4f, 8f)
                lineTo(8f, 8f)
                lineTo(10f, 6f)
                lineTo(14f, 6f)
                lineTo(16f, 8f)
                lineTo(20f, 8f)
                lineTo(20f, 20f)
                lineTo(4f, 20f)
                close()
            }
        }.build()

    val Phone: ImageVector
        get() = androidx.compose.material.icons.Icons.Default.run { 
            // We can just use the path from Call if available, but let's draw a simple phone:
            ImageVector.Builder(
                name = "Phone",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.White)) {
                    moveTo(6f, 4f)
                    lineTo(6f, 20f)
                    lineTo(18f, 20f)
                    lineTo(18f, 4f)
                    close()
                }
            }.build()
        }

    val Flashlight: ImageVector
        get() = ImageVector.Builder(
            name = "Flashlight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(8f, 2f)
                lineTo(16f, 2f)
                lineTo(16f, 8f)
                lineTo(12f, 22f)
                lineTo(8f, 8f)
                close()
            }
        }.build()
}
