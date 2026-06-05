package com.navrotskyi.trippyapp.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spójna skala odstępów i promieni używana w całej aplikacji.
 * Zamiast wpisywać "magiczne" wartości dp na każdym ekranie, korzystamy z tych tokenów,
 * dzięki czemu paddingi i marginesy są jednolite na wszystkich widokach.
 */
object Dimens {
    /** 4.dp – najmniejszy odstęp (np. ikona-tekst). */
    val SpaceXs = 4.dp

    /** 8.dp – mały odstęp między powiązanymi elementami. */
    val SpaceSm = 8.dp

    /** 12.dp – odstęp wewnątrz list / kart. */
    val SpaceMd = 12.dp

    /** 16.dp – standardowy odstęp / padding ekranów osadzonych w TopAppBar. */
    val SpaceLg = 16.dp

    /** 20.dp – odstęp między polami formularzy. */
    val SpaceXl = 20.dp

    /** 24.dp – poziomy padding głównych ekranów (lista podróży, finanse). */
    val ScreenPadding = 24.dp

    /** 32.dp – padding ekranów logowania / rejestracji. */
    val SpaceXxl = 32.dp

    /** Promień zaokrąglenia kart. */
    val CardCorner = 16.dp

    /** Promień zaokrąglenia pól tekstowych / przycisków. */
    val FieldCorner = 12.dp

    /** Domyślna wysokość uniesienia kart. */
    val CardElevation = 2.dp

    /** Margines dolny pod listami z FAB, żeby przycisk nie zasłaniał ostatniego elementu. */
    val FabListBottomPadding = 88.dp
}
