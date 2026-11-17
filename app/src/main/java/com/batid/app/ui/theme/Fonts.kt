package com.batid.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.batid.app.R

/** Poppins: body text, labels, buttons, screen titles. Bundled in res/font. */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

/**
 * Archivo Black: BATid branding and selected headings.
 *
 * TODO: download "Archivo Black" from fonts.google.com, save it as
 * app/src/main/res/font/archivo_black.ttf, then replace the line below with:
 *     val ArchivoBlack = FontFamily(Font(R.font.archivo_black, FontWeight.Normal))
 * Until then Poppins Bold is used as a stand-in.
 */
val ArchivoBlack: FontFamily = Poppins
